package org.jpc.j2se;

import static org.jpc.emulator.DriveSet.DeviceSpec.BYTE_ARRAY;
import static org.jpc.emulator.block.BlockDevice.Type.CDROM;
import static org.jpc.emulator.block.BlockDevice.Type.FLOPPY;
import static org.jpc.emulator.block.BlockDevice.Type.HARDDRIVE;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jpc.emulator.DriveSet;
import org.jpc.emulator.PC;
import org.jpc.emulator.block.BlockDevice;
import org.jpc.emulator.block.BlockDevice.Type;
import org.jpc.emulator.block.CDROMBlockDevice;

public class DriveSetHandler {
    private static final Logger LOGGING = Logger.getLogger(DriveSetHandler.class.getName());

    private static final String IMAGES_PATH = "images/";

    private final PCMonitor monitor;
    private final Component parent;
    private final PC pc;
    private final DriveSet driveSet;
    private final JMenu driveSetMenu;
    private final JFileChooser diskImageChooser;

    public DriveSetHandler(PCMonitor monitor, Component parent) {
        this.monitor = monitor;
        this.parent = parent;
        this.pc = monitor.getPC();
        this.driveSet = pc.getDriveSet();
        this.driveSetMenu = createDriveSetMenu();
        this.diskImageChooser = createFileChooser();
    }

    public JMenu getDriveSetMenu() {
        return driveSetMenu;
    }

    private JMenu createDriveSetMenu() {
        final JMenu disks = new JMenu("Disks");

        for (int i = 0; i < 2; i++) {
            final int index = i;

            final JMenu floppyMenu = new JMenu();

            final JMenu included = new JMenu("Included Images");
            getIncludedImages().forEach(image -> {
                final ActionListener handler = new DriveChangeHandler(FLOPPY, index, () -> BYTE_ARRAY.getPrefix() + image,
                    pc::changeFloppyDisk, floppyMenu);
                included.add(image.substring(IMAGES_PATH.length())).addActionListener(handler);
            });
            floppyMenu.add(included);

            final JMenuItem custom = new JMenuItem("Choose Image...");
            final ActionListener customImageHandler = new DriveChangeHandler(FLOPPY, i, this::chooseImage, pc::changeFloppyDisk,
                floppyMenu);
            floppyMenu.add(custom).addActionListener(customImageHandler);

            final JMenuItem eject = new JMenuItem("Eject Image");
            eject.setEnabled(driveSet.getFloppyDrive(i) != null);
            final Runnable ejectHandler = () -> {
                pc.changeFloppyDisk(null, index);
                eject.setEnabled(driveSet.getFloppyDrive(index) != null);
            };
            final ActionListener ejectImageHandler = new EjectDriveHandler(FLOPPY, i, ejectHandler, floppyMenu);
            floppyMenu.add(eject).addActionListener(ejectImageHandler);

            disks.add(floppyMenu);
        }

        for (int i = 0; i < 4; i++) {
            final int index = i;
            final Type type = driveSet.getHardDrive(i) instanceof CDROMBlockDevice ? CDROM : HARDDRIVE;

            final JMenu hdMenu = new JMenu();

            final JMenu included = new JMenu("Included Images");
            getIncludedImages().forEach(image -> {
                final ActionListener handler = new DriveChangeHandler(type, index, () -> BYTE_ARRAY.getPrefix() + image,
                    this::changeHardrive, hdMenu);
                included.add(image.substring(IMAGES_PATH.length())).addActionListener(handler);
            });
            hdMenu.add(included);

            final JMenuItem custom = new JMenuItem("Choose Image...");
            final ActionListener customImageHandler = new DriveChangeHandler(type, index, this::chooseImage, this::changeHardrive, hdMenu);
            hdMenu.add(custom).addActionListener(customImageHandler);

            JMenuItem directory = new JMenuItem("Choose Directory...");
            final ActionListener directoryHandler = new DriveChangeHandler(type, index, this::chooseDirectory, this::changeHardrive,
                hdMenu);
            hdMenu.add(directory).addActionListener(directoryHandler);

            if (CDROM.equals(type)) {
                final JMenuItem eject = new JMenuItem("Eject Image");
                eject.setEnabled(((CDROMBlockDevice)driveSet.getHardDrive(i)).isInserted());
                final Runnable ejectHandler = () -> {
                    ((CDROMBlockDevice)driveSet.getHardDrive(index)).eject();
                    eject.setEnabled(((CDROMBlockDevice)driveSet.getHardDrive(index)).isInserted());
                };
                final ActionListener ejectImageHandler = new EjectDriveHandler(CDROM, index, ejectHandler, hdMenu);
                hdMenu.add(eject).addActionListener(ejectImageHandler);
            }

            disks.add(hdMenu);
        }

        return disks;
    }

    private JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        return chooser;
    }

    private static final SortedSet<String> getIncludedImages() {
        final SortedSet<String> resources = new TreeSet<>();

        final InputStream stream = ClassLoader.getSystemResourceAsStream(IMAGES_PATH);
        try (final Scanner scanner = new Scanner(stream).useDelimiter(System.lineSeparator())) {
            while (scanner.hasNext()) {
                String next = scanner.next();
                if (ClassLoader.getSystemResource(IMAGES_PATH + next) != null) {
                    resources.add(IMAGES_PATH + next);
                }
            }
        }

        return resources;
    }

    private String chooseImage() {
        diskImageChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = diskImageChooser.showDialog(parent, "Assign Image");
        if (result != JFileChooser.APPROVE_OPTION)
            return null;
        return diskImageChooser.getSelectedFile().getAbsolutePath();
    }

    private String chooseDirectory() {
        diskImageChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = diskImageChooser.showDialog(parent, "Use Directory");
        if (result != JFileChooser.APPROVE_OPTION)
            return null;
        return diskImageChooser.getSelectedFile().getAbsolutePath();
    }

    private void changeHardrive(BlockDevice blockDevice, int index) {
        final boolean isRunning = pc.isRunning();
        if (isRunning)
            pc.stop();
        driveSet.setHardDrive(index, blockDevice);
        if (isRunning) {
            pc.reset();
            monitor.revalidate();
            monitor.requestFocus();
        }
    }

    private class DriveChangeHandler implements ActionListener {
        protected final Type driveType;
        protected final int driveIndex;
        protected final Supplier<String> driveSpecSupplier;
        protected final ObjIntConsumer<BlockDevice> bdChangeHandler;
        protected final JMenu driveMenu;

        public DriveChangeHandler(Type driveType, int driveIndex, Supplier<String> driveSpecSupplier,
            ObjIntConsumer<BlockDevice> bdChangeHandler, JMenu driveMenu) {

            this.driveType = driveType;
            this.driveIndex = driveIndex;
            this.driveSpecSupplier = driveSpecSupplier;
            this.bdChangeHandler = bdChangeHandler;
            this.driveMenu = driveMenu;
            update();
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            final String driveSpec = driveSpecSupplier.get();
            if (driveSpec == null) {
                return;
            }
            BlockDevice blockDevice = null;
            try {
                blockDevice = createBlockDevice(driveSpec);
            } catch (IOException e) {
                LOGGING.log(Level.WARNING, "Exception changing " + driveName() + " to " + driveSpec, e);
            }
            bdChangeHandler.accept(blockDevice, driveIndex);
            update();
        }

        protected BlockDevice createBlockDevice(String driveSpec) throws IOException {
            return DriveSet.DeviceSpec.createFrom(driveType, driveSpec);
        }

        protected String driveName() {
            return (Type.FLOPPY.equals(driveType) ? "FD" : "HD") + driveIndex;
        }

        protected BlockDevice currentDevice() {
            return Type.FLOPPY.equals(driveType) ? driveSet.getFloppyDrive(driveIndex) : driveSet.getHardDrive(driveIndex);
        }

        protected void update() {
            final BlockDevice blockDevice = currentDevice();
            driveMenu.setText(driveName() + " " + (blockDevice != null ? blockDevice.toString() : "[none]"));
        }
    }

    private class EjectDriveHandler extends DriveChangeHandler {
        private final Runnable ejectHandler;

        public EjectDriveHandler(Type driveType, int driveIndex, Runnable ejectHandler, JMenu driveMenu) {
            super(driveType, driveIndex, null, null, driveMenu);
            this.ejectHandler = ejectHandler;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            ejectHandler.run();
            update();
        }
    }
}
