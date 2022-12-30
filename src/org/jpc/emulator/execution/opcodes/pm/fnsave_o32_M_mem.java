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

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.decoder.Modrm;
import org.jpc.emulator.execution.decoder.PeekableInputStream;
import org.jpc.emulator.execution.decoder.Pointer;
import org.jpc.emulator.processor.Processor;

public class fnsave_o32_M_mem extends Executable {
    final Pointer op1;

    public fnsave_o32_M_mem(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        int modrm = input.readU8();
        op1 = Modrm.getPointer(prefices, modrm, input);
    }

    @Override
    public Branch execute(Processor cpu) {
        System.out.println("Warning: Using incomplete opcode: FNSAVE_108");
        int addr = op1.get(cpu);
        cpu.linearMemory.setDoubleWord(addr, cpu.fpu.getControl());
        cpu.linearMemory.setDoubleWord(addr + 4, cpu.fpu.getStatus());
        cpu.linearMemory.setDoubleWord(addr + 8, cpu.fpu.getTagWord());
        cpu.linearMemory.setDoubleWord(addr + 12, 0 /* cpu.fpu.getIP()  offset*/);
        cpu.linearMemory.setDoubleWord(addr + 16, 0 /* (selector & 0xFFFF)*/);
        cpu.linearMemory.setDoubleWord(addr + 20, 0 /* operand pntr offset*/);
        cpu.linearMemory.setDoubleWord(addr + 24, 0 /* operand pntr selector & 0xFFFF*/);
        return Branch.None;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public String toString() {
        return "fnsave_o32" + " " + "[" + op1.toString() + "]";
    }
}
