package tools.generator.template;

import tools.generator.GeneratorHelper;

public class DecoderTemplate {
    public void writeStart(StringBuilder b) {
        b.append("new OpcodeDecoder() {\n    public Executable decodeOpcode(" + GeneratorHelper.argsDef + ") {\n");
    }

    public void writeBody(StringBuilder b) {
        b.append("throw new IllegalStateException(\"Unimplemented Opcode\");");
    }

    public void writeEnd(StringBuilder b) {
        b.append("    }\n};\n");
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        writeStart(b);
        writeBody(b);
        writeEnd(b);
        return b.toString();
    }
}
