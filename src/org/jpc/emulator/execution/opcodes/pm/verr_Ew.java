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

import static org.jpc.emulator.processor.Processor.getRegString;

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.decoder.Modrm;
import org.jpc.emulator.execution.decoder.PeekableInputStream;
import org.jpc.emulator.processor.Processor;
import org.jpc.emulator.processor.Processor.Reg;
import org.jpc.emulator.processor.ProcessorException;
import org.jpc.emulator.processor.ProtectedModeSegment;
import org.jpc.emulator.processor.Segment;

public class verr_Ew extends Executable {
    final int op1Index;

    public verr_Ew(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        int modrm = input.readU8();
        op1Index = Modrm.Ew(modrm);
    }

    @Override
    public Branch execute(Processor cpu) {
        Reg op1 = cpu.regs[op1Index];
        try {
            Segment test = cpu.getSegment(op1.get16() & 0xffff);
            int type = test.getType();
            if ((type & ProtectedModeSegment.DESCRIPTOR_TYPE_CODE_DATA) == 0 || (type & ProtectedModeSegment.TYPE_CODE_CONFORMING) == 0
                && (cpu.getCPL() > test.getDPL() || test.getRPL() > test.getDPL()))
                cpu.zf(false);
            else
                cpu.zf((type & ProtectedModeSegment.TYPE_CODE) == 0 && (type & ProtectedModeSegment.TYPE_CODE_READABLE) != 0);
        } catch (ProcessorException e) {
            cpu.zf(false);
        }
        return Branch.None;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public String toString() {
        return "verr" + " " + getRegString(op1Index);
    }
}
