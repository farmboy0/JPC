package org.jpc.emulator.execution.opcodes.vm;

import org.jpc.assembly.Disassembler;
import org.jpc.assembly.Instruction;
import org.jpc.assembly.PeekableInputStream;
import org.jpc.assembly.Prefices;
import org.jpc.emulator.execution.Executable;
import org.jpc.emulator.processor.Processor;
import org.jpc.emulator.processor.ProcessorException;

public class InvalidOpcode extends Executable {
    final int blockLength;
    final int instructionLength;
    String error;

    public InvalidOpcode(int blockStart, int eip, int prefices, PeekableInputStream input) {
        super(blockStart, eip);
        instructionLength = (int)input.getAddress() - eip;
        blockLength = (int)input.getAddress() - blockStart;
        input.seek(-instructionLength);
        Instruction in = Disassembler.disassemble(input, Prefices.isAddr16(prefices) ? 32 : 16);
        error = in.toString() + ", x86 byte = " + Disassembler.getRawBytes(input, 0);
    }

    @Override
    public Branch execute(Processor cpu) {
        if (true)
            throw ProcessorException.UNDEFINED;
        return Branch.Jmp_Unknown;
    }

    @Override
    public boolean isBranch() {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
