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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.jpc.debugger.util.BasicTableModel;
import org.jpc.debugger.util.UtilityFrame;
import org.jpc.emulator.memory.AddressSpace;
import org.jpc.emulator.memory.PhysicalAddressSpace;

public class WatchpointsFrame extends UtilityFrame implements PCListener {
    public static final String WATCHPOINT_FILE = "watchpoints.jpc";
    public static final long WATCHPOINT_MAGIC = 0x81057FAB7272F11L;

    private boolean edited;
    private List<Watchpoint> watchpoints;
    private WPModel model;
    private JTable wpTable;
    private String watchpointFileName;
    private AddressSpace addressSpace;

    private JCheckBoxMenuItem ignoreWP, watchPrimary;

    public WatchpointsFrame() {
        super("Watchpoints");

        watchpointFileName = WATCHPOINT_FILE;
        watchpoints = new Vector();
        model = new WPModel();
        edited = false;

        addressSpace = (AddressSpace)JPC.getObject(PhysicalAddressSpace.class);

        wpTable = new JTable(model);
        model.setupColumnWidths(wpTable);

        String delWP = "Del WP";
        InputMap in = new InputMap();
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), delWP);
        ActionMap ac = new ActionMap();
        ac.setParent(wpTable.getActionMap());
        ac.put(delWP, new Deleter());

        wpTable.setInputMap(JComponent.WHEN_FOCUSED, in);
        wpTable.setActionMap(ac);

        add("Center", new JScrollPane(wpTable));

        JMenu options = new JMenu("Options");
        options.add("Set Watchpoint").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    String input = JOptionPane.showInputDialog(WatchpointsFrame.this, "Enter the address (in Hex) for the watchpoint: ",
                        "Watchpoint", JOptionPane.QUESTION_MESSAGE);
                    int address = (int)Long.parseLong(input.toLowerCase(), 16);
                    setWatchpoint(address);
                } catch (Exception e) {
                }
            }
        });
        options.addSeparator();
        options.add("Remove All Watchpoints").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                removeAllWatchpoints();
            }
        });

        options.addSeparator();
        ignoreWP = new JCheckBoxMenuItem("Ignore Watchpoints");
        options.add(ignoreWP);
        watchPrimary = new JCheckBoxMenuItem("Watch 'Primary' watchpoints only");
        options.add(watchPrimary);

        JMenuBar bar = new JMenuBar();
        bar.add(new WPFileMenu());
        bar.add(options);
        setJMenuBar(bar);

        setPreferredSize(new Dimension(600, 300));
        JPC.getInstance().objects().addObject(this);
        loadWatchpoints();
    }

    public boolean WatchPrimaryOnly() {
        return watchPrimary.getState();
    }

    public boolean ignoreWatchpoints() {
        return ignoreWP.getState();
    }

    public boolean isEdited() {
        return edited;
    }

    @Override
    public void frameClosed() {
        if (edited) {
            if (JOptionPane.showConfirmDialog(this, "Do you want to save the changes to the Watchpoints?", "Save Watchpoints",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                saveWatchpoints();
            edited = false;
        }

        JPC.getInstance().objects().removeObject(this);
    }

    class WPFileMenu extends JMenu implements ActionListener {
        private JMenuItem load, save, saveAs, importWP;

        WPFileMenu() {
            super("File");

            load = add("Load Watchpoints");
            load.addActionListener(this);
            save = add("Save Watchpoints");
            save.addActionListener(this);
            saveAs = add("Save Watchpoints As");
            saveAs.addActionListener(this);
            addSeparator();
            importWP = add("Import Watchpoints");
            importWP.addActionListener(this);
        }

        private String deriveWPFileName(String name) {
            String nm = name.toLowerCase();
            if (nm.endsWith(".jpc"))
                return name;

            int dot = nm.indexOf('.');
            if (dot < 0)
                dot = nm.length();

            return name.substring(0, dot) + ".jpc";
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            JFileChooser chooser = (JFileChooser)JPC.getObject(JFileChooser.class);
            if (evt.getSource() == load) {
                if (chooser.showOpenDialog(JPC.getInstance()) != JFileChooser.APPROVE_OPTION)
                    return;

                watchpointFileName = chooser.getSelectedFile().getAbsolutePath();
                removeAllWatchpoints();
                loadWatchpoints();
            } else if (evt.getSource() == save) {
                saveWatchpoints();
            } else if (evt.getSource() == importWP) {
                if (chooser.showOpenDialog(JPC.getInstance()) != JFileChooser.APPROVE_OPTION)
                    return;

                removeAllWatchpoints();
                String fileName = chooser.getSelectedFile().getAbsolutePath();
                importWatchpoints(fileName, false);
            } else if (evt.getSource() == saveAs) {
                if (chooser.showSaveDialog(JPC.getInstance()) != JFileChooser.APPROVE_OPTION)
                    return;

                watchpointFileName = chooser.getSelectedFile().getAbsolutePath();
                saveWatchpoints();
            }
        }
    }

    class Deleter extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            deleteWatchpoint(wpTable.getSelectedRow());
        }
    }

    public boolean isWatchpoint(int address) {
        Watchpoint wp = new Watchpoint(address);
        return watchpoints.contains(wp);
    }

    public void setWatchpoint(int address) {
        setWatchpoint(address, false);
    }

    public void setWatchpoint(int address, boolean isPrimary) {
        Watchpoint wp = new Watchpoint(address);
        int idx = watchpoints.indexOf(wp);
        if (idx < 0)
            watchpoints.add(wp);
        else
            wp = watchpoints.get(idx);

        if (isPrimary)
            wp.isPrimary = isPrimary;

        edited = true;
        JPC.getInstance().refresh();
    }

    public void removeAllWatchpoints() {
        watchpoints.clear();
        edited = true;
        JPC.getInstance().refresh();
    }

    public void removeWatchpoint(int address) {
        Watchpoint wp = new Watchpoint(address);
        int idx = watchpoints.indexOf(wp);
        if (idx < 0)
            return;

        deleteWatchpoint(idx);
    }

    public Watchpoint checkForWatch() {
        return checkForWatch(watchPrimary.getState());
    }

    public Watchpoint checkForWatch(boolean isPrimary) {
        if (ignoreWP.getState())
            return null;

        for (Watchpoint wp : watchpoints) {
            byte b = addressSpace.getByte(wp.address);
            //if ((b != (byte) 0xff) && (b != 0) && (wp.value!=b))
            if (wp.value != b) {
                if (isPrimary && !wp.isPrimary)
                    continue;

                return wp;
            }
        }

        return null;
    }

    private void deleteWatchpoint(int index) {
        try {
            watchpoints.remove(index);
        } catch (IndexOutOfBoundsException e) {
        }
        edited = true;

        JPC.getInstance().refresh();
    }

    public class Watchpoint implements Comparable<Watchpoint> {
        private int address;
        private int value;
        private boolean isPrimary;
        private String name;
        private boolean watchForValue;
        private byte watchValue;

        Watchpoint(int addr) {
            this(addr, false, (byte)0, false);
        }

        public Watchpoint(String name, int addr, byte watchValue, boolean watchForValue) {
            this(addr, false, watchValue, watchForValue);
            this.name = name;
        }

        Watchpoint(int addr, boolean primary, byte watchValue, boolean watchForValue) {
            address = addr;
            isPrimary = primary;
            name = "";
            value = addressSpace.getByte(addr);
            this.watchValue = watchValue;
            this.watchForValue = watchForValue;
        }

        @Override
        public boolean equals(Object another) {
            if (!(another instanceof Watchpoint))
                return false;

            return address == ((Watchpoint)another).address && watchValue == ((Watchpoint)another).watchValue;
        }

        @Override
        public int compareTo(Watchpoint wp) {
            if (address != wp.address)
                return address - wp.address;
            if (watchValue == wp.watchValue)
                return 0;
            if ((0xff & watchValue) < (0xff & wp.watchValue))
                return -1;
            return 1;
        }

        public String getName() {
            return name;
        }

        public int getAddress() {
            return address;
        }

        public int getValue() {
            return value;
        }

        public void updateValue() {
            value = addressSpace.getByte(address);
        }

        public boolean isWatchingForValue() {
            return watchForValue;
        }

        public byte getWatchTarget() {
            return watchValue;
        }

        public boolean isPrimary() {
            return isPrimary;
        }
    }

    class WPModel extends BasicTableModel {
        WPModel() {
            super(new String[] { "Address", "Name", "Primary", "Watch target", "Watch for value" }, new int[] { 80, 250, 70, 90, 90 });
        }

        @Override
        public int getRowCount() {
            return watchpoints.size();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        @Override
        public Class getColumnClass(int col) {
            if (col == 2 || col == 4)
                return Boolean.class;
            return String.class;
        }

        @Override
        public void setValueAt(Object obj, int row, int column) {
            Watchpoint wp = watchpoints.get(row);

            if (column == 0) {
                try {
                    int addr = (int)Long.parseLong(obj.toString().toLowerCase(), 16);
                    wp.address = addr;
                } catch (Exception e) {
                }
            } else if (column == 2)
                wp.isPrimary = ((Boolean)obj).booleanValue();
            else if (column == 1)
                wp.name = obj.toString();
            else if (column == 3)
                wp.watchValue = (byte)Integer.parseInt(obj.toString(), 16);
            else if (column == 4)
                wp.watchForValue = ((Boolean)obj).booleanValue();

            int selected = sortWatchpoints(row);
            JPC.getInstance().refresh();

            if (selected >= 0) {
                wpTable.setRowSelectionInterval(selected, selected);
                Rectangle rect = wpTable.getCellRect(selected, 0, true);
                wpTable.scrollRectToVisible(rect);
            }
            edited = true;
        }

        @Override
        public Object getValueAt(int row, int column) {
            Watchpoint wp = watchpoints.get(row);

            switch (column) {
            case 0:
                return MemoryViewPanel.zeroPadHex(wp.address, 8);
            case 1:
                return wp.name;
            case 2:
                return Boolean.valueOf(wp.isPrimary);
            case 3:
                return String.format("%02x", wp.watchValue);
            case 4:
                return Boolean.valueOf(wp.watchForValue);
            default:
                return "";
            }
        }
    }

    private int sortWatchpoints(int selectedRow) {
        Watchpoint selected = null;
        if (selectedRow >= 0)
            selected = watchpoints.get(selectedRow);

        Collections.sort(watchpoints);

        if (selected == null)
            return 0;

        for (int i = 0; i < watchpoints.size(); i++) {
            if (watchpoints.get(i) == selected)
                return i;
        }

        return 0;
    }

    public boolean importWatchpoints(String fileName, boolean ignoreDots) {
        List<Watchpoint> loaded = new ArrayList<Watchpoint>();

        File f = new File(fileName);
        if (!f.exists())
            return false;

        try {
            FileReader fin = new FileReader(f);
            try {
                BufferedReader in = new BufferedReader(fin);

                while (true) {

                    String line = in.readLine();
                    if (line == null)
                        break;

                    String[] elements = line.split("\\s", 2);

                    String name = elements[1];
                    if (name.startsWith(".") && ignoreDots)
                        continue;

                    int addr = Integer.parseInt(elements[0], 16);
                    byte watchValue = (byte)0;
                    boolean watchForValue = false;
                    if (elements.length > 2) {
                        watchValue = (byte)Integer.parseInt(elements[2], 16);
                        watchForValue = true;
                    }

                    loaded.add(new Watchpoint(name, addr, watchValue, watchForValue));
                }
            } catch (IOException e) {
                return false;
            } finally {
                try {
                    fin.close();
                } catch (IOException e) {
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        }

        watchpoints.clear();
        watchpoints.addAll(loaded);
        sortWatchpoints(-1);
        edited = true;
        return true;
    }

    public void loadWatchpoints() {
        FileInputStream fin = null;
        watchpoints.clear();

        try {
            File f = new File(watchpointFileName);
            if (!f.exists())
                return;

            fin = new FileInputStream(f);
            DataInputStream din = new DataInputStream(fin);

            if (din.readLong() != WATCHPOINT_MAGIC)
                throw new IOException("Magic number mismatch");

            while (true) {
                int addr = din.readInt();
                boolean primary = din.readBoolean();
                String name = din.readUTF();
                byte watchValue = din.readByte();
                boolean watchForValue = din.readBoolean();

                Watchpoint wp = new Watchpoint(addr, primary, watchValue, watchForValue);
                wp.name = name;
                watchpoints.add(wp);
            }
        } catch (EOFException e) {
            setTitle("Watchpoints: " + watchpointFileName);
        } catch (Exception e) {
            System.out.println("Warning: failed to load watchpoints");
            e.printStackTrace();
            setTitle("Watchpoints: " + watchpointFileName + " ERROR");
            alert("Error loading watchpoints: " + e, JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                fin.close();
            } catch (Exception e) {
            }
            sortWatchpoints(-1);
            edited = false;
        }
    }

    public void saveWatchpoints() {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(watchpointFileName);
            DataOutputStream dout = new DataOutputStream(out);
            dout.writeLong(WATCHPOINT_MAGIC);

            for (Watchpoint wp : watchpoints) {
                dout.writeInt(wp.address);
                dout.writeBoolean(wp.isPrimary);
                dout.writeUTF(wp.name);
                dout.writeByte(wp.watchValue);
                dout.writeBoolean(wp.watchForValue);
            }

            setTitle("Watchpoints: " + watchpointFileName);
        } catch (Exception e) {
            System.out.println("Warning: failed to save watchpoints");
            e.printStackTrace();
            setTitle("Watchpoints: " + watchpointFileName + " ERROR");
            alert("Error saving watchpoints: " + e, JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
            edited = false;
        }
    }

    @Override
    public void pcCreated() {
    }

    @Override
    public void pcDisposed() {
    }

    @Override
    public void executionStarted() {
    }

    @Override
    public void executionStopped() {
    }

    @Override
    public void refreshDetails() {
        model.fireTableDataChanged();
    }
}
