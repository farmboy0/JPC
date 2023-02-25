package tools.generator;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OpcodeParserHandler {
    private OpcodeParserHandler() {
    }

    public static void parseUsing(Callable callable) throws Exception {
        final Document rmpmvm = parseXML("RMPMVM");
        final Document rmvm = parseXML("RMVM");

        int rm = opcodeParse(rmpmvm, "rm", callable);
        rm += opcodeParse(rmvm, "rm", callable);
        rm += opcodeParse(parseXML("RM"), "rm", callable);

        int vm = opcodeParse(rmpmvm, "vm", callable);
        vm += opcodeParse(rmvm, "vm", callable);
        vm += opcodeParse(parseXML("VM"), "vm", callable);

        int pm = opcodeParse(rmpmvm, "pm", callable);
        pm += opcodeParse(parseXML("PM"), "pm", callable);

        System.out.printf("Generated %d RM opcodes, %d VM opcodes and %d PM opcodes\n", rm, vm, pm);
    }

    private static int opcodeParse(Document dom, String mode, Callable call) {
        int count = 0;
        NodeList properties = dom.getElementsByTagName("jcc");
        String jcc = null;
        for (int i = 0; i < properties.getLength(); i++) {
            Node n = properties.item(i);
            String content = n.getTextContent();
            if (content.trim().length() > 0)
                jcc = content;
        }

        NodeList list = dom.getElementsByTagName("opcode");
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            String mnemonic = n.getAttributes().getNamedItem("mnemonic").getNodeValue();
            Node seg = n.getAttributes().getNamedItem("segment");
            boolean segment = seg != null && seg.getNodeValue().equals("true");
            Node memNode = n.getAttributes().getNamedItem("mem");
            boolean singleType = memNode != null;
            boolean mem = memNode != null && memNode.getNodeValue().equals("true");
            NodeList children = n.getChildNodes();
            String ret = null, snippet = null;
            // get return and snippet
            for (int j = 0; j < children.getLength(); j++) {
                Node c = children.item(j);
                if (c.getNodeName().equals("return"))
                    ret = c.getTextContent().trim();
                if (c.getNodeName().equals("snippet"))
                    snippet = c.getTextContent();
                if (c.getNodeName().equals("jcc"))
                    snippet += jcc;
            }
            if (ret == null)
                throw new IllegalStateException("No return value for " + mnemonic);
            if (snippet == null)
                throw new IllegalStateException("No snippet for " + mnemonic);

            // get each opcode definition
            for (int j = 0; j < children.getLength(); j++) {
                Node c = children.item(j);
                if (!c.getNodeName().equals("args"))
                    continue;
                String argsText = c.getTextContent();
                int size = Integer.parseInt(c.getAttributes().getNamedItem("size").getNodeValue());
                String[] args = argsText.split(";");
                if (argsText.length() == 0)
                    args = new String[0];
                List<Opcode> ops = Opcode.get(mnemonic, args, size, snippet, ret, segment, singleType, mem);
                for (Opcode op : ops) {
                    call.call(op, mode);
                    count++;
                }
            }
        }
        return count;
    }

    private static Document parseXML(String mode) throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(OpcodeParserHandler.class.getResourceAsStream("/Opcodes_" + mode + ".xml"));
    }
}
