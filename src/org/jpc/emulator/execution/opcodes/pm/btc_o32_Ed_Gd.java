package org.jpc.emulator.execution.opcodes.pm;

import org.jpc.assembly.Instruction;
import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.processor.Processor;
import org.jpc.emulator.processor.Processor.Reg;

public class btc_o32_Ed_Gd extends Executable {
    final int op1Index;
    final int op2Index;

    public btc_o32_Ed_Gd(int blockStart, Instruction parent) {
        super(blockStart, parent);
        op1Index = Processor.getRegIndex(parent.operand[0].toString());
        op2Index = Processor.getRegIndex(parent.operand[1].toString());
    }

    @Override
    public Branch execute(Processor cpu) {
        Reg op1 = cpu.regs[op1Index];
        Reg op2 = cpu.regs[op2Index];
        int bit = 1 << op2.get32();
        cpu.cf = 0 != (op1.get32() & bit);
        cpu.flagStatus &= NCF;
        op1.set32(op1.get32() ^ bit);
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
