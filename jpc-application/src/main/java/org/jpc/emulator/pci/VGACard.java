package org.jpc.emulator.pci;

import java.awt.Dimension;
import java.awt.Graphics2D;

import org.jpc.emulator.HardwareComponent;
import org.jpc.emulator.Monitor;
import org.jpc.emulator.motherboard.IODevice;

public interface VGACard extends HardwareComponent, IODevice, PCIDevice {
    int[] getDisplayBuffer();

    Dimension getDisplaySize();

    String getText();

    void paintOnMonitor(Graphics2D g);

    void prepareUpdate();

    void resizeDisplay(int width, int height);

    void setMonitor(Monitor monitor);

    void setOriginalDisplaySize();

    void updateDisplay();
}
