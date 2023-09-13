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

package org.jpc.emulator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import org.jpc.emulator.block.BlockDevice;
import org.jpc.emulator.block.BlockDevice.Type;
import org.jpc.emulator.block.CDROMBlockDevice;
import org.jpc.emulator.block.FloppyBlockDevice;
import org.jpc.emulator.block.HDBlockDevice;
import org.jpc.emulator.block.TreeBlockDevice;
import org.jpc.emulator.block.backing.ArrayBackedSeekableIODevice;
import org.jpc.emulator.block.backing.CachingSeekableIODevice;
import org.jpc.emulator.block.backing.FileBackedSeekableIODevice;
import org.jpc.emulator.block.backing.RemoteSeekableIODevice;
import org.jpc.emulator.block.backing.SeekableIODevice;
import org.jpc.j2se.Option;

/**
 * Represents the set of disk drive devices associated with this emulator instance.
 * @author Chris Dennis
 */
public class DriveSet extends AbstractHardwareComponent {
    public enum DeviceSpec {
        DEFAULT("default:", FileBackedSeekableIODevice::new), //
        CACHING("caching:", spec -> {
            final String subspec = spec.substring(8);
            final SeekableIODevice seekableIODevice = deviceSpecFrom(subspec).ioDeviceFrom(subspec);
            return new CachingSeekableIODevice(seekableIODevice);
        }), //
        DIRECTORY("dir:", new DirectoryDeviceSupplier()), //
        BYTE_ARRAY("mem:", spec -> new ArrayBackedSeekableIODevice(spec.substring(4))), //
        REMOTE("net:", spec -> new RemoteSeekableIODevice(spec.substring(4)));

        private final String prefix;
        private final DeviceSupplier deviceSupplier;

        DeviceSpec(String prefix, DeviceSupplier deviceSupplier) {
            this.prefix = prefix;
            this.deviceSupplier = deviceSupplier;
        }

        public static BlockDevice createFrom(BlockDevice.Type type, String spec) throws IOException {
            if (spec == null)
                return null;
            if (spec.indexOf("\"") == 0 && spec.indexOf("\"", 1) > 0)
                spec = spec.substring(1, spec.length() - 2);
            return deviceSpecFrom(spec).deviceSupplier.create(type, spec);
        }

        public static void changeTo(CDROMBlockDevice device, String spec) throws IOException {
            if (spec == null) {
                device.eject();
                return;
            }
            if (spec.indexOf("\"") == 0 && spec.indexOf("\"", 1) > 0)
                spec = spec.substring(1, spec.length() - 2);
            device.insert(deviceSpecFrom(spec).ioDeviceFrom(spec));
        }

        private static DeviceSpec deviceSpecFrom(String spec) {
            for (DeviceSpec deviceSpec : values()) {
                if (spec.startsWith(deviceSpec.prefix)) {
                    return deviceSpec;
                }
            }
            return DEFAULT;
        }

        public String getPrefix() {
            return prefix;
        }

        SeekableIODevice ioDeviceFrom(String spec) throws IOException {
            return deviceSupplier.ioDeviceFrom(spec);
        }

        private static class DirectoryDeviceSupplier implements DeviceSupplier {
            @Override
            public SeekableIODevice ioDeviceFrom(String spec) throws IOException {
                throw new IOException("Can't create SeekableIODevice from " + spec);
            }

            @Override
            public BlockDevice create(Type type, String spec) throws IOException {
                return new TreeBlockDevice(spec.substring(4));
            }
        }

        private interface DeviceSupplier {
            SeekableIODevice ioDeviceFrom(String spec) throws IOException;

            default BlockDevice create(BlockDevice.Type type, String spec) throws IOException {
                return create(type, ioDeviceFrom(spec));
            }

            default BlockDevice create(BlockDevice.Type type, SeekableIODevice seekableIODevice) throws IOException {
                switch (type) {
                case FLOPPY:
                    return new FloppyBlockDevice(seekableIODevice);
                case CDROM:
                    return new CDROMBlockDevice(seekableIODevice);
                case HARDDRIVE:
                    return new HDBlockDevice(seekableIODevice);
                default:
                    return null;
                }
            }
        }
    }

    private BlockDevice.Type bootType;
    private BlockDevice[] floppies = new BlockDevice[2];
    private BlockDevice[] ides = new BlockDevice[4];

    public DriveSet() throws IOException {
        floppies[0] = DeviceSpec.createFrom(BlockDevice.Type.FLOPPY, Option.fda.value());
        floppies[1] = DeviceSpec.createFrom(BlockDevice.Type.FLOPPY, Option.fdb.value());

        ides[0] = DeviceSpec.createFrom(BlockDevice.Type.HARDDRIVE, Option.hda.value());
        ides[1] = DeviceSpec.createFrom(BlockDevice.Type.HARDDRIVE, Option.hdb.value());
        if (Option.cdrom.isSet())
            ides[2] = DeviceSpec.createFrom(BlockDevice.Type.CDROM, Option.cdrom.value());
        else
            ides[2] = DeviceSpec.createFrom(BlockDevice.Type.HARDDRIVE, Option.hdc.value());
        if (ides[2] == null) {
            ides[2] = new CDROMBlockDevice();
        }
        ides[3] = DeviceSpec.createFrom(BlockDevice.Type.HARDDRIVE, Option.hdd.value());

        bootType = null;
        String bootArg = Option.boot.value();
        if ("fda".equalsIgnoreCase(bootArg))
            bootType = BlockDevice.Type.FLOPPY;
        else if ("hda".equalsIgnoreCase(bootArg))
            bootType = BlockDevice.Type.HARDDRIVE;
        else if ("cdrom".equalsIgnoreCase(bootArg))
            bootType = BlockDevice.Type.CDROM;
        else if (ides[0] != null)
            bootType = BlockDevice.Type.HARDDRIVE;
        else if (ides[2] instanceof CDROMBlockDevice)
            bootType = BlockDevice.Type.CDROM;
        if (bootType == null) {
            throw new IllegalStateException("cannot determine boot device.");
        }
    }

    /**
     * Returns the i'th hard drive device.
     * <p>
     * Devices are numbered from 0 to 3 inclusive in order: primary master, primary slave, secondary
     * master, secondary slave.
     * @param index drive index
     * @return hard drive block device
     */
    public BlockDevice getHardDrive(int index) {
        return ides[index];
    }

    public void setHardDrive(int index, BlockDevice device) {
        ides[index] = device;
    }

    /**
     * Returns the i'th floppy drive device.
     * <p>
     * The drives are numbered sequentially A:, B:.
     * @param index floppy drive index
     * @return floppy drive block device
     */
    public BlockDevice getFloppyDrive(int index) {
        return floppies[index];
    }

    public void setFloppyDrive(int index, BlockDevice device) {
        floppies[index] = device;
    }

    /**
     * Returns the current boot device as determined by the boot type parameter.
     * @return boot block device
     */
    public BlockDevice getBootDevice() {
        switch (bootType) {
        case FLOPPY:
            return floppies[0];
        case CDROM:
            return ides[2];
        case HARDDRIVE:
            return ides[0];
        default:
            return null;
        }
    }

    public void setBootType(BlockDevice.Type bootType) {
        Objects.requireNonNull(bootType);
        this.bootType = bootType;
    }

    /**
     * Returns the boot type being used by this driveset.
     * @return boot type
     */
    public BlockDevice.Type getBootType() {
        return bootType;
    }

    @Override
    public void saveState(DataOutput output) throws IOException {
        // TODO
    }

    @Override
    public void loadState(DataInput input) throws IOException {
        // TODO
    }

    public void close() {
        for (BlockDevice d : ides)
            if (d != null)
                d.close();
        for (BlockDevice d : floppies)
            if (d != null)
                d.close();
    }
}
