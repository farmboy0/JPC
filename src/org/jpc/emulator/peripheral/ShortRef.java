package org.jpc.emulator.peripheral;

public class ShortRef {
    public ShortRef() {
        this.value = 0;
    }

    public ShortRef(int value) {
        this.value = (short)value;
    }

    public ShortRef(short value) {
        this.value = value;
    }

    public short value;

    @Override
    public String toString() {
        throw new RuntimeException("Ooops");
    }
}
