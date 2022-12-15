package org.jpc.emulator.execution.opcodes.rm;

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.decoder.Instruction;
import org.jpc.emulator.execution.decoder.Pointer;
import org.jpc.emulator.processor.Processor;
import org.jpc.emulator.processor.Processor.Reg;

public class btc_o32_Ed_Gd_mem extends Executable {
    final Pointer op1;
    final int op2Index;

    public btc_o32_Ed_Gd_mem(int blockStart, Instruction parent) {
        super(blockStart, parent);
        op1 = new Pointer(parent.operand[0], parent.adr_mode);
        op2Index = Processor.getRegIndex(parent.operand[1].toString());
    }

    @Override
    public Branch execute(Processor cpu) {
        Reg op2 = cpu.regs[op2Index];
        int bit = 1 << op2.get32();
        int offset = (op2.get32() & ~(32 - 1)) / 8;
        cpu.cf = 0 != (op1.get32(cpu, offset) & bit);
        cpu.flagStatus &= NCF;
        op1.set32(cpu, offset, op1.get32(cpu, offset) ^ bit);
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
