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

package org.jpc.emulator.execution.opcodes.vm;

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.UCodes;
import org.jpc.emulator.execution.decoder.Modrm;
import org.jpc.emulator.execution.decoder.PeekableInputStream;
import org.jpc.emulator.processor.Processor;

public class sub_AL_Ib extends Executable {
    final int immb;

    public sub_AL_Ib(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        immb = Modrm.Ib(input);
    }

    @Override
    public Branch execute(Processor cpu) {
        cpu.flagOp1 = (byte)cpu.r_al.get8();
        cpu.flagOp2 = (byte)immb;
        cpu.flagResult = (byte)(cpu.flagOp1 - cpu.flagOp2);
        cpu.r_al.set8((byte)cpu.flagResult);
        cpu.flagIns = UCodes.SUB8;
        cpu.flagStatus = OSZAPC;
        return Branch.None;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
