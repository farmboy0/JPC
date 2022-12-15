/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine

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
 
    Details (including contact information) can be found at: 

    jpc.sourceforge.net
    or the developer website
    sourceforge.net/projects/jpc/

    End of licence header
*/

package org.jpc.emulator.execution.opcodes.rm;

import org.jpc.emulator.execution.*;
import org.jpc.emulator.execution.decoder.*;
import org.jpc.emulator.processor.*;
import org.jpc.emulator.processor.fpu64.*;
import static org.jpc.emulator.processor.Processor.*;

public class sar_Eb_I1_mem extends Executable {
    final Pointer op1;

    public sar_Eb_I1_mem(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        int modrm = input.readU8();
        op1 = Modrm.getPointer(prefices, modrm, input);
    }

    public Branch execute(Processor cpu) {
        if (1 != 0) {
            cpu.flagOp1 = op1.get8(cpu);
            cpu.flagOp2 = 1;
            int res = (byte)(cpu.flagOp1 >> cpu.flagOp2);
            op1.set8(cpu, (byte)res);
            cpu.setOSZAPC_Logic8(res);
            cpu.flagStatus |= CF;
            cpu.flagIns = UCodes.SAR8;
        }
        return Branch.None;
    }

    public boolean isBranch() {
        return false;
    }

    public String toString() {
        return this.getClass().getName();
    }
}
