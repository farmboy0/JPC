package org.jpc.emulator.motherboard;

public interface IODevice {

    void ioPortWrite8(int address, int data);

    void ioPortWrite16(int address, int data);

    void ioPortWrite32(int address, int data);

    int ioPortRead8(int address);

    int ioPortRead16(int address);

    int ioPortRead32(int address);

    int[] ioPortsRequested();
}
