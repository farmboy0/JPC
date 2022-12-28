package tools.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class OpcodeWriter implements Callable {
    private static final boolean DEBUG_SIZE = true;

    private final String licenseHeader;

    OpcodeWriter() throws IOException {
        StringBuilder header = new StringBuilder();
        BufferedReader r = new BufferedReader(new FileReader("LicenseHeader"));
        String line;
        try {
            while ((line = r.readLine()) != null) {
                header.append(line);
                header.append("\n");
            }
        } finally {
            r.close();
        }
        header.append("\n");
        licenseHeader = header.toString();
    }

    @Override
    public void call(Opcode op, String mode) {
        System.out.println(op.getName());
        writeToFile(op, mode);
    }

    private void writeToFile(Opcode op, String mode) {
        try {
            BufferedWriter w = new BufferedWriter(
                new FileWriter("src/org/jpc/emulator/execution/opcodes/" + mode + "/" + op.getName() + ".java"));
            w.write(licenseHeader);
            w.write(getSource(op, mode));
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSource(Opcode op, String mode) {
        StringBuilder b = new StringBuilder();
        b.append(getPreamble(op, mode));
        if (op.isNeedsSegment())
            b.append("    final int segIndex;\n");
        for (int i = 0; i < op.getOperands().length; i++)
            b.append(op.getOperands()[i].define(i + 1));
        if (op.isBranch()) {
            b.append("    final int blockLength;\n");
            b.append("    final int instructionLength;\n");
        }
        if (op.isMultiSize())
            b.append("    final int size;\n");
        b.append(getDirectConstructor(op));
        b.append(getExecute(op));
        b.append(getBranch(op));
        b.append(getToString(op));
        b.append("}");
        return b.toString();
    }

    private static String getPreamble(Opcode op, String mode) {
        return "package org.jpc.emulator.execution.opcodes." + mode
            + ";\n\nimport org.jpc.emulator.execution.*;\nimport org.jpc.emulator.execution.decoder.*;\nimport org.jpc.emulator.processor.*;\nimport org.jpc.emulator.processor.fpu64.*;\nimport static org.jpc.emulator.processor.Processor.*;\n\npublic class "
            + op.getName() + " extends Executable\n{\n";
    }

    private static String getExecute(Opcode op) {
        StringBuilder b = new StringBuilder();
        b.append("    public Branch execute(Processor cpu)\n    {\n");
        for (int i = 0; i < op.getOperands().length; i++) {
            String load = op.getOperands()[i].load(i + 1);
            if (load.length() > 0)
                b.append(load + "\n");
        }
        if (op.isNeedsSegment())
            b.append("        Segment seg = cpu.segs[segIndex];\n");
        if (op.isMultiSize()) {
            b.append("        if (size == 16)\n        {\n");
            b.append(processSnippet(op.getName(), op.getOperands(), op.getSnippet(), 16));
            b.append("\n        }\n        else if (size == 32)\n        {\n");
            Operand[] op32 = new Operand[op.getSize()];
            for (int i = 0; i < op.getOperands().length; i++)
                op32[i] = Operand.get(op.getOperands()[i].toString(), 32, op.isMem());
            b.append(processSnippet(op.getName(), op32, op.getSnippet(), 32));
            b.append("\n        }");
            if (DEBUG_SIZE) {
                b.append("        else throw new IllegalStateException(\"Unknown size \"+size);");
            }
        } else
            b.append(processSnippet(op.getName(), op.getOperands(), op.getSnippet(), op.getSize()));
        if (op.getRet().trim().length() > 0)
            b.append("\n        return " + op.getRet() + ";");
        b.append("\n    }\n\n");
        return b.toString();
    }

    private static String processSnippet(String name, Operand[] operands, String snippet, int size) {
        String body = snippet;
        if (operands.length > 0) {
            body = replacePlaceHolder(body, "F", operands[0], 1);
            body = replacePlaceHolder(body, "A", operands[0], 1);
            body = replacePlaceHolder(body, "16", operands[0], 1);
            body = replacePlaceHolder(body, "32", operands[0], 1);
            body = replacePlaceHolder(body, "", operands[0], 1);
        }
        if (operands.length > 1) {
            body = replacePlaceHolder(body, "F", operands[1], 2);
            body = replacePlaceHolder(body, "A", operands[1], 2);
            body = replacePlaceHolder(body, "16", operands[1], 2);
            body = replacePlaceHolder(body, "32", operands[1], 2);
            body = replacePlaceHolder(body, "", operands[1], 2);
        }
        if (operands.length > 2) {
            body = replacePlaceHolder(body, "F", operands[2], 3);
            body = replacePlaceHolder(body, "A", operands[2], 3);
            body = replacePlaceHolder(body, "", operands[2], 3);
        }
        body = body.replaceAll("\\$size", size + "");
        if ((name.startsWith("mul_") || name.startsWith("div_")) && size == 32) {
            body = body.replaceAll("\\$mask", "0xFFFFFFFFL & ");
            body = body.replaceAll("\\$cast", "(int)");
        } else {
            body = body.replaceAll("\\$cast", getCast(size));
            if (body.contains("mask2"))
                body = body.replaceAll("\\$mask2", getMask(operands[1].getSize()));
            if (body.contains("mask1"))
                body = body.replaceAll("\\$mask1", getMask(operands[0].getSize()));
            body = body.replaceAll("\\$mask", getMask(size));
        }
        return body;
    }

    private static String replacePlaceHolder(String source, String type, Operand operand, int arg) {
        String result = source;
        if (result.contains("$op" + arg + ".get" + type) || result.contains("$op" + arg + ".set" + type)) {
            result = result.replaceAll("\\$op" + arg + ".get" + type, operand.get(type, arg));
            result = result.replaceAll("\\$op" + arg + ".set" + type, operand.set(type, arg));
        }
        return result;
    }

    private static String getCast(int size) {
        if (size == 8)
            return "(byte)";
        else if (size == 16)
            return "(short)";
        return "";
    }

    private static String getMask(int size) {
        if (size == 8)
            return "0xFF&";
        else if (size == 16)
            return "0xFFFF&";
        return "";
    }

    private static String getDirectConstructor(Opcode op) {
        StringBuilder b = new StringBuilder();
        b.append("\n");
        b.append("    public " + op.getName() + "(" + DecoderGenerator.argsDef + ")\n");
        b.append("    {\n");
        b.append("        super(blockStart, eip);\n");
        if (op.needsModrm())
            b.append("        int modrm = input.readU8();\n");
        if (op.isNeedsSegment())
            b.append("        segIndex = Prefices.getSegment(prefices, Processor.DS_INDEX);\n");
        for (int i = 0; i < op.getOperands().length; i++) {
            String cons = op.getOperands()[i].directConstruct(i + 1);
            if (cons.length() > 0)
                b.append(cons + "\n");
        }
        if (op.isBranch()) {
            b.append("        instructionLength = (int)input.getAddress()-eip;\n");
            b.append("        blockLength = eip-blockStart+instructionLength;\n");
        }
        b.append("    }\n\n");
        return b.toString();
    }

    private static String getBranch(Opcode op) {
        if (op.getRet().equals("Branch.None"))
            return "    public boolean isBranch()\n    {\n        return false;\n    }\n\n";
        else
            return "    public boolean isBranch()\n    {\n        return true;\n    }\n\n";
    }

    private static String getToString(Opcode op) {
        return "    public String toString()\n    {\n        return this.getClass().getName();\n    }\n";
    }
}
