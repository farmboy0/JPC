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

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.UCodes;
import org.jpc.emulator.execution.decoder.PeekableInputStream;
import org.jpc.emulator.processor.Processor;

public class inc_o16_eBP extends Executable {

    public inc_o16_eBP(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
    }

    @Override
    public Branch execute(Processor cpu) {
        cpu.cf = Processor.getCarryFlag(cpu.flagStatus, cpu.cf, cpu.flagOp1, cpu.flagOp2, cpu.flagResult, cpu.flagIns);
        cpu.flagOp1 = cpu.r_ebp.get16();
        cpu.flagOp2 = 1;
        cpu.flagResult = (short)(cpu.flagOp1 + 1);
        cpu.r_ebp.set16((short)cpu.flagResult);
        cpu.flagIns = UCodes.ADD16;
        cpu.flagStatus = NCF;
        return Branch.None;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public String toString() {
        return "inc_o16" + " " + "eBP";
    }
}
