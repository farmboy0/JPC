package tools.generator.template;

import tools.generator.GeneratorHelper;

public class SingleOpcode extends DecoderTemplate {
    String classname;

    public SingleOpcode(String name) {
        this.classname = name;
    }

    @Override
    public void writeBody(StringBuilder b) {
        b.append("        return new " + classname + "(" + GeneratorHelper.args + ");\n");
    }
}
