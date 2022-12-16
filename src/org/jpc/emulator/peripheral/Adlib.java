package org.jpc.emulator.peripheral;

import org.jpc.emulator.AbstractHardwareComponent;
import org.jpc.emulator.HardwareComponent;
import org.jpc.emulator.motherboard.IODevice;
import org.jpc.emulator.motherboard.IOPortHandler;
import org.jpc.j2se.Option;
import org.jpc.support.Clock;

public class Adlib extends AbstractHardwareComponent implements IODevice {
    private static final int HW_OPL2 = 0;
    private static final int HW_DUALOPL2 = 1;
    private static final int HW_OPL3 = 2;

    private boolean ioportRegistered;
    private boolean single;
    private Clock timeSource;

    private static class RawHeader {
        /*Bit8u*/byte[] id = new byte[8];
        /* 0x00, "DBRAWOPL" */
        /*Bit16u*/int versionHigh;
        /* 0x08, size of the data following the m */
        /*Bit16u*/int versionLow;
        /* 0x0a, size of the data following the m */
        /*Bit32u*/long commands;
        /* 0x0c, Bit32u amount of command/data pairs */
        /*Bit32u*/long milliseconds;
        /* 0x10, Bit32u Total milliseconds of data in this chunk */
        /*Bit8u*/short hardware;
        /* 0x14, Bit8u Hardware Type 0=opl2,1=dual-opl2,2=opl3 */
        /*Bit8u*/short format;
        /* 0x15, Bit8u Format 0=cmd/data interleaved, 1 maybe all cdms, followed by all data */
        /*Bit8u*/short compression;
        /* 0x16, Bit8u Compression Type, 0 = No Compression */
        /*Bit8u*/short delay256;
        /* 0x17, Bit8u Delay 1-256 msec command */
        /*Bit8u*/short delayShift8;
        /* 0x18, Bit8u (delay + 1)*256 */
        /*Bit8u*/short conversionTableSize; /* 0x191, Bit8u Raw Conversion Table size */
    }

    private static final class Timer {
        double start;
        double delay;
        boolean enabled, overflow, masked;
        /*Bit8u*/short counter;

        Timer() {
            masked = false;
            overflow = false;
            enabled = false;
            counter = 0;
            delay = 0;
        }

        //Call update before making any further changes
        void Update(double time) {
            if (!enabled || delay == 0)
                return;
            double deltaStart = time - start;
            //Only set the overflow flag when not masked
            if (deltaStart >= 0 && !masked) {
                overflow = true;
            }
        }

        //On a reset make sure the start is in sync with the next cycle
        void Reset(double time) {
            overflow = false;
            if (delay == 0 || !enabled)
                return;
            double delta = time - start;
            double rem = delta % delay;
            double next = delay - rem;
            start = time + next;
        }

        void Stop() {
            enabled = false;
        }

        void Start(double time, /*Bits*/int scale) {
            //Don't enable again
            if (enabled) {
                return;
            }
            enabled = true;
            delay = 0.001 * (256 - counter) * scale;
            start = time + delay;
        }
    }

    private final class Chip {
        //Last selected register
        Timer[] timer = new Timer[2];

        public Chip() {
            for (int i = 0; i < timer.length; i++) {
                timer[i] = new Timer();
            }
        }

        //Check for it being a write to the timer
        boolean Write(/*Bit32u*/int addr, /*Bit8u*/short val) {
            switch (addr) {
            case 0x02:
                timer[0].counter = val;
                return true;
            case 0x03:
                timer[1].counter = val;
                return true;
            case 0x04:
                double time = timeSource.getEmulatedNanos();
                if ((val & 0x80) != 0) {
                    timer[0].Reset(time);
                    timer[1].Reset(time);
                } else {
                    timer[0].Update(time);
                    timer[1].Update(time);
                    if ((val & 0x1) != 0) {
                        timer[0].Start(time, 80);
                    } else {
                        timer[0].Stop();
                    }
                    timer[0].masked = (val & 0x40) > 0;
                    if (timer[0].masked)
                        timer[0].overflow = false;
                    if ((val & 0x2) != 0) {
                        timer[1].Start(time, 320);
                    } else {
                        timer[1].Stop();
                    }
                    timer[1].masked = (val & 0x20) > 0;
                    if (timer[1].masked)
                        timer[1].overflow = false;

                }
                return true;
            }
            return false;
        }

        //Read the current timer state, will use current double
        /*Bit8u*/short Read() {
            double time = timeSource.getEmulatedNanos();
            timer[0].Update(time);
            timer[1].Update(time);
            /*Bit8u*/short ret = 0;
            //Overflow won't be set if a channel is masked
            if (timer[0].overflow) {
                ret |= 0x40;
                ret |= 0x80;
            }
            if (timer[1].overflow) {
                ret |= 0x20;
                ret |= 0x80;
            }
            return ret;
        }
    }

//The type of handler this is
    private static final int MODE_OPL2 = 0;
    private static final int MODE_DUALOPL2 = 1;
    private static final int MODE_OPL3 = 2;

    public interface Handler {
        //Write an address to a chip, returns the address the chip sets
        /*Bit32u*/long WriteAddr( /*Bit32u*/int port, /*Bit8u*/short val);

        //Write to a specific register in the chip
        void WriteReg( /*Bit32u*/int addr, /*Bit8u*/short val);

        //Generate a certain amount of samples
        void Generate(Mixer.MixerChannel chan, /*Bitu*/int samples);

        //Initialize at a specific sample rate and mode
        void Init( /*Bitu*/long rate);
    }

    private class Module {
        private Mixer.MixerObject mixerObject = new Mixer.MixerObject();

        public Module() {
            for (int i = 0; i < chip.length; i++) {
                chip[i] = new Chip();
            }
            reg.normal = 0;

            /*Bitu*/int base = Option.sbbase.intValue(SBlaster.BASE, 16);
            /*Bitu*/int rate = Option.oplrate.intValue(SBlaster.OPL_RATE);
            //Make sure we can't select lower than 8000 to prevent fixed point issues
            if (rate < 8000)
                rate = 8000;
            String oplemu = Option.oplemu.value(SBlaster.OPLEMU);

            mixerChan = mixerObject.Install(OPL_CallBack, rate, "FM");
            mixerChan.SetScale(2.0f);
            if (oplemu.equals("fast")) {
                handler = new DbOPL.Handler();
            } else if (oplemu.equals("compat")) {
                System.out.println("OPLEMU compat not implemented");
                handler = new DbOPL.Handler();
            } else {
                handler = new DbOPL.Handler();
            }
            handler.Init(rate);
            switch (SBlaster.oplmode) {
            case 2:
                single = true;
                Init(MODE_OPL2);
                break;
            case 3:
                Init(MODE_DUALOPL2);
                single = false;
                break;
            case 4:
                Init(MODE_OPL3);
                single = false;
                break;
            default:
                single = false;
            }
        }

        //Mode we're running in
        private int mode;

        //Last selected address in the chip for the different modes
        private class Reg {
            /*Bit32u*/int normal;

            /*Bit8u*/short dual(int index) {
                if (index == 0)
                    return (short)(normal & 0xFF);
                else
                    return (short)(normal >> 8 & 0xFF);
            }

            /*Bit8u*/void dual(int index, int value) {
                if (index == 0) {
                    normal &= 0xFFFFFF00;
                    normal |= value & 0xFF;
                } else {
                    normal &= 0xFFFF00FF;
                    normal |= value << 8 & 0xFF;
                }
            }
        }

        private Reg reg = new Reg();

        private void CacheWrite( /*Bit32u*/int reg, /*Bit8u*/short val) {
            //Store it into the cache
            cache[reg] = val;
        }

        private void DualWrite( /*Bit8u*/short index, /*Bit8u*/short reg, /*Bit8u*/short val) {
            //Make sure you don't use opl3 features
            //Don't allow write to disable opl3
            if (reg == 5) {
                return;
            }
            //Only allow 4 waveforms
            if (reg >= 0xE0) {
                val &= 3;
            }
            //Write to the timer?
            if (chip[index].Write(reg, val))
                return;
            //Enabling panning
            if (reg >= 0xc0 && reg <= 0xc8) {
                val &= 0x0f;
                val |= index != 0 ? 0xA0 : 0x50;
            }
            /*Bit32u*/int fullReg = reg + (index != 0 ? 0x100 : 0);
            handler.WriteReg(fullReg, val);
            CacheWrite(fullReg, val);
        }

        public Mixer.MixerChannel mixerChan;
        public /*Bit32u*/long lastUsed; //Ticks when adlib was last used to turn of mixing after a few second

        public Handler handler; //Handler that will generate the sound
        public short[] cache = new short[512];
        public Chip[] chip = new Chip[2];

        //Handle port writes
        public void PortWrite(/*Bitu*/int port, /*Bitu*/short val) {
            //Keep track of last write time
            lastUsed = timeSource.getEmulatedMicros();
            //Maybe only enable with a keyon?
            if (!mixerChan.enabled) {
                mixerChan.Enable(true);
            }
            if ((port & 1) != 0) {
                switch (mode) {
                case MODE_OPL2:
                case MODE_OPL3:
                    if (!chip[0].Write(reg.normal, val)) {
                        handler.WriteReg(reg.normal, val);
                        CacheWrite(reg.normal, val);
                    }
                    break;
                case MODE_DUALOPL2:
                    //Not a 0x??8 port, then write to a specific port
                    if ((port & 0x8) == 0) {
                        /*Bit8u*/short index = (short)((port & 2) >> 1);
                        DualWrite(index, reg.dual(index), val);
                    } else {
                        //Write to both ports
                        DualWrite((short)0, reg.dual(0), val);
                        DualWrite((short)1, reg.dual(1), val);
                    }
                    break;
                }
            } else {
                //Ask the handler to write the address
                //Make sure to clip them in the right range
                switch (mode) {
                case MODE_OPL2:
                    reg.normal = (int)handler.WriteAddr(port, val) & 0xff;
                    break;
                case MODE_OPL3:
                    reg.normal = (int)handler.WriteAddr(port, val) & 0x1ff;
                    break;
                case MODE_DUALOPL2:
                    //Not a 0x?88 port, when write to a specific side
                    if ((port & 0x8) == 0) {
                        /*Bit8u*/int index = (port & 2) >> 1;
                        reg.dual(index, val & 0xff);
                    } else {
                        reg.dual(0, val & 0xff);
                        reg.dual(1, val & 0xff);
                    }
                    break;
                }
            }
        }

        public /*Bitu*/int PortRead(/*Bitu*/int port) {
            switch (mode) {
            case MODE_OPL2:
                //We allocated 4 ports, so just return -1 for the higher ones
                if ((port & 3) == 0) {
                    //Make sure the low /*Bits*/int are 6 on opl2
                    return chip[0].Read() | 0x6;
                } else {
                    return 0xff;
                }
            case MODE_OPL3:
                //We allocated 4 ports, so just return -1 for the higher ones
                if ((port & 3) == 0) {
                    return chip[0].Read();
                } else {
                    return 0xff;
                }
            case MODE_DUALOPL2:
                //Only return for the lower ports
                if ((port & 1) != 0) {
                    return 0xff;
                }
                //Make sure the low /*Bits*/int are 6 on opl2
                return chip[port >> 1 & 1].Read() | 0x6;
            }
            return 0;
        }

        public void Init(int m) {
            mode = m;
            switch (mode) {
            case MODE_OPL3:
            case MODE_OPL2:
                break;
            case MODE_DUALOPL2:
                //Setup opl3 mode in the hander
                handler.WriteReg(0x105, (short)1);
                //Also set it up in the cache so the capturing will start opl3
                CacheWrite(0x105, (short)1);
                break;
            }
        }
    }

    private static Module module = null;

    private final Mixer.MIXER_Handler OPL_CallBack = new Mixer.MIXER_Handler() {
        @Override
        public void call(/*Bitu*/int len) {
            module.handler.Generate(module.mixerChan, len);
            //Disable the sound generation after 30 seconds of silence
            if (timeSource.getEmulatedMicros() - module.lastUsed > 30000000) {
                /*Bitu*/int i;
                for (i = 0xb0; i < 0xb9; i++)
                    if ((module.cache[i] & 0x20) != 0 || (module.cache[i + 0x100] & 0x20) != 0)
                        break;
                if (i == 0xb9)
                    module.mixerChan.Enable(false);
                else
                    module.lastUsed = timeSource.getEmulatedMicros();
            }
        }
    };

    public Adlib() {
        // soundblaster must be instantiated first!
        module = new Module();
    }

    @Override
    public int[] ioPortsRequested() {
        int[] ports = new int[6 + (single ? 0 : 4)];
        int base = Option.sbbase.intValue(SBlaster.BASE, 16);
        int i = 0;
        for (; i < 4; i++)
            ports[i] = 0x388 + i;
        if (!single)
            for (int j = 0; j < 4; j++)
                ports[i++] = base + j;
        ports[i++] = base + 8;
        ports[i++] = base + 9;
        return ports;
    }

    @Override
    public void acceptComponent(HardwareComponent component) {
        if (component instanceof Clock && component.initialised())
            timeSource = (Clock)component;
        if (component instanceof IOPortHandler && component.initialised()) {
            ((IOPortHandler)component).registerIOPortCapable(this);
            ioportRegistered = true;
        }
    }

    @Override
    public boolean initialised() {
        return timeSource != null && ioportRegistered;
    }

    @Override
    public int ioPortRead8(int address) {
        return ioPortRead32(address);
    }

    @Override
    public int ioPortRead16(int address) {
        return ioPortRead32(address);
    }

    @Override
    public int ioPortRead32(int port) {
        return module.PortRead(port);
    }

    @Override
    public void ioPortWrite8(int address, int data) {
        ioPortWrite32(address, data);
    }

    @Override
    public void ioPortWrite16(int address, int data) {
        ioPortWrite32(address, data);
    }

    @Override
    public void ioPortWrite32(int port, int data) {
        module.PortWrite(port, (short)data);
    }
}
