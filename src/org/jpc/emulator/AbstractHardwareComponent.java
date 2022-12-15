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

package org.jpc.emulator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This class provides default implementations of the <code>HardwareComponent</code> methods. The
 * default implementations are all empty.
 * @author Chris Dennis
 */
public abstract class AbstractHardwareComponent implements HardwareComponent {
    @Override
    public boolean initialised() {
        return true;
    }

    @Override
    public void acceptComponent(HardwareComponent component) {
    }

    @Override
    public boolean updated() {
        return true;
    }

    @Override
    public void updateComponent(HardwareComponent component) {
    }

    @Override
    public void reset() {
    }

    @Override
    public void saveState(DataOutput output) throws IOException {
    }

    @Override
    public void loadState(DataInput input) throws IOException {
    }
}
