/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.4

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007-2010 The University of Oxford

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

    Conceived and Developed by:
    Rhys Newman, Ian Preston, Chris Dennis

    End of licence header
*/

package org.jpc.j2se;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import org.jpc.assembly.Disassembler;
import org.jpc.assembly.Instruction;
import org.jpc.emulator.DriveSet;
import org.jpc.emulator.PC;
import org.jpc.emulator.pci.peripheral.EthernetCard;
import org.jpc.support.EthernetHub;
import org.jpc.support.EthernetOutput;

public class JPCApplication {
    private static final Logger LOGGING = Logger.getLogger(JPCApplication.class.getName());

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("-disam")) {
            boolean is32Bit = args[1].equals("32");
            byte[] m = new byte[args.length - 2];
            for (int i = 0; i < m.length; i++)
                m[i] = (byte)Integer.parseInt(args[i + 2], 16);
            System.out.println(disam(m, 1, is32Bit));
            return;
        }

        Option.parse(args);
        if (Option.help.isSet()) {
            Option.printHelp();
            System.exit(0);
        }

        PC pc = new PC(new VirtualClock(), new DriveSet());
        PCMonitor monitor = new PCMonitor(pc);

        String net = Option.net.value();
        if (Option.net.isSet() && net.startsWith("hub:")) {
            int port = 80;
            String server;
            int index = net.indexOf(":", 5);
            if (index != -1) {
                port = Integer.parseInt(net.substring(index + 1));
                server = net.substring(4, index);
            } else
                server = net.substring(4);
            EthernetOutput hub = new EthernetHub(server, port);
            EthernetCard card = pc.getComponent(EthernetCard.class);
            card.setOutputDevice(hub);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGING.log(Level.INFO, "System Look-and-Feel not loaded", e);
        }

        final JPCApplicationWindow window = new JPCApplicationWindow(monitor);
        window.show();
    }

    public static String disam(byte[] code, Integer ops, boolean is32Bit) {
        Disassembler.ByteArrayPeekStream mem = new Disassembler.ByteArrayPeekStream(code);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < ops; i++) {
            Instruction disam = is32Bit ? Disassembler.disassemble32(mem) : Disassembler.disassemble16(mem);
            mem.seek(disam.x86Length);
            b.append(disam.toString() + "\n");
        }
        return b.toString();
    }
}
