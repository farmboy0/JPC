package org.jpc.emulator;

import java.awt.Dimension;

public interface Monitor {
    Dimension getSize();

    void resizeDisplay(int width, int height);
}
