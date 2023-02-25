package tools.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jpc.emulator.execution.decoder.Disassembler;
import org.jpc.emulator.execution.decoder.Instruction;

import tools.generator.template.MemoryChooser;
import tools.generator.template.RepChooser;
import tools.generator.template.SingleOpcode;

class OpcodeHolder {
    Map<Instruction, byte[]> myops = new HashMap();
    List<String> names = new ArrayList();
    Set<String> namesSet = new HashSet();
    private int modeType;
    private int copyOf = -1;

    public OpcodeHolder(int modeType) {
        this.modeType = modeType;
    }

    public void addOpcode(Instruction in, byte[] raw) {
        String name = Disassembler.getExecutableName(modeType, in);
        names.add(name);
        namesSet.add(name);
        myops.put(in, raw);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OpcodeHolder))
            return false;
        OpcodeHolder other = (OpcodeHolder)o;
        if ((myops.size() != other.myops.size()) || !namesSet.equals(other.namesSet) || !names.equals(other.names))
            return false;
        return true;
    }

    public void setDuplicateOf(int index) {
        copyOf = index;
    }

    public Map<Instruction, byte[]> getReps() {
        Map<Instruction, byte[]> reps = new HashMap();
        for (Instruction in : myops.keySet())
            if (myops.get(in)[0] == (byte)0xF3)
                reps.put(in, myops.get(in));
        return reps;
    }

    public Map<Instruction, byte[]> getRepnes() {
        Map<Instruction, byte[]> reps = new HashMap();
        for (Instruction in : myops.keySet())
            if (myops.get(in)[0] == (byte)0xF2)
                reps.put(in, myops.get(in));
        return reps;
    }

    public Map<Instruction, byte[]> getNonreps() {
        Map<Instruction, byte[]> reps = new HashMap();
        for (Instruction in : myops.keySet())
            if (myops.get(in)[0] != (byte)0xF2 && myops.get(in)[0] != (byte)0xF3)
                reps.put(in, myops.get(in));
        return reps;
    }

    public boolean hasReps() {
        for (String opname : namesSet)
            if (opname.contains("rep"))
                return true;
        return false;
    }

    public boolean hasUnimplemented() {
        for (String name : names)
            if (name.contains("Unimplemented"))
                return true;
        return false;
    }

    public boolean allUnimplemented() {
        for (String name : names)
            if (!name.contains("Unimplemented"))
                return false;
        return true;
    }

    public boolean isMem() {
        if (namesSet.size() > 2)
            return false;
        String name = null;
        for (String s : namesSet) {
            if (name == null)
                name = s;
            else if ((name + "_mem").equals(s))
                return true;
            else if ((s + "_mem").equals(name))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        if (namesSet.size() == 0)
            return "null;";

        if (copyOf != -1) {
            return String.format("ops[0x%x];\n", copyOf);
        }

        StringBuilder b = new StringBuilder();
        if (namesSet.size() == 1) {
            b.append(new SingleOpcode(names.get(0)));
        } else if (isMem()) {
            String name = null;
            for (String n : namesSet) {
                if (name == null)
                    name = n;
                else if (name.length() > n.length())
                    name = n;
            }
            b.append(new MemoryChooser(name));
        } else {
            if (allUnimplemented()) {
                b.append(new SingleOpcode(names.get(0)));
            } else {
                b.append(new RepChooser(getReps(), getRepnes(), getNonreps(), modeType));
            }
        }
        return b.toString().trim();
    }
}
