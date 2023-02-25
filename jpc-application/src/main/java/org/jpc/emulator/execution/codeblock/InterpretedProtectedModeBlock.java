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

package org.jpc.emulator.execution.codeblock;

import org.jpc.assembly.Instruction;
import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.Executable.Branch;
import org.jpc.emulator.execution.SelfModifyingCodeException;
import org.jpc.emulator.execution.decoder.BasicBlock;
import org.jpc.emulator.processor.ModeSwitchException;
import org.jpc.emulator.processor.Processor;
import org.jpc.emulator.processor.ProcessorException;

public class InterpretedProtectedModeBlock implements ProtectedModeCodeBlock {
    private final BasicBlock b;
    private boolean valid = true;

    public InterpretedProtectedModeBlock(BasicBlock b) {
        this.b = b;
    }

    @Override
    public int getX86Length() {
        return b.getX86Length();
    }

    @Override
    public int getX86Count() {
        return b.getX86Count();
    }

    @Override
    public Branch execute(Processor cpu) {
        Executable current = b.start;
        Executable.Branch ret;

        b.preBlock(cpu);
        try {
            while ((ret = current.execute(cpu)) == Executable.Branch.None) {
                b.postInstruction(cpu, current);
                if (!valid)
                    throw new SelfModifyingCodeException("Block modified itself!");
                current = current.next;
            }
            b.postInstruction(cpu, current);
            return ret;
        } catch (ProcessorException e) {
            cpu.eip += current.delta;
            if (current.isBranch()) // branches have already updated eip
                cpu.eip -= getX86Length(); // so eip points at the branch that barfed
            if (!e.pointsToSelf()) {
                if (current.isBranch())
                    cpu.eip += getX86Length() - current.delta;
                else
                    cpu.eip += current.next.delta - current.delta;
            }
            cpu.handleProtectedModeException(e);
            return Branch.Exception;
        } catch (ModeSwitchException e) {
            int count = 1;
            Executable p = b.start;
            while (p != current) {
                count++;
                p = p.next;
            }
            e.setX86Count(count);
            throw e;
        } catch (SelfModifyingCodeException e) {
            cpu.eip += current.next.delta;
            return Branch.Exception;
        } finally {
            b.postBlock(cpu);
        }
    }

    @Override
    public String getDisplayString() {
        return "Interpreted Protected Mode Block:\n" + b.getDisplayString();
    }

    @Override
    public Instruction getInstructions() {
        return b.getInstructions();
    }

    @Override
    public boolean handleMemoryRegionChange(int startAddress, int endAddress) {
        valid = b.handleMemoryRegionChange(startAddress, endAddress);
        return valid;
    }
}
