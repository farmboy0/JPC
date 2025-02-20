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

package org.jpc.emulator.motherboard;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jpc.emulator.HardwareComponent;

/**
 * This class provides a <code>Bios</code> implementation for the VGA Bios. The VGA Bios is loaded
 * at address <code>0xc0000</code>.
 * <p>
 * IO ports <code>0x500-0x503</code> are registered for debugging output. Byte writes cause ASCII
 * characters to be written to standard output, and word writes indicate a BIOS panic at the written
 * value line number.
 * @author Chris Dennis
 */
public class VGABIOS extends Bios implements IODevice {
    private static final Logger LOGGING = Logger.getLogger(VGABIOS.class.getName());

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    private boolean ioportRegistered;

    /**
     * Loads the vga bios from the resource <code>image</code>.
     * @param image vga bios resource name.
     * @throws java.io.IOException propogated from the resource load.
     * @throws java.util.MissingResourceException propogated from the resource load
     */
    public VGABIOS(String image) throws IOException {
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
        return new int[] { 0x500, 0x501, 0x502, 0x503 };
    }

    @Override
    public void ioPortWrite8(int address, int data) {
        switch (address) {
        /* LGPL VGA-BIOS Messages */
        case 0x500:
        case 0x503:
            print(new String(new byte[] { (byte)data }, US_ASCII));
            break;
        default:
        }
    }

    @Override
    public void ioPortWrite16(int address, int data) {
        switch (address) {
        /* Bochs BIOS Messages */
        case 0x501:
        case 0x502:
            LOGGING.log(Level.SEVERE, "panic in vgabios at line {0,number,integer}", Integer.valueOf(data));
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
        return 0xc0000;
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
