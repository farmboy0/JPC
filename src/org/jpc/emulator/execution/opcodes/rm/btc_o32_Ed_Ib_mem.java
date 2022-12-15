package org.jpc.emulator.execution.opcodes.rm;

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.decoder.Instruction;
import org.jpc.emulator.execution.decoder.Pointer;
import org.jpc.emulator.processor.Processor;

public class btc_o32_Ed_Ib_mem extends Executable {
    final Pointer op1;
    final int immb;

    public btc_o32_Ed_Ib_mem(int blockStart, Instruction parent) {
        super(blockStart, parent);
        op1 = new Pointer(parent.operand[0], parent.adr_mode);
        immb = (byte)parent.operand[1].lval;
    }

    @Override
    public Branch execute(Processor cpu) {
        int bit = 1 << immb;
        int offset = (immb & ~(32 - 1)) / 8;
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
