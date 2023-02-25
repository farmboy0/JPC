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
import org.jpc.emulator.execution.decoder.Modrm;
import org.jpc.emulator.processor.Processor;

public class je_Jw extends Executable {
    final int jmp;
    final int blockLength;
    final int instructionLength;

    public je_Jw(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        jmp = Modrm.Jw(input);
        instructionLength = (int)input.getAddress() - eip;
        blockLength = eip - blockStart + instructionLength;
    }

    @Override
    public Branch execute(Processor cpu) {
        if (cpu.zf()) {
            int tmpEip = cpu.eip + jmp + blockLength;
            cpu.cs.checkAddress(tmpEip);
            cpu.eip = tmpEip;
            return Branch.T1;
        } else {
            cpu.eip += blockLength;
            return Branch.T2;
        }
    }

    @Override
    public boolean isBranch() {
        return true;
    }

    @Override
    public String toString() {
        return "je" + " " + jmp;
    }
}
