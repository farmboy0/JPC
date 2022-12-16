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

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chris Dennis
 */
public class KeyMapping {
    private static Map<Integer, Byte> scancodeTable = new HashMap<Integer, Byte>();

    static {
        scancodeTable.put(KeyEvent.VK_ESCAPE, (byte)0x01);
        scancodeTable.put(KeyEvent.VK_1, (byte)0x02);
        scancodeTable.put(KeyEvent.VK_2, (byte)0x03);
        scancodeTable.put(KeyEvent.VK_3, (byte)0x04);
        scancodeTable.put(KeyEvent.VK_4, (byte)0x05);
        scancodeTable.put(KeyEvent.VK_5, (byte)0x06);
        scancodeTable.put(KeyEvent.VK_6, (byte)0x07);
        scancodeTable.put(KeyEvent.VK_7, (byte)0x08);
        scancodeTable.put(KeyEvent.VK_8, (byte)0x09);
        scancodeTable.put(KeyEvent.VK_9, (byte)0x0a);
        scancodeTable.put(KeyEvent.VK_0, (byte)0x0b);
        scancodeTable.put(KeyEvent.VK_MINUS, (byte)0x0c);
        scancodeTable.put(KeyEvent.VK_EQUALS, (byte)0x0d);
        scancodeTable.put(KeyEvent.VK_BACK_SPACE, (byte)0x0e);

        scancodeTable.put(KeyEvent.VK_TAB, (byte)0xf);
        scancodeTable.put(KeyEvent.VK_Q, (byte)0x10);
        scancodeTable.put(KeyEvent.VK_W, (byte)0x11);
        scancodeTable.put(KeyEvent.VK_E, (byte)0x12);
        scancodeTable.put(KeyEvent.VK_R, (byte)0x13);
        scancodeTable.put(KeyEvent.VK_T, (byte)0x14);
        scancodeTable.put(KeyEvent.VK_Y, (byte)0x15);
        scancodeTable.put(KeyEvent.VK_U, (byte)0x16);
        scancodeTable.put(KeyEvent.VK_I, (byte)0x17);
        scancodeTable.put(KeyEvent.VK_O, (byte)0x18);
        scancodeTable.put(KeyEvent.VK_P, (byte)0x19);
        scancodeTable.put(KeyEvent.VK_OPEN_BRACKET, (byte)0x1a);
        scancodeTable.put(KeyEvent.VK_CLOSE_BRACKET, (byte)0x1b);

        scancodeTable.put(KeyEvent.VK_ENTER, (byte)0x1c);

        scancodeTable.put(KeyEvent.VK_CONTROL, (byte)0x1d);

        scancodeTable.put(KeyEvent.VK_A, (byte)0x1e);
        scancodeTable.put(KeyEvent.VK_S, (byte)0x1f);
        scancodeTable.put(KeyEvent.VK_D, (byte)0x20);
        scancodeTable.put(KeyEvent.VK_F, (byte)0x21);
        scancodeTable.put(KeyEvent.VK_G, (byte)0x22);
        scancodeTable.put(KeyEvent.VK_H, (byte)0x23);
        scancodeTable.put(KeyEvent.VK_J, (byte)0x24);
        scancodeTable.put(KeyEvent.VK_K, (byte)0x25);
        scancodeTable.put(KeyEvent.VK_L, (byte)0x26);
        scancodeTable.put(KeyEvent.VK_SEMICOLON, (byte)0x27);
        scancodeTable.put(KeyEvent.VK_QUOTE, (byte)0x28);

        scancodeTable.put(KeyEvent.VK_BACK_QUOTE, (byte)0x29);

        scancodeTable.put(KeyEvent.VK_SHIFT, (byte)0x2a);

        scancodeTable.put(KeyEvent.VK_BACK_SLASH, (byte)0x2b);

        scancodeTable.put(KeyEvent.VK_Z, (byte)0x2c);
        scancodeTable.put(KeyEvent.VK_X, (byte)0x2d);
        scancodeTable.put(KeyEvent.VK_C, (byte)0x2e);
        scancodeTable.put(KeyEvent.VK_V, (byte)0x2f);
        scancodeTable.put(KeyEvent.VK_B, (byte)0x30);
        scancodeTable.put(KeyEvent.VK_N, (byte)0x31);
        scancodeTable.put(KeyEvent.VK_M, (byte)0x32);
        scancodeTable.put(KeyEvent.VK_COMMA, (byte)0x33);
        scancodeTable.put(KeyEvent.VK_PERIOD, (byte)0x34);
        scancodeTable.put(KeyEvent.VK_SLASH, (byte)0x35);
        scancodeTable.put(KeyEvent.VK_SHIFT, (byte)0x36);

        //37 KPad *

        //38 Missing L-Alt - Java does not pickup
        scancodeTable.put(KeyEvent.VK_ALT, (byte)0x38);
        scancodeTable.put(KeyEvent.VK_SPACE, (byte)0x39);

        scancodeTable.put(KeyEvent.VK_CAPS_LOCK, (byte)0x3a);

        scancodeTable.put(KeyEvent.VK_F1, (byte)0x3b);
        scancodeTable.put(KeyEvent.VK_F2, (byte)0x3c);
        scancodeTable.put(KeyEvent.VK_F3, (byte)0x3d);
        scancodeTable.put(KeyEvent.VK_F4, (byte)0x3e);
        scancodeTable.put(KeyEvent.VK_F5, (byte)0x3f);
        scancodeTable.put(KeyEvent.VK_F6, (byte)0x40);
        scancodeTable.put(KeyEvent.VK_F7, (byte)0x41);
        scancodeTable.put(KeyEvent.VK_F8, (byte)0x42);
        scancodeTable.put(KeyEvent.VK_F9, (byte)0x43);
        scancodeTable.put(KeyEvent.VK_F10, (byte)0x44);

        //45 Missing Num-Lock - Java does not pickup

        scancodeTable.put(KeyEvent.VK_SCROLL_LOCK, (byte)0x46);

        //47-53 are Numpad keys

        //54-56 are not used

        scancodeTable.put(122, (byte)0x57); // F11
        scancodeTable.put(123, (byte)0x58); // F12

        //59-ff are unused (for normal keys)

        //Extended Keys
        //e0,1c KPad Enter
        //e0,1d R-Ctrl
        //e0,2a fake L-Shift
        //e0,35 KPad /
        //e0,36 fake R-Shift
        //e0,37 Ctrl + Print Screen
        scancodeTable.put(KeyEvent.VK_ALT_GRAPH, (byte)(0x38 | 0x80));
        //e0,46 Ctrl + Break
        scancodeTable.put(KeyEvent.VK_HOME, (byte)(0x47 | 0x80));
        scancodeTable.put(KeyEvent.VK_UP, (byte)(0x48 | 0x80));
        scancodeTable.put(KeyEvent.VK_PAGE_UP, (byte)(0x49 | 0x80));
        scancodeTable.put(KeyEvent.VK_LEFT, (byte)(0x4b | 0x80));
        scancodeTable.put(KeyEvent.VK_RIGHT, (byte)(0x4d | 0x80));
        scancodeTable.put(KeyEvent.VK_END, (byte)(0x4f | 0x80));
        scancodeTable.put(KeyEvent.VK_DOWN, (byte)(0x50 | 0x80));
        scancodeTable.put(KeyEvent.VK_PAGE_DOWN, (byte)(0x51 | 0x80));
        scancodeTable.put(KeyEvent.VK_INSERT, (byte)(0x52 | 0x80));
        scancodeTable.put(KeyEvent.VK_DELETE, (byte)(0x53 | 0x80));
        //e0,5b L-Win
        //e0,5c R-Win
        //e0,5d Context-Menu

        scancodeTable.put(19, (byte)0xFF); //Pause
    }

    public static byte getScancode(Integer keyCode) {
        try {
            return scancodeTable.get(keyCode).byteValue();
        } catch (NullPointerException e) {
            return (byte)0x00;
        }
    }

    public static int[] getJavaKeycodes(char c) {
        switch (c) {
        case 'a':
            return new int[] { KeyEvent.VK_A };
        case 'b':
            return new int[] { KeyEvent.VK_B };
        case 'c':
            return new int[] { KeyEvent.VK_C };
        case 'd':
            return new int[] { KeyEvent.VK_D };
        case 'e':
            return new int[] { KeyEvent.VK_E };
        case 'f':
            return new int[] { KeyEvent.VK_F };
        case 'g':
            return new int[] { KeyEvent.VK_G };
        case 'h':
            return new int[] { KeyEvent.VK_H };
        case 'i':
            return new int[] { KeyEvent.VK_I };
        case 'j':
            return new int[] { KeyEvent.VK_J };
        case 'k':
            return new int[] { KeyEvent.VK_K };
        case 'l':
            return new int[] { KeyEvent.VK_L };
        case 'm':
            return new int[] { KeyEvent.VK_M };
        case 'n':
            return new int[] { KeyEvent.VK_N };
        case 'o':
            return new int[] { KeyEvent.VK_O };
        case 'p':
            return new int[] { KeyEvent.VK_P };
        case 'q':
            return new int[] { KeyEvent.VK_Q };
        case 'r':
            return new int[] { KeyEvent.VK_R };
        case 's':
            return new int[] { KeyEvent.VK_S };
        case 't':
            return new int[] { KeyEvent.VK_T };
        case 'u':
            return new int[] { KeyEvent.VK_U };
        case 'v':
            return new int[] { KeyEvent.VK_V };
        case 'w':
            return new int[] { KeyEvent.VK_W };
        case 'x':
            return new int[] { KeyEvent.VK_X };
        case 'y':
            return new int[] { KeyEvent.VK_Y };
        case 'z':
            return new int[] { KeyEvent.VK_Z };
        case 'A':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_A };
        case 'B':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_B };
        case 'C':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_C };
        case 'D':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_D };
        case 'E':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_E };
        case 'F':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_F };
        case 'G':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_G };
        case 'H':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_H };
        case 'I':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_I };
        case 'J':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_J };
        case 'K':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_K };
        case 'L':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_L };
        case 'M':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_M };
        case 'N':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_N };
        case 'O':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_O };
        case 'P':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_P };
        case 'Q':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Q };
        case 'R':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_R };
        case 'S':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_S };
        case 'T':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_T };
        case 'U':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_U };
        case 'V':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_V };
        case 'W':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_W };
        case 'X':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_X };
        case 'Y':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Y };
        case 'Z':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Z };
        case '`':
            return new int[] { KeyEvent.VK_BACK_QUOTE };
        case '0':
            return new int[] { KeyEvent.VK_0 };
        case '1':
            return new int[] { KeyEvent.VK_1 };
        case '2':
            return new int[] { KeyEvent.VK_2 };
        case '3':
            return new int[] { KeyEvent.VK_3 };
        case '4':
            return new int[] { KeyEvent.VK_4 };
        case '5':
            return new int[] { KeyEvent.VK_5 };
        case '6':
            return new int[] { KeyEvent.VK_6 };
        case '7':
            return new int[] { KeyEvent.VK_7 };
        case '8':
            return new int[] { KeyEvent.VK_8 };
        case '9':
            return new int[] { KeyEvent.VK_9 };
        case '-':
            return new int[] { KeyEvent.VK_MINUS };
        case '=':
            return new int[] { KeyEvent.VK_EQUALS };
        case '~':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE };
        case '!':
            return new int[] { KeyEvent.VK_EXCLAMATION_MARK };
        case '@':
            return new int[] { KeyEvent.VK_AT };
        case '#':
            return new int[] { KeyEvent.VK_NUMBER_SIGN };
        case '$':
            return new int[] { KeyEvent.VK_DOLLAR };
        case '%':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_5 };
        case '^':
            return new int[] { KeyEvent.VK_CIRCUMFLEX };
        case '&':
            return new int[] { KeyEvent.VK_AMPERSAND };
        case '*':
            return new int[] { KeyEvent.VK_ASTERISK };
        case '(':
            return new int[] { KeyEvent.VK_LEFT_PARENTHESIS };
        case ')':
            return new int[] { KeyEvent.VK_RIGHT_PARENTHESIS };
        case '_':
            return new int[] { KeyEvent.VK_UNDERSCORE };
        case '+':
            return new int[] { KeyEvent.VK_PLUS };
        case '\t':
            return new int[] { KeyEvent.VK_TAB };
        case '\n':
            return new int[] { KeyEvent.VK_ENTER };
        case '[':
            return new int[] { KeyEvent.VK_OPEN_BRACKET };
        case ']':
            return new int[] { KeyEvent.VK_CLOSE_BRACKET };
        case '\\':
            return new int[] { KeyEvent.VK_BACK_SLASH };
        case '{':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET };
        case '}':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET };
        case '|':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH };
        case ';':
            return new int[] { KeyEvent.VK_SEMICOLON };
        case ':':
            return new int[] { KeyEvent.VK_COLON };
        case '\'':
            return new int[] { KeyEvent.VK_QUOTE };
        case '"':
            return new int[] { KeyEvent.VK_QUOTEDBL };
        case ',':
            return new int[] { KeyEvent.VK_COMMA };
        case '<':
            return new int[] { KeyEvent.VK_LEFT };
        case '.':
            return new int[] { KeyEvent.VK_PERIOD };
        case '>':
            return new int[] { KeyEvent.VK_RIGHT };
        case '/':
            return new int[] { KeyEvent.VK_SLASH };
        case '?':
            return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH };
        case ' ':
            return new int[] { KeyEvent.VK_SPACE };
        default:
            throw new IllegalArgumentException("Cannot type character " + c);
        }
    }

    private KeyMapping() {
    }
}
