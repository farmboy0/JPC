package org.jpc.j2se;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.jpc.emulator.PC;
import org.jpc.emulator.block.TreeBlockDevice;
import org.jpc.emulator.pci.VGACard;

public class JPCApplicationWindow {
    private static final Logger LOGGING = Logger.getLogger(JPCApplicationWindow.class.getName());

    private static final int MONITOR_WIDTH = 720;
    private static final int MONITOR_HEIGHT = 400 + 100;
    private static final int COUNTDOWN = 10000000;

    private static final DecimalFormat TWO_DP = new DecimalFormat("0.00");
    private static final DecimalFormat THREE_DP = new DecimalFormat("0.000");

    private static final URI JPC_URI = URI.create("http://jpc.sourceforge.net/");
    private static final String ABOUT_US = """
        JPC: Developed since August 2005 in Oxford University's Subdepartment of Particle Physics,
        and subsequently rewritten by Ian Preston.


        For more information visit our website at:
        http://jpc.sourceforge.net/""";
    private static final String LICENCE_HTML = """
        JPC is released under GPL Version 2 and comes with absolutely no warranty


        See http://jpc.sourceforge.net/ for more details""";

    private final PCMonitor monitor;
    private final PC pc;

    private final ExecutorService pcRunner;

    private JFrame frame;
    private JMenuBar menuBar;
    private JMenu mnFile;
    private JMenu mnSnapshot;
    private DriveSetHandler driveSetHandler;
    private JMenu mnHelp;
    private JMenu mnTools;
    private JMenuItem mntmStart;
    private JMenuItem mntmStop;
    private JMenuItem mntmReset;
    private JMenuItem mntmLoadConfiguration;
    private JMenuItem mntmSaveConfiguration;
    private JMenuItem mntmQuit;
    private JMenuItem mntmLoadSnapshot;
    private JMenuItem mntmSaveSnapshot;
    private JMenuItem mntmCreateFloppyDisk;
    private JMenuItem mntmCreateDiskFrom;
    private JMenuItem mntmGettingStarted;
    private JMenuItem mntmAboutJpc;
    private JScrollPane monitorPane;
    private JProgressBar speedDisplay;

    private JFileChooser configFileChooser;
    private JFileChooser snapshotFileChooser;
    private JFileChooser imageFileChooser;
    private JFileChooser directoryChooser;

    private JEditorPane licensePane;

    public JPCApplicationWindow(PCMonitor monitor) {
        this.monitor = monitor;
        this.pc = monitor.getPC();
        this.pcRunner = Executors.newSingleThreadExecutor(r -> new Thread(r, "PC Execute"));
        initialize();
    }

    public void show() {
        frame.validate();
        frame.setVisible(true);
        start();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, MONITOR_WIDTH + 20, MONITOR_HEIGHT + 70);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("icon.png")));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setJMenuBar(getMenuBar());
        frame.getContentPane().add(getMonitorPane(), BorderLayout.CENTER);
        frame.getContentPane().add(getSpeedDisplay(), BorderLayout.SOUTH);
    }

    private JMenuBar getMenuBar() {
        if (menuBar == null) {
            menuBar = new JMenuBar();
            menuBar.add(getMnFile());
            menuBar.add(getMnSnapshot());
            menuBar.add(getMnDisks());
            menuBar.add(getMnTools());
            menuBar.add(getMnHelp());
        }
        return menuBar;
    }

    private JMenu getMnFile() {
        if (mnFile == null) {
            mnFile = new JMenu("File");
            mnFile.add(getMntmStart());
            mnFile.add(getMntmStop());
            mnFile.add(getMntmReset());
            mnFile.addSeparator();
            mnFile.add(getMntmLoadConfiguration());
            mnFile.add(getMntmSaveConfiguration());
            mnFile.addSeparator();
            mnFile.add(getMntmQuit());
        }
        return mnFile;
    }

    private JMenu getMnSnapshot() {
        if (mnSnapshot == null) {
            mnSnapshot = new JMenu("Snapshot");
            mnSnapshot.add(getMntmLoadSnapshot());
            mnSnapshot.add(getMntmSaveSnapshot());
        }
        return mnSnapshot;
    }

    private JMenu getMnDisks() {
        if (driveSetHandler == null) {
            driveSetHandler = new DriveSetHandler(monitor, frame);
        }
        return driveSetHandler.getDriveSetMenu();
    }

    private JMenu getMnHelp() {
        if (mnHelp == null) {
            mnHelp = new JMenu("Help");
            mnHelp.add(getMntmGettingStarted());
            mnHelp.add(getMntmAboutJpc());
        }
        return mnHelp;
    }

    private JMenu getMnTools() {
        if (mnTools == null) {
            mnTools = new JMenu("Tools");
            mnTools.add(getMntmCreateFloppyDisk());
            mnTools.add(getMntmCreateDiskFrom());
        }
        return mnTools;
    }

    private JMenuItem getMntmStart() {
        if (mntmStart == null) {
            mntmStart = new JMenuItem("Start");
            mntmStart.addActionListener(e -> start());
        }
        return mntmStart;
    }

    private JMenuItem getMntmStop() {
        if (mntmStop == null) {
            mntmStop = new JMenuItem("Stop");
            mntmStop.addActionListener(e -> stop());
        }
        return mntmStop;
    }

    private JMenuItem getMntmReset() {
        if (mntmReset == null) {
            mntmReset = new JMenuItem("Reset");
            mntmReset.addActionListener(e -> reset());
        }
        return mntmReset;
    }

    private JMenuItem getMntmLoadConfiguration() {
        if (mntmLoadConfiguration == null) {
            mntmLoadConfiguration = new JMenuItem("Load Configuration");
            mntmLoadConfiguration.addActionListener(e -> loadConfig());
        }
        return mntmLoadConfiguration;
    }

    private JMenuItem getMntmSaveConfiguration() {
        if (mntmSaveConfiguration == null) {
            mntmSaveConfiguration = new JMenuItem("Save Configuration");
            mntmSaveConfiguration.addActionListener(e -> saveConfig());
        }
        return mntmSaveConfiguration;
    }

    private JMenuItem getMntmQuit() {
        if (mntmQuit == null) {
            mntmQuit = new JMenuItem("Quit");
            mntmQuit.addActionListener(e -> quit());
        }
        return mntmQuit;
    }

    private JMenuItem getMntmLoadSnapshot() {
        if (mntmLoadSnapshot == null) {
            mntmLoadSnapshot = new JMenuItem("Load Snapshot");
            mntmLoadSnapshot.addActionListener(e -> loadSnapshot());
        }
        return mntmLoadSnapshot;
    }

    private JMenuItem getMntmSaveSnapshot() {
        if (mntmSaveSnapshot == null) {
            mntmSaveSnapshot = new JMenuItem("Save Snapshot");
            mntmSaveSnapshot.addActionListener(e -> saveSnapshot());
        }
        return mntmSaveSnapshot;
    }

    private JMenuItem getMntmCreateFloppyDisk() {
        if (mntmCreateFloppyDisk == null) {
            mntmCreateFloppyDisk = new JMenuItem("Create empty harddrive image...");
            mntmCreateFloppyDisk.addActionListener(e -> createBlankDisk());
        }
        return mntmCreateFloppyDisk;
    }

    private JMenuItem getMntmCreateDiskFrom() {
        if (mntmCreateDiskFrom == null) {
            mntmCreateDiskFrom = new JMenuItem("Create harddrive from directory...");
            mntmCreateDiskFrom.addActionListener(e -> createDiskFromDirectory());
        }
        return mntmCreateDiskFrom;
    }

    private JMenuItem getMntmGettingStarted() {
        if (mntmGettingStarted == null) {
            mntmGettingStarted = new JMenuItem("Getting Started");
            mntmGettingStarted.addActionListener(e -> showGettingStarted());
        }
        return mntmGettingStarted;
    }

    private JMenuItem getMntmAboutJpc() {
        if (mntmAboutJpc == null) {
            mntmAboutJpc = new JMenuItem("About JPC");
            mntmAboutJpc.addActionListener(e -> showAboutJPC());
        }
        return mntmAboutJpc;
    }

    private JScrollPane getMonitorPane() {
        if (monitorPane == null) {
            monitorPane = new JScrollPane();
            monitorPane.setViewportView(monitor);
        }
        return monitorPane;
    }

    private JProgressBar getSpeedDisplay() {
        if (speedDisplay == null) {
            speedDisplay = new JProgressBar();
            speedDisplay.setPreferredSize(new Dimension(100, 20));
            speedDisplay.setStringPainted(true);
            speedDisplay.setString(" 0.00 Mhz");
        }
        return speedDisplay;
    }

    private JFileChooser getConfigFileChooser() {
        if (configFileChooser == null) {
            configFileChooser = new JFileChooser(System.getProperty("user.dir"));
            configFileChooser.setDialogTitle("Choose config file");
        }
        return configFileChooser;
    }

    private JFileChooser getSnapshotFileChooser() {
        if (snapshotFileChooser == null) {
            snapshotFileChooser = new JFileChooser(System.getProperty("user.dir"));
            snapshotFileChooser.setDialogTitle("Choose snapshot file");
        }
        return snapshotFileChooser;
    }

    private JFileChooser getImageFileChooser() {
        if (imageFileChooser == null) {
            imageFileChooser = new JFileChooser(System.getProperty("user.dir"));
            imageFileChooser.setDialogTitle("Choose image file");
        }
        return imageFileChooser;
    }

    private JFileChooser getDirectoryChooser() {
        if (directoryChooser == null) {
            directoryChooser = new JFileChooser(System.getProperty("user.dir"));
            directoryChooser.setDialogTitle("Choose directory");
        }
        return directoryChooser;
    }

    private JEditorPane getLicensePane() {
        if (licensePane == null) {
            final URL licence = getLicence();
            if (licence != null) {
                try {
                    licensePane = new JEditorPane(licence);
                } catch (IOException e) {
                    licensePane = new JEditorPane("text/html", LICENCE_HTML);
                }
            } else {
                licensePane = new JEditorPane("text/html", LICENCE_HTML);
            }
            licensePane.setEditable(false);
        }
        return licensePane;
    }

    private void start() {
        getMntmStart().setEnabled(false);

        monitor.startUpdateThread();
        pc.start();
        pcRunner.execute(this::run);

        getMntmStop().setEnabled(true);

        monitor.validate();
        monitor.requestFocus();
    }

    public void run() {
        pc.start();

        long markTime = System.currentTimeMillis();
        long execCount = COUNTDOWN;
        long totalExec = 0;
        try {
            while (pc.isRunning()) {
                execCount -= pc.execute();
                if (execCount > 0)
                    continue;
                totalExec += COUNTDOWN - execCount;
                execCount = COUNTDOWN;

                if (updateMHz(markTime, totalExec)) {
                    markTime = System.currentTimeMillis();
                    totalExec = 0;
                }
            }
        } finally {
            pc.stop();
            LOGGING.log(Level.INFO, "PC Stopped");
        }
    }

    private boolean updateMHz(long time, long count) {
        long t2 = System.currentTimeMillis();
        if (t2 - time < 100)
            return false;

        float mhz = count * 1000.0F / (t2 - time) / 1000000;

        getSpeedDisplay().setValue((int)(mhz / 1000));
        synchronized (TWO_DP) {
            getSpeedDisplay().setString(TWO_DP.format(mhz) + " MHz or " + THREE_DP.format(mhz / 1000) + " GHz Clock");
        }
        return true;
    }

    private void stop() {
        getMntmStop().setEnabled(false);

        pc.stop();
        monitor.stopUpdateThread();

        getMntmStart().setEnabled(true);
    }

    private void reset() {
        stop();
        pc.reset();
        start();
    }

    private void loadConfig() {
        if (getConfigFileChooser().showDialog(frame, "Load JPC Configuration") == JFileChooser.APPROVE_OPTION) {
            try {
                Option.loadConfig(getConfigFileChooser().getSelectedFile());
            } catch (IOException e) {
                LOGGING.log(Level.WARNING, "Exception loading configuration.", e);
            }
        }
    }

    private void saveConfig() {
        if (getConfigFileChooser().showDialog(frame, "Save JPC Configuration") == JFileChooser.APPROVE_OPTION) {
            try {
                Option.saveConfig(getConfigFileChooser().getSelectedFile());
            } catch (IOException e) {
                LOGGING.log(Level.WARNING, "Exception saving configuration.", e);
            }
        }
    }

    private void quit() {
        System.exit(0);
    }

    private void loadSnapshot() {
        int option = JOptionPane.showOptionDialog(frame,
            "Selecting a snapshot now will discard the current state of the emulated PC. Are you sure you want to continue?", "Warning",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] { "Continue", "Cancel" }, "Continue");

        if (option != 0) {
            return;
        }

        stop();

        if (getSnapshotFileChooser().showDialog(frame, "Load Snapshot") == JFileChooser.APPROVE_OPTION) {
            try {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(getSnapshotFileChooser().getSelectedFile()));
                zin.getNextEntry();
                pc.loadState(zin);
                zin.closeEntry();
                VGACard card = pc.getComponent(VGACard.class);
                card.setOriginalDisplaySize();
                zin.getNextEntry();
                monitor.loadState(zin);
                zin.closeEntry();
                zin.close();
            } catch (IOException e) {
                LOGGING.log(Level.SEVERE, "Exception during snapshot load", e);
            }
        }

        monitor.revalidate();
        monitor.requestFocus();
    }

    private void saveSnapshot() {
        stop();

        if (getSnapshotFileChooser().showDialog(frame, "Save JPC Snapshot") == JFileChooser.APPROVE_OPTION) {
            try {
                ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(getSnapshotFileChooser().getSelectedFile()));

                zip.putNextEntry(new ZipEntry("pc"));
                pc.saveState(zip);
                zip.closeEntry();

                zip.putNextEntry(new ZipEntry("monitor"));
                monitor.saveState(zip);
                zip.closeEntry();

                zip.finish();
                zip.close();
            } catch (IOException e) {
                LOGGING.log(Level.WARNING, "Exception saving snapshot.", e);
            }
        }

        start();
    }

    private void createBlankDisk() {
        try {
            if (getImageFileChooser().showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            String sizeString = JOptionPane.showInputDialog(frame, "Enter the size in MB for the disk", "Disk Image Creation",
                JOptionPane.QUESTION_MESSAGE);
            if (sizeString == null) {
                return;
            }
            long size = Long.parseLong(sizeString) * 1024L * 1024L;
            if (size < 0) {
                throw new Exception("Negative file size");
            }
            RandomAccessFile f = new RandomAccessFile(getImageFileChooser().getSelectedFile(), "rw");
            f.setLength(size);
            f.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to create blank disk " + e, "Create Disk", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createDiskFromDirectory() {
        try {
            if (getImageFileChooser().showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            getDirectoryChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (getDirectoryChooser().showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File out = getImageFileChooser().getSelectedFile();
            File root = getDirectoryChooser().getSelectedFile();
            if (!out.exists())
                out.createNewFile();
            TreeBlockDevice tbd = new TreeBlockDevice(root, true);
            DataOutput dataout = new DataOutputStream(new FileOutputStream(out));
            tbd.writeImage(dataout);
            System.out.println("Done saving disk image");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to create disk from directory" + e, "Create Disk", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showGettingStarted() {
        JFrame help1 = new JFrame("JPC - Getting Started");
        help1.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("icon.png")));
        help1.getContentPane().add("Center", new JScrollPane(getLicensePane()));
        help1.setBounds(300, 200, MONITOR_WIDTH + 20, MONITOR_HEIGHT - 70);
        help1.setVisible(true);
        frame.getContentPane().validate();
        frame.getContentPane().repaint();
    }

    private void showAboutJPC() {
        Object[] buttons = { "Visit our Website", "Ok" };
        if (JOptionPane.showOptionDialog(frame, ABOUT_US, "About JPC", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
            buttons, buttons[1]) == 0) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(JPC_URI);
                } catch (IOException e) {
                    LOGGING.log(Level.INFO, "Couldn't find or launch the default browser.", e);
                } catch (UnsupportedOperationException e) {
                    LOGGING.log(Level.INFO, "Browse action not supported.", e);
                } catch (SecurityException e) {
                    LOGGING.log(Level.INFO, "Browse action not permitted.", e);
                }
            }
        }
    }

    private URL getLicence() {
        return getClass().getResource("licence.html");
    }
}
