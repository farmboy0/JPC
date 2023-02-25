package tools.generator.template;

import tools.generator.GeneratorHelper;

public class MemoryChooser extends DecoderTemplate {
    String name;

    public MemoryChooser(String name) {
        this.name = name;
    }

    @Override
    public void writeBody(StringBuilder b) {
        b.append("        if (Modrm.isMem(input.peek()))\n            return new " + name + "_mem(" + GeneratorHelper.args
            + ");\n        else\n            return new " + name + "(" + GeneratorHelper.args + ");\n");
    }
}
