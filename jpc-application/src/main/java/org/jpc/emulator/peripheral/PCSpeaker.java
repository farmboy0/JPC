/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.4

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007-2010 The University of Oxford

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Details (including contact information) can be found at:

    jpc.sourceforge.net
    or the developer website
    sourceforge.net/projects/jpc/

    Conceived and Developed by:
    Rhys Newman, Ian Preston, Chris Dennis

    End of licence header
*/

package org.jpc.emulator.peripheral;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import org.jpc.emulator.AbstractHardwareComponent;
import org.jpc.emulator.HardwareComponent;
import org.jpc.emulator.motherboard.IODevice;
import org.jpc.emulator.motherboard.IOPortHandler;
import org.jpc.emulator.motherboard.IntervalTimer;

/**
 * @author Ian Preston
 */
public class PCSpeaker extends AbstractHardwareComponent implements IODevice {
    private static final Logger LOGGING = Logger.getLogger(PCSpeaker.class.getName());

    private static final int SPEAKER_SAMPLE_RATE = 22050;
    private static final int SPEAKER_MAX_FREQ = SPEAKER_SAMPLE_RATE >> 1;
    private static final int SPEAKER_MIN_FREQ = 10;
    private static final int SPEAKER_VOLUME = 16000;
    private static final int SPEAKER_OFF = 0, SPEAKER_ON = 2, SPEAKER_PIT_ON = 3, SPEAKER_PIT_OFF = 1;

    private int dummyRefreshClock, speakerOn, lastNote, currentNote, velocity = 90, waitingForPit;
    private IntervalTimer pit;
    private boolean enabled = false, ioportRegistered;
    private Synthesizer synthesizer;
    private Receiver receiver;
    private ShortMessage message = new ShortMessage();
    private Instrument[] instruments;
    private MidiChannel cc; // current channel

    public int mode;

    public PCSpeaker() {
        ioportRegistered = false;
        if (enabled) {
            configure();
        }
    }

    public void enable(boolean value) {
        if (!value) {
            enabled = false;
        } else {
            enabled = true;
            configure();
        }
    }

    private void configure() {
        try {
            if (synthesizer == null) {
                if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
                    LOGGING.log(Level.INFO, "couldn't get MIDI synthesizer failed");
                    enabled = false;
                    return;
                }
            }
            synthesizer.open();
            receiver = synthesizer.getReceiver();
        } catch (MidiUnavailableException e) {
            LOGGING.log(Level.INFO, "pc speaker disabled", e);
            enabled = false;
            return;
        } catch (Exception e) {
            LOGGING.log(Level.INFO, "pc speaker disabled", e);
            enabled = false;
            return;
        }

        Soundbank sb = synthesizer.getDefaultSoundbank();
        if (sb == null) {
            System.out.println("Warning: loading remote soundbank.");
            try {
                sb = MidiSystem.getSoundbank(new URI("http://www.classicdosgames.com/soundbank.gm").toURL());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (sb != null) {
            instruments = sb.getInstruments();
            synthesizer.loadInstrument(instruments[0]);
        }
        MidiChannel[] channels = synthesizer.getChannels();
        cc = channels[0];
        programChange(80); //80 = load square wave instrument
    }

    private int getNote() {
        double freq = IntervalTimer.PIT_FREQ / pit.getInitialCount(2); //actual frequency in Hz
        if (freq > SPEAKER_MAX_FREQ)
            freq = SPEAKER_MAX_FREQ;
        if (freq < SPEAKER_MIN_FREQ)
            freq = SPEAKER_MIN_FREQ;
        return frequencyToNote(freq);
    }

    public static int frequencyToNote(double f) {
        double ans = 12 * (Math.log(f) - Math.log(440)) / Math.log(2);
        return (int)ans + 69;
    }

    private void playNote(int note) {
        try {
            message.setMessage(ShortMessage.NOTE_ON, 0, note, velocity);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        receiver.send(message, -1);
    }

    private void stopNote(int note) {
        try {
            message.setMessage(ShortMessage.NOTE_OFF, 0, note, velocity);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        receiver.send(message, -1);
    }

    public synchronized void play() {
        waitingForPit++;
        if (enabled && waitingForPit == 2) {
            if (pit.getMode(2) != 3)
                return;

            lastNote = currentNote;
            currentNote = getNote();

            stopNote(lastNote);
            playNote(currentNote);
        }
    }

    private void programChange(int program) {
        if (instruments != null) {
            synthesizer.loadInstrument(instruments[program]);
        }
        cc.programChange(program);
    }

    @Override
    public void saveState(DataOutput output) throws IOException {
        output.writeInt(dummyRefreshClock);
        output.writeInt(speakerOn);
    }

    @Override
    public void loadState(DataInput input) throws IOException {
        ioportRegistered = false;
        dummyRefreshClock = input.readInt();
        speakerOn = input.readInt();
    }

    @Override
    public int[] ioPortsRequested() {
        return new int[] { 0x61 };
    }

    @Override
    public int ioPortRead8(int address) {
        int out = pit.getOut(2);
        dummyRefreshClock ^= 1;
        return speakerOn << 1 | (pit.getGate(2) ? 1 : 0) | out << 5 | dummyRefreshClock << 4;
    }

    @Override
    public int ioPortRead16(int address) {
        return 0xff & ioPortRead8(address) | 0xff00 & ioPortRead8(address + 1) << 8;
    }

    @Override
    public int ioPortRead32(int address) {
        return 0xffff & ioPortRead16(address) | 0xffff0000 & ioPortRead16(address + 2) << 16;
    }

    @Override
    public synchronized void ioPortWrite8(int address, int data) {
        if (!enabled)
            return;
        speakerOn = data >> 1 & 1;
        pit.setGate(2, (data & 1) != 0);
        if ((data & 1) == 1) {
            if (speakerOn == 1) {
                //connect speaker to PIT
                mode = SPEAKER_PIT_ON;
                waitingForPit = 0;
            } else {
                //leave speaker disconnected from following PIT
                mode = SPEAKER_PIT_OFF;
                stopNote(currentNote);
            }
        } else {
            // zero bit is 0, speaker follows bit 1
            mode = SPEAKER_OFF;
            stopNote(currentNote);
            if (speakerOn != 0)
                LOGGING.log(Level.INFO, "manual speaker management not implemented");
        }
    }

    @Override
    public void ioPortWrite16(int address, int data) {
        this.ioPortWrite8(address, data);
        this.ioPortWrite8(address + 1, data >> 8);
    }

    @Override
    public void ioPortWrite32(int address, int data) {
        this.ioPortWrite16(address, data);
        this.ioPortWrite16(address + 2, data >> 16);
    }

    @Override
    public boolean initialised() {
        return ioportRegistered && pit != null;
    }

    @Override
    public void reset() {
        pit = null;
        ioportRegistered = false;
    }

    @Override
    public boolean updated() {
        return ioportRegistered && pit.updated();
    }

    @Override
    public void updateComponent(HardwareComponent component) {
        if (component instanceof IOPortHandler && component.updated()) {
            ((IOPortHandler)component).registerIOPortCapable(this);
            ioportRegistered = true;
        }
    }

    @Override
    public void acceptComponent(HardwareComponent component) {
        if (component instanceof IntervalTimer && component.initialised()) {
            pit = (IntervalTimer)component;
        }
        if (component instanceof IOPortHandler && component.initialised()) {
            ((IOPortHandler)component).registerIOPortCapable(this);
            ioportRegistered = true;
        }
    }
}
