package org.jpc.j2se;

import java.awt.event.KeyEvent;

public class KeyboardKey {
    private final int location;
    private final int code;

    public KeyboardKey(KeyEvent event) {
        this(event.getKeyLocation(), event.getKeyCode() == 0 ? event.getExtendedKeyCode() : event.getKeyCode());
    }

    public KeyboardKey(int code) {
        this.location = KeyEvent.KEY_LOCATION_UNKNOWN;
        this.code = code;
    }

    public KeyboardKey(int location, int code) {
        this.location = location;
        this.code = code;
    }

    public int getLocation() {
        return location;
    }

    public int getCode() {
        return code;
    }

    @Override
    public int hashCode() {
        return 255 * getLocation() + getCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        KeyboardKey other = (KeyboardKey)obj;
        return getLocation() == other.getLocation() && getCode() == other.getCode();
    }
}
