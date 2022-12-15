package org.jpc.emulator.execution.opcodes.rm;

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.decoder.Instruction;
import org.jpc.emulator.processor.Processor;
import org.jpc.emulator.processor.Processor.Reg;

public class btc_o32_Ed_Ib extends Executable {
    final int op1Index;
    final int immb;

    public btc_o32_Ed_Ib(int blockStart, Instruction parent) {
        super(blockStart, parent);
        op1Index = Processor.getRegIndex(parent.operand[0].toString());
        immb = (byte)parent.operand[1].lval;
    }

    @Override
    public Branch execute(Processor cpu) {
        Reg op1 = cpu.regs[op1Index];
        int bit = 1 << immb;
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
