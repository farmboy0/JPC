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

package tools.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jpc.assembly.Disassembler;
import org.jpc.assembly.Instruction;
import org.jpc.assembly.Prefices;

public class DecoderGenerator {
    private static final List<String> IMMEDIATES = Arrays.asList("Jb", "Jw", "Jd", "Ib", "Iw", "Id");
    private static final byte[] EMPTY = new byte[28];

    private static final Set<String> RM_OPS = new HashSet<String>();
    private static final Set<String> PM_OPS = new HashSet<String>();
    private static final Set<String> VM_OPS = new HashSet<String>();

    public static void main(String[] args) throws Exception {
        OpcodesCollector occ = new OpcodesCollector();
        OpcodeParserHandler.parseUsing(occ);
        String dir = "src";
        if (args.length == 1 && args[0] != null)
            dir = args[0];
        File fDir = new File(dir);
        fDir.mkdirs();
        DecoderGenerator.generate(fDir);
    }

    public static String getExecutableName(int mode, Instruction in) {
        Set<String> instructions;
        String prefix;
        switch (mode) {
        case 1:
            instructions = RM_OPS;
            prefix = "org.jpc.emulator.execution.opcodes.rm.";
            break;
        case 2:
            instructions = PM_OPS;
            prefix = "org.jpc.emulator.execution.opcodes.pm.";
            break;
        case 3:
            instructions = VM_OPS;
            prefix = "org.jpc.emulator.execution.opcodes.vm.";
            break;
        default:
            throw new IllegalStateException("Unknown mode: " + mode);
        }
        String gen;
        try {
            gen = in.getGeneralClassName(false, false);
        } catch (IllegalStateException e) {
            return prefix + "InvalidOpcode/*(DecoderGenerator.java line 80)*/";
        }

        if (instructions.contains(gen))
            return prefix + gen;

        if (instructions.contains(in.getGeneralClassName(true, false)))
            return prefix + in.getGeneralClassName(true, false);
        if (instructions.contains(in.getGeneralClassName(false, true)))
            return prefix + in.getGeneralClassName(false, true);
        if (instructions.contains(in.getGeneralClassName(true, true)))
            return prefix + in.getGeneralClassName(true, true);
        if (gen.equals("invalid"))
            return prefix + "InvalidOpcode";
        return prefix + "UnimplementedOpcode";
    }

    public static void generate(File dir) throws IOException {
        final File target = new File(dir, "org/jpc/emulator/execution/opcodes/ExecutableTables.java");
        target.getParentFile().mkdirs();
        final BufferedWriter w = new BufferedWriter(new FileWriter(target));
        try {
            w.write(GeneratorHelper.readLicenseHeader());
            w.write("package org.jpc.emulator.execution.opcodes;\n\n");
            w.write("import org.jpc.assembly.PeekableInputStream;\n");
            w.write("import org.jpc.assembly.Prefices;\n");
            w.write("import org.jpc.emulator.execution.Executable;\n");
            w.write("import org.jpc.emulator.execution.decoder.Modrm;\n");
            w.write("import org.jpc.emulator.execution.decoder.OpcodeDecoder;\n\n");
            w.write("public class ExecutableTables {\n");

            generateMode(w, 1, "RM");
            generateMode(w, 2, "PM");
            generateMode(w, 3, "VM");

            w.write("}\n");

            w.flush();
        } finally {
            w.close();
        }
    }

    private static void generateMode(Writer w, int modeType, String mode) throws IOException {
        final OpcodeHolder[] ops = new OpcodeHolder[0x800];
        for (int i = 0; i < ops.length; i++) {
            ops[i] = new OpcodeHolder(modeType);
        }
        generateRep(16, ops);
        removeDuplicates(ops);

        w.write("    public static void populate" + mode + "Opcodes(OpcodeDecoder[] ops) {\n");
        for (int i = 0; i < ops.length; i++) {
            w.write(String.format("        ops[0x%02x] = %s\n", i, ops[i]));
        }
        w.write("    }\n\n");
    }

    private static void generateRep(int mode, OpcodeHolder[] ops) {
        byte[] x86 = new byte[28];
        generateAll(mode, x86, 0, ops);
        x86[0] = (byte)0xF2;
        generateAll(mode, x86, 1, ops);
        x86[0] = (byte)0xF3;
        generateAll(mode, x86, 1, ops);
    }

    private static void generateAll(int mode, byte[] x86, int opbyte, OpcodeHolder[] ops) {
        Disassembler.ByteArrayPeekStream input = new Disassembler.ByteArrayPeekStream(x86);

        int originalOpbyte = opbyte;
        int base = 0;
        for (int k = 0; k < 2; k++) // addr
        {
            for (int j = 0; j < 2; j++) // op size
            {
                for (int i = 0; i < 2; i++) // 0F opcode start
                {
                    for (int opcode = 0; opcode < 256; opcode++) {
                        if (Prefices.isPrefix(opcode) || (opcode == 0x0f && (base & 0x100) == 0))
                            continue;
                        // fill x86 with appropriate bytes
                        x86[opbyte] = (byte)opcode;
                        input.resetCounter();

                        // decode prefices
                        Instruction in = new Instruction();
                        Disassembler.get_prefixes(mode, input, in);
                        int preficesLength = input.getCounter();

                        int opcodeLength;
                        try {
                            // decode opcode part
                            Disassembler.search_table(mode, input, in);
                            Disassembler.do_mode(mode, in);
                            opcodeLength = input.getCounter() - preficesLength;

                            // decode operands
                            Disassembler.disasm_operands(mode, input, in);
                            Disassembler.resolve_operator(mode, input, in);
                        } catch (IllegalStateException s) {
                            continue;
                        }
                        int argumentsLength = input.getCounter() - opcodeLength - preficesLength;
                        String[] args = in.getArgsTypes();
                        if (args.length == 1 && IMMEDIATES.contains(args[0])) {
                            // don't enumerate immediates
                            ops[base + opcode].addOpcode(in, x86.clone());
                        } else {
                            // enumerate modrm
                            for (int modrm = 0; modrm < 256; modrm++) {
                                input.resetCounter();
                                x86[opbyte + 1] = (byte)modrm;
                                Instruction modin = new Instruction();
                                try {
                                    Disassembler.get_prefixes(mode, input, modin);
                                    Disassembler.search_table(mode, input, modin);
                                    Disassembler.do_mode(mode, modin);
                                    Disassembler.disasm_operands(mode, input, modin);
                                    Disassembler.resolve_operator(mode, input, modin);
                                } catch (IllegalStateException s) {
                                    // add the illegals
                                    ops[base + opcode].addOpcode(modin, x86.clone());
                                    x86[opbyte + 1] = 0;
                                    continue;
                                }
                                ops[base + opcode].addOpcode(modin, x86.clone());
                            }
                            x86[opbyte + 1] = 0;
                        }
                    }
                    System.arraycopy(EMPTY, opbyte, x86, opbyte, x86.length - opbyte);
                    x86[opbyte++] = 0x0f;
                    base += 0x100; // now do the 0x0f opcodes (2 byte opcodes)
                }

                if (x86[originalOpbyte] == (byte)0x67)
                    opbyte = originalOpbyte + 1;
                else
                    opbyte = originalOpbyte;
                System.arraycopy(EMPTY, opbyte, x86, opbyte, x86.length - opbyte);
                x86[opbyte++] = 0x66;
            }
            System.arraycopy(EMPTY, originalOpbyte, x86, originalOpbyte, x86.length - originalOpbyte);
            x86[originalOpbyte] = 0x67;
            opbyte = originalOpbyte + 1;
        }
    }

    private static void removeDuplicates(OpcodeHolder[] ops) {
        for (int i = 0x200; i < 0x800; i++)
            if (ops[i].equals(ops[i % 0x200]))
                ops[i].setDuplicateOf(i % 0x200);
    }

    private static class OpcodesCollector implements Callable {
        @Override
        public void call(Opcode op, String mode) {
            if ("rm".equals(mode)) {
                RM_OPS.add(op.getName());
            } else if ("pm".equals(mode)) {
                PM_OPS.add(op.getName());
            } else if ("vm".equals(mode)) {
                VM_OPS.add(op.getName());
            } else {
                throw new IllegalArgumentException("Unknown mode: " + mode);
            }
        }
    }
}
