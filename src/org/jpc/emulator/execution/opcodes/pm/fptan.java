/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 3.0

    A project by Ian Preston, ianopolous AT gmail.com

    Copyright (C) 2012-2013 Ian Preston

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

    Details (including current contact information) can be found at:

    jpc.sourceforge.net
    or the developer website
    sourceforge.net/projects/jpc/

    End of licence header
*/

package org.jpc.emulator.execution.opcodes.pm;

import org.jpc.assembly.PeekableInputStream;
import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.processor.Processor;

public class fptan extends Executable {

    public fptan(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
    }

    @Override
    public Branch execute(Processor cpu) {
        double freg0 = cpu.fpu.ST(0);
        if ((freg0 > Math.pow(2.0, 63.0)) || (freg0 < -1.0 * Math.pow(2.0, 63.0))) {
            if (Double.isInfinite(freg0))
                cpu.fpu.setInvalidOperation();
            cpu.fpu.conditionCode |= 4;
        } else {
            cpu.fpu.conditionCode &= ~4;
            cpu.fpu.setST(0, Math.tan(freg0));
            cpu.fpu.push(1.0);
        }
        return Branch.None;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public String toString() {
        return "fptan";
    }
}
