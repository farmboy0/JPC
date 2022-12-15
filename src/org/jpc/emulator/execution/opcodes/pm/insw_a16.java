package org.jpc.emulator.execution.opcodes.pm;

import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.execution.StaticOpcodes;
import org.jpc.emulator.execution.decoder.Instruction;
import org.jpc.emulator.processor.Processor;

public class insw_a16 extends Executable {

    public insw_a16(int blockStart, Instruction parent) {
        super(blockStart, parent);
    }

    @Override
    public Branch execute(Processor cpu) {
        StaticOpcodes.insw_a16(cpu, cpu.es);
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
