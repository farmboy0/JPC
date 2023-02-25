package tools.generator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GeneratorHelper {
    public static final String args = "blockStart, eip, prefices, input";
    public static final String argsDef = "int blockStart, int eip, int prefices, PeekableInputStream input";

    private static final String LICENSE_HEADER_FILE = "LicenseHeader";

    private GeneratorHelper() {
    }

    public static String readLicenseHeader() throws IOException {
        final StringBuilder header = new StringBuilder();
        final BufferedReader r = new BufferedReader(new FileReader(LICENSE_HEADER_FILE));

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

        return header.toString();
    }

    public static String getConstructorLine(String name, int modrm) {
        modrm &= 0xff;
        String[] argTypes;
        if (name.contains("_"))
            argTypes = name.substring(name.indexOf("_") + 1).split("_");
        else
            argTypes = new String[0];
        boolean consumesModrm = false;
        for (String arg : argTypes)
            if (!Operand.segs.containsKey(arg) && !Operand.reg8.containsKey(arg) && !Operand.reg16.containsKey(arg)
                && !Operand.reg16only.containsKey(arg))
                consumesModrm = true;
        if (!consumesModrm && !name.contains("Unimplemented") && !name.contains("Illegal")) // has zero args, but uses modrm as opcode extension
            return String.format("            case 0x%02x", modrm) + ": input.read8(); return new " + name + "(" + args + ");\n";
        else
            return String.format("            case 0x%02x", modrm) + ": return new " + name + "(" + args + ");\n";
    }
}
