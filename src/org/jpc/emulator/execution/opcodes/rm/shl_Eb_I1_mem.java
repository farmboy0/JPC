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

package org.jpc.emulator.execution.opcodes.rm;

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.UCodes;
import org.jpc.emulator.execution.decoder.Modrm;
import org.jpc.emulator.execution.decoder.PeekableInputStream;
import org.jpc.emulator.execution.decoder.Pointer;
import org.jpc.emulator.processor.Processor;

public class shl_Eb_I1_mem extends Executable {
    final Pointer op1;

    public shl_Eb_I1_mem(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        int modrm = input.readU8();
        op1 = Modrm.getPointer(prefices, modrm, input);
    }

    @Override
    public Branch execute(Processor cpu) {
        int shift = 1 & 0x1f;
        if (shift != 0) {
            if (shift <= 16) {
                cpu.flagStatus = OSZPC;
            } else {
                cpu.flagStatus = SZP;
                cpu.of = false;
                cpu.cf = false;
            }
            cpu.af = false;
            cpu.flagOp1 = op1.get8(cpu);
            cpu.flagOp2 = shift;
            cpu.flagResult = (byte)(cpu.flagOp1 << cpu.flagOp2);
            op1.set8(cpu, (byte)cpu.flagResult);
            cpu.flagIns = UCodes.SHL8;
        }
        return Branch.None;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public String toString() {
        return "shl" + " " + "[" + op1.toString() + "]" + ", " + "0x1";
    }
}
