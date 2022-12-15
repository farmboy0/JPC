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

package org.jpc.debugger;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jpc.debugger.util.UtilityFrame;
import org.jpc.emulator.PC;
import org.jpc.emulator.pci.peripheral.VGACard;
import org.jpc.j2se.PCMonitor;

public class PCMonitorFrame extends UtilityFrame implements PCListener {
    private PC currentPC;
    private PCMonitor monitor;
    private JScrollPane main;

    public PCMonitorFrame() {
        super("PC Monitor");

        currentPC = null;
        monitor = null;
        main = new JScrollPane();

        add("Center", main);
        JPC.getInstance().objects().addObject(this);
    }

    @Override
    public void setSize(Dimension d) {
        java.awt.Insets borders = this.getBorder().getBorderInsets(this);
        super.setSize(new Dimension(d.width + borders.bottom + borders.top + 10, d.height + borders.left + borders.right + 25));
    }

    public void loadMonitorState(InputStream in) throws IOException {
        monitor.loadState(in);
    }

    public void resizeDisplay() {
        ((VGACard)currentPC.getComponent(VGACard.class)).setOriginalDisplaySize();
    }

    public void saveState(OutputStream out) throws IOException {
        monitor.saveState(out);
    }

    @Override
    public void frameClosed() {
        if (monitor != null)
            monitor.stopUpdateThread();
        JPC.getInstance().objects().removeObject(this);
    }

    @Override
    public void pcCreated() {
    }

    @Override
    public void pcDisposed() {
        dispose();
    }

    @Override
    public void executionStarted() {
    }

    @Override
    public void executionStopped() {
    }

    @Override
    public void refreshDetails() {
        PC pc = (PC)JPC.getObject(PC.class);
        if (pc != currentPC) {
            if (monitor != null) {
                monitor.stopUpdateThread();
                main.setViewportView(new JPanel());
                monitor.setFrame(this);
                setPreferredSize(monitor.getPreferredSize());
            }

            currentPC = pc;
            if (pc != null) {
                monitor = new PCMonitor(pc);
                monitor.setFrame(this);
                setPreferredSize(monitor.getPreferredSize());
                monitor.startUpdateThread();
                main.setViewportView(monitor);
                monitor.revalidate();
                main.revalidate();
                monitor.requestFocus();
            }
        }
    }
}
