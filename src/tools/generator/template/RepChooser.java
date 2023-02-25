package tools.generator.template;

import static tools.generator.GeneratorHelper.getConstructorLine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jpc.emulator.execution.decoder.Instruction;

import tools.generator.DecoderGenerator;
import tools.generator.GeneratorHelper;

public class RepChooser extends DecoderTemplate {
    Map<Instruction, byte[]> reps;
    Map<Instruction, byte[]> repnes;
    Map<Instruction, byte[]> normals;
    int mode;

    public RepChooser(Map<Instruction, byte[]> reps, Map<Instruction, byte[]> repnes, Map<Instruction, byte[]> normals, int mode) {
        this.reps = reps;
        this.repnes = repnes;
        this.normals = normals;
        this.mode = mode;
    }

    @Override
    public void writeBody(StringBuilder b) {
        Set<String> repNames = new HashSet<String>();
        for (Instruction in : reps.keySet())
            repNames.add(DecoderGenerator.getExecutableName(mode, in));
        Set<String> repneNames = new HashSet<String>();
        for (Instruction in : repnes.keySet())
            repneNames.add(DecoderGenerator.getExecutableName(mode, in));
        Set<String> normalNames = new HashSet<String>();
        for (Instruction in : normals.keySet())
            normalNames.add(DecoderGenerator.getExecutableName(mode, in));

        // only add rep clauses if rep name sets are different to normal name set
        if (!normalNames.containsAll(repneNames))
            if (repnes.size() > 0) {
                b.append("        if (Prefices.isRepne(prefices))\n        {\n");
                genericChooser(repnes, mode, b);
                b.append("        }\n");
            }
        if (!normalNames.containsAll(repNames))
            if (reps.size() > 0) {
                b.append("        if (Prefices.isRep(prefices))\n        {\n");
                genericChooser(reps, mode, b);
                b.append("        }\n");
            }
        genericChooser(normals, mode, b);
    }

    private static void genericChooser(Map<Instruction, byte[]> ops, int mode, StringBuilder b) {
        if (ops.size() == 0)
            return;
        if (ops.size() == 1) {
            for (Instruction in : ops.keySet()) {
                String name = DecoderGenerator.getExecutableName(mode, in);
                b.append("            return new " + name + "(" + GeneratorHelper.args + ");\n");
            }
            return;
        }
        int differentIndex = 0;
        byte[][] bs = new byte[ops.size()][];
        int index = 0;
        for (byte[] bytes : ops.values())
            bs[index++] = bytes;
        boolean same = true;
        while (same) {
            byte elem = bs[0][differentIndex];
            for (int i = 1; i < bs.length; i++)
                if (bs[i][differentIndex] != elem) {
                    same = false;
                    break;
                }
            if (same)
                differentIndex++;
        }
        // if all names are the same, collapse to 1
        String prevname = null;
        boolean allSameName = true;
        for (Instruction in : ops.keySet()) {
            String name = DecoderGenerator.getExecutableName(mode, in);
            if (prevname == null)
                prevname = name;
            else if (prevname.equals(name))
                continue;
            else {
                allSameName = false;
                break;
            }
        }
        if (allSameName) {
            b.append("        return new " + prevname + "(" + GeneratorHelper.args + ");\n");
        } else if (isSimpleModrmSplit(ops, mode, differentIndex, b)) {

        } else if (almostIsSimpleModrmSplit(ops, mode, differentIndex, b)) {

        } else {
            String[] cases = new String[ops.size()];
            int i = 0;
            for (Instruction in : ops.keySet()) {
                String name = DecoderGenerator.getExecutableName(mode, in);
                cases[i++] = getConstructorLine(name, ops.get(in)[differentIndex]);
            }
            b.append("        switch (input.peek()) {\n");
            Arrays.sort(cases);
            for (String line : cases)
                b.append(line);
            b.append("        }\n        return null;\n");
        }
    }

    private static boolean almostIsSimpleModrmSplit(Map<Instruction, byte[]> ops, int mode, int differentIndex, StringBuilder b) {
        String[] names = new String[256];
        for (Instruction in : ops.keySet())
            names[ops.get(in)[differentIndex] & 0xff] = DecoderGenerator.getExecutableName(mode, in);
        boolean subC0Simple = true;
        for (int i = 0; i < 8; i++)
            for (int k = 0; k < 0xC0; k += 0x40)
                for (int j = 0; j < 8; j++)
                    if (!names[j + k + (i << 3)].equals(names[i << 3]))
                        subC0Simple = false;
        boolean postC0Simple = true;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (!names[j + 0xC0 + (i << 3)].equals(names[0xC0 + (i << 3)]))
                    postC0Simple = false;

        if (subC0Simple) {
            b.append("        int modrm = input.peek() & 0xFF;\n");
            b.append("        int reg = (modrm >> 3) & 7;\n");
            b.append("        if (modrm < 0xC0)\n        {\n");
            b.append("            switch (reg) {\n");
            for (int i = 0; i < 8; i++)
                b.append(getConstructorLine(names[i * 8], i));
            b.append("            }\n");
            b.append("        }\n");

            // post must be false otherwise IsSimpleModrmSplit would be true
            b.append("            switch (modrm) {\n");
            for (int i = 0xc0; i < 0x100; i++)
                if (i + 1 < 0x100 && names[i].equals(names[i + 1]))
                    b.append(String.format("            case 0x%02x:\n", i));
                else
                    b.append(getConstructorLine(names[i], i));
            b.append("            }\n");
            b.append("        return null;\n");
            return true;
        } else
            return false;
    }

    private static boolean isSimpleModrmSplit(Map<Instruction, byte[]> ops, int mode, int differentIndex, StringBuilder b) {
        String[] names = new String[256];
        for (Instruction in : ops.keySet())
            names[ops.get(in)[differentIndex] & 0xff] = DecoderGenerator.getExecutableName(mode, in);
        for (int i = 0; i < 8; i++)
            for (int k = 0; k < 0xC0; k += 0x40)
                for (int j = 0; j < 8; j++)
                    if (!names[j + k + (i << 3)].equals(names[i << 3]))
                        return false;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (!names[j + 0xC0 + (i << 3)].equals(names[0xC0 + (i << 3)]))
                    return false;

        // write out code
        b.append("        int modrm = input.peek() & 0xFF;\n");
        b.append("        int reg = (modrm >> 3) & 7;\n");
        b.append("        if (modrm < 0xC0)\n        {\n");
        b.append("            switch (reg) {\n");
        for (int i = 0; i < 8; i++)
            if (i + 1 < 8 && names[i * 8].equals(names[i * 8 + 8]))
                b.append(String.format("            case 0x%02x:\n", i));
            else
                b.append(getConstructorLine(names[i * 8], i));
        b.append("            }\n");
        b.append("        }\n");
        b.append("        else\n        {\n");
        b.append("            switch (reg) {\n");
        for (int i = 0; i < 8; i++)
            if (i + 1 < 8 && names[0xc0 + i * 8].equals(names[0xc0 + i * 8 + 8]))
                b.append(String.format("            case 0x%02x:\n", i));
            else
                b.append(getConstructorLine(names[0xc0 + i * 8], i));
        b.append("            }\n");
        b.append("        }\n");
        b.append("        return null;\n");
        return true;
    }
}
