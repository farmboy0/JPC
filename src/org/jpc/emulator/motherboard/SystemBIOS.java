package org.jpc.emulator.motherboard;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jpc.emulator.HardwareComponent;

/**
 * This class provides a <code>Bios</code> implementation for the emulated machines system bios. The
 * system bios is loaded so that it runs up to address 0x100000 (1M).
 * <p>
 * IO ports <code>0x400-0x403</code> are registered for debugging output. Byte writes cause ASCII
 * characters to be written to standard output, and word writes indicate a BIOS panic at the written
 * value line number.
 * <p>
 * IO port <code>0x8900</code> is registered for system shutdown requests. Currently this triggers a
 * debugging output, but does not actually shutdown the machine.
 * @author Chris Dennis
 */
public class SystemBIOS extends Bios implements IODevice {
    private static final Logger LOGGING = Logger.getLogger(SystemBIOS.class.getName());

    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private boolean ioportRegistered;

    public SystemBIOS(String image) throws IOException {
        super(image);
        ioportRegistered = false;

    }

    @Override
    public void loadState(DataInput input) throws IOException {
        super.loadState(input);
        ioportRegistered = false;
    }

    @Override
    public int[] ioPortsRequested() {
        return new int[] { 0x400, 0x401, 0x402, 0x403, 0x8900 };
    }

    @Override
    public void ioPortWrite8(int address, int data) {
        switch (address) {
        /* Bochs BIOS Messages */
        case 0x402:
        case 0x403:
            print(new String(new byte[] { (byte)data }, US_ASCII));
            break;
        case 0x8900:
            LOGGING.log(Level.INFO, "attempted shutdown");
            break;
        default:
        }
    }

    @Override
    public void ioPortWrite16(int address, int data) {
        switch (address) {
        /* Bochs BIOS Messages */
        case 0x400:
        case 0x401:
            LOGGING.log(Level.SEVERE, "panic in rombios.c at line {0,number,integer}", Integer.valueOf(data));
        }
    }

    @Override
    public int ioPortRead8(int address) {
        return 0xff;
    }

    @Override
    public int ioPortRead16(int address) {
        return 0xffff;
    }

    @Override
    public int ioPortRead32(int address) {
        return 0xffffffff;
    }

    @Override
    public void ioPortWrite32(int address, int data) {
    }

    @Override
    protected int loadAddress() {
        return 0x100000 - length();
    }

    @Override
    public boolean updated() {
        return super.updated() && ioportRegistered;
    }

    @Override
    public void updateComponent(HardwareComponent component) {
        super.updateComponent(component);

        if (component instanceof IOPortHandler && component.updated()) {
            ((IOPortHandler)component).registerIOPortCapable(this);
            ioportRegistered = true;
        }
    }

    @Override
    public boolean initialised() {
        return super.initialised() && ioportRegistered;
    }

    @Override
    public void acceptComponent(HardwareComponent component) {
        super.acceptComponent(component);

        if (component instanceof IOPortHandler && component.initialised()) {
            ((IOPortHandler)component).registerIOPortCapable(this);
            ioportRegistered = true;
        }
    }

    @Override
    public void reset() {
        super.reset();
        ioportRegistered = false;
    }
}
