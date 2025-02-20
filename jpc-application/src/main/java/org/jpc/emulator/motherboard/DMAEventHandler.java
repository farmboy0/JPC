package org.jpc.emulator.motherboard;

/**
 * Implemented by devices that can handle events associated with DMA transfers.
 * @author Ian Preston
 */
public interface DMAEventHandler {
    void handleDMAEvent(DMAEvent ev);
}
