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

public class mov_S_Ew extends Executable {
    public final int segIndex;
    final int op2Index;

    public mov_S_Ew(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        int modrm = input.readU8();
        segIndex = Modrm.reg(modrm);
        op2Index = Modrm.Ew(modrm);
    }

    public Branch execute(Processor cpu) {
        Reg op2 = cpu.regs[op2Index];
        if (segIndex == 1)
            throw ProcessorException.UNDEFINED;
        cpu.setSeg(segIndex, (short)op2.get16());
        return Branch.None;
    }

    public boolean isBranch() {
        return false;
    }

    public String toString() {
        return this.getClass().getName();
    }
}
