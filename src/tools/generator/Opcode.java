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

import java.util.LinkedList;
import java.util.List;

public class Opcode {
    private final String name;
    private final Operand[] operands;
    private final String snippet;
    private final String ret;
    private final int size;
    private final boolean multiSize;
    private final boolean mem, branch, needsSegment;

    private Opcode(String mnemonic, String[] args, int size, String snippet, String ret, boolean isMem, boolean needsSegment) {
        this.needsSegment = needsSegment;
        boolean msize = false;
        for (String s : args)
            if (s.equals("Ev") || s.equals("Gv") || s.equals("Iv") || s.equals("Iz")
                || s.equals("Ov") && !mnemonic.contains("o16") && !mnemonic.contains("o32"))
                msize = true;
        multiSize = msize;
        operands = new Operand[args.length];
        for (int i = 0; i < operands.length; i++)
            operands[i] = Operand.get(args[i], size, isMem);
        StringBuilder tmp = new StringBuilder();
        tmp.append(mnemonic);
        for (Operand operand : operands) {
            tmp.append("_");
            tmp.append(operand);
        }
        if (isMem)
            tmp.append("_mem");
        this.mem = isMem;
        name = tmp.toString();
        this.snippet = snippet;
        this.ret = ret;
        this.size = size;
        branch = !ret.startsWith("Branch.None");
    }

    public String getName() {
        return name;
    }

    public Operand[] getOperands() {
        return operands;
    }

    public String getRet() {
        return ret;
    }

    public int getSize() {
        return size;
    }

    public String getSnippet() {
        return snippet;
    }

    public boolean isMultiSize() {
        return multiSize;
    }

    public boolean isBranch() {
        return branch;
    }

    public boolean isMem() {
        return mem;
    }

    public boolean isNeedsSegment() {
        return needsSegment;
    }

    public boolean needsModrm() {
        for (Operand operand : operands) {
            if (operand instanceof Operand.Address)
                return true;
            else if (operand instanceof Operand.ControlReg)
                return true;
            else if (operand instanceof Operand.DebugReg)
                return true;
            else if (operand instanceof Operand.Mem)
                return operand.needsModrm();
            else if (operand instanceof Operand.Reg)
                return true;
            else if (operand instanceof Operand.Segment)
                return true;
            else if (operand instanceof Operand.STi)
                return true;
            else if (operand instanceof Operand.FarMemPointer)
                return true;
        }
        return false;
    }

    private static boolean isMem(String[] args) {
        for (String arg : args)
            if (arg.equals("Eb") || arg.equals("Ew") || arg.equals("Ed") || arg.equals("Ob") || arg.equals("Ow") || arg.equals("Od")
                || arg.equals("M") || arg.equals("R"))
                return true;
        return false;
    }

    private static boolean isMemOnly(String[] args) {
        for (String arg : args)
            if (arg.equals("Ep"))
                return true;
        if (args.length == 1 && (args[0].equals("Mw") || args[0].equals("Md") || args[0].equals("Mq") || args[0].equals("Mt")))
            return true;
        return false;
    }

    private static List<String> enumerateArg(String arg) {
        List<String> res = new LinkedList();
        if (arg.equals("STi")) {
            res.add("ST0");
            res.add("ST1");
            res.add("ST2");
            res.add("ST3");
            res.add("ST4");
            res.add("ST5");
            res.add("ST6");
            res.add("ST7");
        } else
            res.add(arg);
        return res;
    }

    private static List<String[]> enumerateArgs(String[] in) {
        List<String[]> res = new LinkedList();
        List<String[]> next = new LinkedList();
        res.add(in);
        for (int i = 0; i < in.length; i++) {
            for (String[] args : res) {
                for (String arg : enumerateArg(args[i])) {
                    String[] tmp = new String[args.length];
                    System.arraycopy(args, 0, tmp, 0, tmp.length);
                    tmp[i] = arg;
                    next.add(tmp);
                }
            }
            res = next;
            if (i < in.length - 1)
                next = new LinkedList();
        }
        if (in.length == 0)
            next.add(new String[0]);
        return next;
    }

    public static List<Opcode> get(String mnemonic, String[] args, int size, String snippet, String ret, boolean segment,
        boolean singleType, boolean mem) {
        List<Opcode> ops = new LinkedList();
        if (isMemOnly(args)) {
            ops.add(new Opcode(mnemonic, args, size, snippet, ret, true, segment));
            return ops;
        }
        for (String[] eachArgs : enumerateArgs(args)) {
            if (!singleType || singleType && !mem)
                ops.add(new Opcode(mnemonic, eachArgs, size, snippet, ret, false, segment));
            if (!singleType || singleType && mem)
                if (isMem(args))
                    ops.add(new Opcode(mnemonic, eachArgs, size, snippet, ret, true, segment));
        }
        return ops;
    }
}
