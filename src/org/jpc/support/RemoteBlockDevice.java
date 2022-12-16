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

package org.jpc.support;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Ian Preston
 */
public class RemoteBlockDevice implements BlockDevice {
    enum Protocol {
        READ, WRITE, TOTAL_SECTORS, CYLINDERS, HEADS, SECTORS, TYPE, INSERTED, LOCKED, READ_ONLY, SET_LOCKED, CLOSE;
    }

    private DataInputStream in;
    private DataOutputStream out;

    @Override
    public void configure(String spec) throws IOException {
        String server = spec;
        int port = 6666;
        int colon = spec.indexOf(':');
        if (colon >= 0) {
            port = Integer.parseInt(spec.substring(colon + 1));
            server = spec.substring(0, colon);
        }

        Socket sock = new Socket(server, port);
        this.in = new DataInputStream(sock.getInputStream());
        this.out = new DataOutputStream(sock.getOutputStream());

    }

    public RemoteBlockDevice() {
    }

    public RemoteBlockDevice(InputStream in, OutputStream out) {
        this.in = new DataInputStream(in);
        this.out = new DataOutputStream(out);
    }

    @Override
    public synchronized void close() {
        try {
            out.write(Protocol.CLOSE.ordinal());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized int read(long sectorNumber, byte[] buffer, int size) {
        try {
            out.write(Protocol.READ.ordinal());
            out.writeLong(sectorNumber);
            out.writeInt(size);
            out.flush();

            if (in.read() != 0)
                throw new IOException("Read failed");

            int result = in.readInt();
            int toRead = in.readInt();
            in.read(buffer, 0, toRead);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public synchronized int write(long sectorNumber, byte[] buffer, int size) {
        try {
            out.write(Protocol.WRITE.ordinal());
            out.writeLong(sectorNumber);
            out.writeInt(size * 512);
            out.write(buffer, 0, size * 512);
            out.flush();

            if (in.read() != 0)
                throw new IOException("Write failed");

            return in.readInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public synchronized boolean isInserted() {
        try {
            out.write(Protocol.INSERTED.ordinal());
            out.flush();

            return in.readBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized boolean isLocked() {
        try {
            out.write(Protocol.LOCKED.ordinal());
            out.flush();

            return in.readBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized boolean isReadOnly() {
        try {
            out.write(Protocol.READ_ONLY.ordinal());
            out.flush();

            return in.readBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized void setLock(boolean locked) {
        try {
            out.write(Protocol.SET_LOCKED.ordinal());
            out.writeBoolean(locked);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized long getTotalSectors() {
        try {
            out.write(Protocol.TOTAL_SECTORS.ordinal());
            out.flush();

            return in.readLong();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public synchronized int getCylinders() {
        try {
            out.write(Protocol.CYLINDERS.ordinal());
            out.flush();

            return in.readInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public synchronized int getHeads() {
        try {
            out.write(Protocol.HEADS.ordinal());
            out.flush();

            return in.readInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public synchronized int getSectors() {
        try {
            out.write(Protocol.SECTORS.ordinal());
            out.flush();

            return in.readInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public synchronized Type getType() {
        try {
            out.write(Protocol.TYPE.ordinal());
            out.flush();

            int result = in.readInt();
            return Type.values()[result];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
