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

package org.jpc.emulator.pci;

/**
 * A <code>PCIDevice</code> object can be registered with a <code>PCIBus</code> object which will
 * configure it and organise the allocation of its resources.
 * @author Chris Dennis
 */
public interface PCIDevice {
    int PCI_ROM_SLOT = 6;
    int PCI_NUM_REGIONS = 7;

    int PCI_CONFIG_VENDOR_ID = 0x00;
    int PCI_CONFIG_DEVICE_ID = 0x02;
    int PCI_CONFIG_COMMAND = 0x04;
    int PCI_CONFIG_STATUS = 0x06;
    int PCI_CONFIG_REVISION = 0x08;
    int PCI_CONFIG_CLASS_DEVICE = 0x0a;
    int PCI_CONFIG_CLS = 0x0c;
    int PCI_CONFIG_LATENCY = 0x0d;
    int PCI_CONFIG_HEADER = 0x0e;
    int PCI_CONFIG_BIST = 0x0f;
    int PCI_CONFIG_BASE_ADDRESS = 0x10;
    int PCI_CONFIG_EXPANSION_ROM_BASE_ADDRESS = 0x30;
    int PCI_CONFIG_INTERRUPT_LINE = 0x3c;
    int PCI_CONFIG_INTERRUPT_PIN = 0x3d;
    int PCI_CONFIG_MIN_GNT = 0x3e;
    int PCI_CONFIG_MAX_LATENCY = 0x3f;
    int PCI_COMMAND_IO = 0x1;
    int PCI_COMMAND_MEMORY = 0x2;
    int PCI_HEADER_PCI_PCI_BRIDGE = 0x01;
    int PCI_HEADER_SINGLE_FUNCTION = 0x00;
    int PCI_HEADER_MULTI_FUNCTION = 0x80;

    //PCI Bus Registering

    /**
     * Returns this devices device/function number.
     * @return device/function number.
     */
    int getDeviceFunctionNumber();

    /**
     * Returns <code>true</code> if this device's device/function number can be chosen for it.
     * <p>
     * Some high-level devices (for example the PCI bridges) may have statically assigned device
     * numbers, they will return <code>false</code> here to prevent the bus from choosing a value for
     * them.
     * @return <code>true</code> if this devices number can be assigned by the PCI bus.
     */
    boolean autoAssignDeviceFunctionNumber();

    /**
     * Attempt to assign this device's device/function number.
     * <p>
     * Currently this may silently fail, decision must be made as to behaviour if this is called on say
     * the host bridge.
     * @param number new device/function number.
     */
    void assignDeviceFunctionNumber(int number);

    /**
     * Deassign this devices device/function number.
     * <p>
     * Currently this happens when a PCI device is removed. That can only happen if a new device
     * specifically requests the same device/function number.
     */
    void deassignDeviceFunctionNumber();

    /**
     * Writes a byte into this PCI devices configuration space.
     * <p>
     * The method should return <code>true</code> if this devices configuration has changed such that
     * it's resource mappings need updating.
     * <p>
     * Attempts to write into read-only or reserved locations in the configuration space of this device
     * using this method should silently fail.
     * @param address offset to write to.
     * @param data byte value to be written.
     * @return <code>true</code> if the device needs remapping.
     */
    boolean configWriteByte(int address, byte data);

    /**
     * Writes a word in little-endian format into this PCI devices configuration space.
     * <p>
     * The method should return <code>true</code> if this devices configuration has changed such that
     * it's resource mappings need updating.
     * <p>
     * Attempts to write into read-only or reserved locations in the configuration space of this device
     * using this method should silently fail.
     * @param address offset of the first byte to be written.
     * @param data short value to be written.
     * @return <code>true</code> if this device needs remapping.
     */
    boolean configWriteWord(int address, short data);

    /**
     * Writes a long in little-endian format into this PCI devices configuration space.
     * <p>
     * The method should return <code>true</code> if this devices configuration has changed such that
     * it's resource mappings need updating.
     * <p>
     * Attempts to write into read-only or reserved locations in the configuration space of this device
     * using this method should silently fail.
     * @param address offset of the first byte to be written.
     * @param data int value to be written.
     * @return <code>true</code> if this device needs remapping.
     */
    boolean configWriteLong(int address, int data);

    /**
     * Reads a byte from this PCI devices configuration space.
     * @param address offset to read from.
     * @return byte value read.
     */
    byte configReadByte(int address);

    /**
     * Reads a little-endian word from this PCI devices configuration space.
     * @param address offset of the first byte to be read.
     * @return short value read.
     */
    short configReadWord(int address);

    /**
     * Reads a little-endian long from this PCI devices configuration space.
     * @param address offset of the first byte to be read.
     * @return int value read.
     */
    int configReadLong(int address);

    /**
     * Forces the writes of a byte into this PCI devices configuration space.
     * <p>
     * This method writes directly into this PCI device's configuration space with no regard for
     * read-only or reserved locations.
     * @param address offset to write to.
     * @param data byte value to be written.
     */
    void putConfigByte(int address, byte data);

    /**
     * Forces the writes of a little-endian word into this PCI device's configuration space.
     * <p>
     * This method writes directly into this PCI device's configuration space with no regard for
     * read-only or reserved locations.
     * @param address offset of the first byte to be written.
     * @param data short value to be written.
     */
    void putConfigWord(int address, short data);

    /**
     * Forces the writes of a little-endian long into this PCI device's configuration space.
     * <p>
     * This method writes directly into this PCI device's configuration space with no regard for
     * read-only or reserved locations.
     * @param address offset of the first byte to be written.
     * @param data int value to be written.
     */
    void putConfigLong(int address, int data);

    /**
     * Returns a list of all of this PCI devices configurable <code>IORegions</code>
     * <p>
     * Note that the number of an <code>IORegion</code> in the returned array is not the same as the
     * <code>IORegion</code>'s region number.
     * @return device's set of <code>IORegions</code>
     */
    IORegion[] getIORegions();

    /**
     * Returns the <code>IORegion</code> with region number <code>number</code>
     * <p>
     * If a device has not region at that number then it will return <code>null</code>
     * <p>
     * Note that devices do not have to have a contiguous set of regions. Just because region
     * <code>n</code> is <code>null</code> does not mean that <code>n+1</code> will be also.
     * @param number <code>IORegion</code> number.
     * @return <code>IORegion</code> with number <code>number</code>.
     */
    IORegion getIORegion(int number);

    void setIRQIndex(int irqIndex);

    int getIRQIndex();

    void addIRQBouncer(IRQBouncer bouncer);

    IRQBouncer getIRQBouncer();
}
