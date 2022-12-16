package org.jpc.emulator.motherboard;

import java.util.HashMap;
import java.util.Map;

public class BochsPIT {
    public static Map<Integer, long[]> timings = new HashMap();

    private final long[] cycles;
    private int index = 0;
    private static BochsPIT instance = null;

    public BochsPIT(int ips) {
        if (!timings.containsKey(ips)) {
            cycles = null;
        } else
            cycles = timings.get(ips);
        instance = this;
    }

    public static boolean getIrqLevel() {
        return instance.getOut() == 1;
    }

    // must be called after getNextExpiry()
    public int getOut() {
        return index % 2;
    }

    public long getNextExpiry() {
        if (cycles == null) {
            index++;
            return Integer.MAX_VALUE;
        }
        return cycles[index++];
    }
}
