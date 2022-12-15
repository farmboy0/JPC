/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine

    Copyright (C) 2012-2013 Ian Preston

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

    End of licence header
*/

package org.jpc.emulator.execution.codeblock;

import org.jpc.emulator.execution.decoder.PeekableInputStream;
import org.jpc.emulator.memory.Memory;

/**
 * @author Ian Preston
 */
public class PeekableMemoryStream implements PeekableInputStream {
    private Memory memory;
    private int position, start;

    public void set(Memory source, int offset) {
        memory = source;
        position = offset;
        start = offset;
    }

    @Override
    public void seek(int delta) {
        position += delta;
    }

    @Override
    public int peek() {
        return 0xFF & memory.getByte(position);
    }

    @Override
    public void forward() {
        position++;
    }

    public long position() {
        return position;
    }

    @Override
    public long readU(long bits) {
        if (bits == 8)
            return 0xFF & memory.getByte(position++);
        if (bits == 16)
            return read16();
        if (bits == 32)
            return read32();
        if (bits == 64)
            return read32() | (long)read32() << 32;
        throw new IllegalStateException("unimplemented read amount " + bits);
    }

    @Override
    public byte read8() {
        return memory.getByte(position++);
    }

    @Override
    public short read16() {
        return (short)(readU8() | read8() << 8);
    }

    @Override
    public int read32() {
        return readU16() | read16() << 16;
    }

    @Override
    public int readU8() {
        return 0xFF & memory.getByte(position++);
    }

    @Override
    public int readU16() {
        return 0xFF & memory.getByte(position++) | (0xFF & memory.getByte(position++)) << 8;
    }

    @Override
    public long readU32() {
        return readU16() | readU16() << 16;
    }

    @Override
    public long getAddress() {
        return position;
    }

    @Override
    public int getCounter() {
        return position - start;
    }

    @Override
    public void resetCounter() {
        start = position;
    }

    @Override
    public String toString() {
        return "PeekableMemoryStream: [" + memory + "] @ 0x" + Integer.toHexString(start);
    }
}
