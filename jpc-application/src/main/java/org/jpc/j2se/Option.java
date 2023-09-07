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

package org.jpc.j2se;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class Option {
    private static final Map<String, Option> names2options = new HashMap<>();

    public static final Opt config = new Opt("config"); // This one is special...

    public static final Switch track_writes = new Switch("track-writes"); // needed to use -mem with compare
    public static final Switch debug_blocks = new Switch("debug-blocks");
    public static final Switch log_disam = new Switch("log-disam");
    public static final Switch log_disam_addresses = new Switch("log-disam-addresses");
    public static final Switch log_state = new Switch("log-state");
    public static final Switch log_blockentry = new Switch("log-block-entry");
    public static final Switch log_memory_maps = new Switch("log-memory-maps");
    public static final Switch compile = new Switch("compile");
    public static final Switch fullscreen = new Switch("fullscreen");
    public static final Switch history = new Switch("history");
    public static final Switch useBochs = new Switch("bochs");

    public static final Switch printCHS = new Switch("printCHS");
    public static final Switch help = new Switch("help");
    public static final Opt min_addr_watch = new Opt("min-addr-watch");
    public static final Opt max_addr_watch = new Opt("max-addr-watch");

    // required for deterministic execution
    public static final Switch deterministic = new Switch("deterministic");
    public static final Opt startTime = new Opt("start-time");
    public static final Switch noScreen = new Switch("no-screen");

    public static final Opt ss = new Opt("ss");
    public static final Opt ram = new Opt("ram");
    public static final Opt ips = new Opt("ips");
    public static final Opt cpulevel = new Opt("cpulevel");
    public static final Opt timeslowdown = new Opt("time-slowdown");
    public static final Switch singlesteptime = new Switch("single-step-time");
    public static final Opt max_instructions_per_block = new Opt("max-block-size");
    public static final Opt boot = new Opt("boot");
    public static final Opt fda = new Opt("fda");
    public static final Opt fdb = new Opt("fdb");
    public static final Opt hda = new Opt("hda");
    public static final Opt hdb = new Opt("hdb");
    public static final Opt hdc = new Opt("hdc");
    public static final Opt hdd = new Opt("hdd");
    public static final Opt cdrom = new Opt("cdrom");
    public static final Opt bios = new Opt("bios");
    public static final Switch ethernet = new Switch("ethernet");
    public static final Opt port = new Opt("port");
    public static final Opt net = new Opt("net");

    public static final Switch sound = new Switch("sound");
    public static final Opt sounddevice = new Opt("sounddevice");
    public static final Opt mixer_rate = new Opt("mixer_rate");
    public static final Opt mixer_javabuffer = new Opt("mixer_java-buffer");
    public static final Opt mixer_blocksize = new Opt("mixer_block-size");
    public static final Switch mixer_nosound = new Switch("mixer_no-sound");
    public static final Opt mixer_prebuffer = new Opt("mixer_prebuffer");

    public static final Opt mpu401 = new Opt("mpu401");
    public static final Opt mididevice = new Opt("midi-device");
    public static final Opt midiconfig = new Opt("midi-config");

    public static final Opt sbbase = new Opt("sbbase");
    public static final Opt sb_irq = new Opt("sb_irq");
    public static final Opt sb_dma = new Opt("sb_dma");
    public static final Opt sb_hdma = new Opt("sb_hdma");
    public static final Switch sbmixer = new Switch("sbmixer");
    public static final Opt sbtype = new Opt("sbtype");
    public static final Opt oplemu = new Opt("oplemu");
    public static final Opt oplrate = new Opt("oplrate");

    private final String name;
    private boolean set;

    protected Option(String name) {
        this.name = name;
        names2options.put(name, this);
    }

    public String getName() {
        return name;
    }

    public boolean isSet() {
        return set;
    }

    public static void printHelp() {
        System.out.println("JPC Help");
        System.out.println("Parameters may be specified on the command line or in a file. ");
        System.out.println();
        System.out.println("-help - display this help");
        System.out
            .println("-config $file - read parameters from $file, any subsequent commandline parameters override parameters in the file");
        System.out.println("-boot $device - the device to boot from out of fda (floppy), hda (hard drive 1), cdrom (CDROM drive)");
        System.out.println("-fda $file - floppy image file");
        System.out.println("-hda $file - hard disk image file");
        System.out.println("-hda dir:$dir - directory to mount as a FAT32 hard disk");
        System.out.println("-ss $file - snapshot file to load");
        System.out.println("-ram $megabytes - the amount RAM the virtual machine should have");
        System.out.println(
            "-ips $number - number of emulated instructions per emulated second - a larger value will cause a slower apparent time in the VM");
        System.out.println("-cpulevel $number - 4 = 486, 5 = Pentium, 6 = Pentium Pro");
        System.out.println();
        System.out.println("-sound - enable sound");
        System.out.println();
        System.out.println("Advanced Options:");
        System.out.println("-bios - specify an alternate bios image");
        System.out.println(
            "-max-block-size $num - maximum number of instructions per basic block (A value of 1 will still have some blocks of length 2 due to mov ss,X, pop ss and sti)");
    }

    public static void parse(String[] source) throws IOException {
        boolean parseConfig = false;
        for (int index = 0; index < source.length; index++) {
            String arg = source[index];
            if (!arg.startsWith("-")) {
                throw new IllegalArgumentException("Error parsing arguments at option " + arg);
            }
            Option option = names2options.get(arg.substring(1));
            if (option == null) {
                throw new IllegalArgumentException("Error parsing arguments at option " + arg);
            } else if (!option.isSet()) {
                option.set = true;
                index = option.update(source, index);
                parseConfig |= config == option;
            }
        }
        if (parseConfig)
            loadConfig(config.value());
    }

    public static void loadConfig(String file) throws IOException {
        loadConfig(new File(file));
    }

    public static void loadConfig(File f) throws IOException {
        StringBuilder b = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                b.append(line + " ");
            }
            parse(b.toString().split(" "));
        }
    }

    public static void saveConfig(File f) throws IOException {
        String conf = saveConfig();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            w.write(conf);
            w.flush();
        }
    }

    public static String saveConfig() {
        StringBuilder sb = new StringBuilder();
        for (Option option : names2options.values()) {
            if (!option.isSet())
                continue;
            sb.append("-");
            sb.append(option.getName());
            if (option instanceof Opt o) {
                sb.append(" ");
                sb.append(o.value());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    protected abstract int update(String[] args, int index);

    public static class OptSet extends Option {
        private Set<String> values = new LinkedHashSet<>();

        public OptSet(String name, String... defaults) {
            super(name);
            values.addAll(Arrays.asList(defaults));
        }

        public String[] values() {
            return values.toArray(new String[0]);
        }

        @Override
        protected int update(String[] args, int index) {
            String value = args[++index];
            values.add(value);
            return index;
        }

        public void remove(String value) {
            values.remove(value);
        }
    }

    public static class Opt extends Option {
        private String value;

        public Opt(String name) {
            super(name);
        }

        public void set(String value) {
            this.value = value;
            super.set = true;
        }

        @Override
        public int update(String[] args, int index) {
            this.value = args[++index];
            return index;
        }

        public int intValue(int defaultValue) {
            if (value != null) {
                return Integer.parseInt(value.trim());
            } else {
                return defaultValue;
            }
        }

        public int intValue(int defaultValue, int radix) {
            if (value != null) {
                return (int)Long.parseLong(value.trim(), radix);
            } else {
                return defaultValue;
            }
        }

        public long longValue(long defaultValue, int radix) {
            if (value != null) {
                return Long.parseLong(value.trim(), radix);
            } else {
                return defaultValue;
            }
        }

        public double doubleValue(double defaultValue) {
            if (value != null) {
                return Double.parseDouble(value.trim());
            } else {
                return defaultValue;
            }
        }

        public String value(String defaultValue) {
            if (value != null) {
                return value;
            } else {
                return defaultValue;
            }
        }

        public String value() {
            return value;
        }
    }

    public static class Switch extends Option {
        private boolean value;

        public Switch(String name) {
            super(name);
        }

        public void set(boolean value) {
            this.value = value;
            super.set = true;
        }

        @Override
        public int update(String[] args, int index) {
            value = true;
            return index; // No Arguments
        }

        public boolean value() {
            return value;
        }
    }

    public static class Select extends Option {
        private final Map<String, Object> values = new LinkedHashMap<>();
        private String key;
        private final String defaultValue;

        public Select(String name, String defaultValue) {
            super(name);
            this.key = defaultValue;
            this.defaultValue = defaultValue;
        }

        public Select entry(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public void set(Object value) {
            key = null;
            super.set = true;
            if (value == null) {
                return;
            }
            for (Map.Entry<String, Object> e : values.entrySet()) {
                if (value.equals(e.getValue())) {
                    key = e.getKey();
                }
            }
        }

        @Override
        public int update(String[] args, int index) {
            this.key = args[++index];
            return index;
        }

        public Object value() {
            return values.containsKey(key) ? values.get(key) : values.get(defaultValue);
        }
    }
}
