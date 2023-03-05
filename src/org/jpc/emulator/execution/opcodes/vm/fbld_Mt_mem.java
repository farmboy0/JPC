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

package org.jpc.emulator.execution.opcodes.vm;

import org.jpc.assembly.PeekableInputStream;
import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.decoder.Modrm;
import org.jpc.emulator.execution.decoder.Pointer;
import org.jpc.emulator.processor.Processor;

public class fbld_Mt_mem extends Executable {
    final Pointer op1;

    public fbld_Mt_mem(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        int modrm = input.readU8();
        op1 = Modrm.getPointer(prefices, modrm, input);
    }

    @Override
    public Branch execute(Processor cpu) {
        byte[] data = op1.getF80(cpu);
        long n = 0;
        long decade = 1;
        for (int i = 0; i < 9; i++) {
            byte b = data[i];
            n += (b & 0xf) * decade;
            decade *= 10;
            n += ((b >> 4) & 0xf) * decade;
            decade *= 10;
        }
        byte sign = data[9];
        double m = n;
        if (sign < 0)
            m *= -1.0;
        cpu.fpu.push(m);
        return Branch.None;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public String toString() {
        return "fbld" + " " + "[" + op1.toString() + "]";
    }
}
