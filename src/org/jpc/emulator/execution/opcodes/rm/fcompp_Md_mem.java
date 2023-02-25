package org.jpc.emulator.execution.opcodes.rm;

import org.jpc.assembly.Instruction;
import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.decoder.Pointer;
import org.jpc.emulator.processor.Processor;

public class fcompp_Md_mem extends Executable {
    final Pointer op1;

    public fcompp_Md_mem(int blockStart, Instruction parent) {
        super(blockStart, parent);
        op1 = new Pointer(parent.operand[0], parent.adr_mode);
    }

    @Override
    public Branch execute(Processor cpu) {
        int newcode = 0xd;
        double freg0 = cpu.fpu.ST(0);
        double freg1 = op1.getF32(cpu);
        if (Double.isNaN(freg0) || Double.isNaN(freg1))
            cpu.fpu.setInvalidOperation();
        else {
            if (freg0 > freg1)
                newcode = 0;
            else if (freg0 < freg1)
                newcode = 1;
            else
                newcode = 8;
        }
        cpu.fpu.conditionCode &= 2;
        cpu.fpu.conditionCode |= newcode;
        cpu.fpu.pop();
        cpu.fpu.pop();
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
