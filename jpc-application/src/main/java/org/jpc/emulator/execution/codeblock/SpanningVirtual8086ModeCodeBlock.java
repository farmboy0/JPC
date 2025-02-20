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

package org.jpc.emulator.execution.codeblock;

import org.jpc.emulator.memory.AddressSpace;
import org.jpc.emulator.processor.Processor;

/**
 * @author Chris Dennis
 */
class SpanningVirtual8086ModeCodeBlock extends SpanningCodeBlock implements Virtual8086ModeCodeBlock {
    private PeekableMemoryStream byteSourceStream = new PeekableMemoryStream();

    private CodeBlockFactory[] factories;

    public SpanningVirtual8086ModeCodeBlock(CodeBlockFactory[] factories) {
        this.factories = factories;
    }

    @Override
    public CodeBlock decode(Processor cpu) {
        Virtual8086ModeCodeBlock block = null;
        AddressSpace memory = cpu.linearMemory;
        int address = cpu.getInstructionPointer();
        for (int i = 0; i < factories.length && block == null; i++) {
            try {
                byteSourceStream.set(memory, address);
                block = factories[i].getVirtual8086ModeCodeBlock(byteSourceStream);
            } catch (IllegalStateException e) {
            }
        }

        byteSourceStream.set(null, 0);
        return block;
    }

    @Override
    public String toString() {
        return "Spanning Virtual8086 Mode CodeBlock";
    }
}
