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
import java.util.Locale;

/**
 * @author Chris Dennis
 */
public class KeyMapping {
    private enum ScanCodeSet1 {
        SC1_ESCAPE((byte)0x01, KeyEvent.VK_ESCAPE), //
        SC1_1((byte)0x02, KeyEvent.VK_1), //
        SC1_2((byte)0x03, KeyEvent.VK_2), //
        SC1_3((byte)0x04, KeyEvent.VK_3), //
        SC1_4((byte)0x05, KeyEvent.VK_4), //
        SC1_5((byte)0x06, KeyEvent.VK_5), //
        SC1_6((byte)0x07, KeyEvent.VK_6), //
        SC1_7((byte)0x08, KeyEvent.VK_7), //
        SC1_8((byte)0x09, KeyEvent.VK_8), //
        SC1_9((byte)0x0a, KeyEvent.VK_9), //
        SC1_0((byte)0x0b, KeyEvent.VK_0), //
        SC1_MINUS((byte)0x0c, KeyEvent.VK_MINUS, 0x10000df), //
        SC1_EQUALS((byte)0x0d, KeyEvent.VK_EQUALS), // TODO
        SC1_BS((byte)0x0e, KeyEvent.VK_BACK_SPACE), //

        SC1_TAB((byte)0xf, KeyEvent.VK_TAB), //
        SC1_Q((byte)0x10, KeyEvent.VK_Q), //
        SC1_W((byte)0x11, KeyEvent.VK_W), //
        SC1_E((byte)0x12, KeyEvent.VK_E), //
        SC1_R((byte)0x13, KeyEvent.VK_R), //
        SC1_T((byte)0x14, KeyEvent.VK_T), //
        SC1_Y((byte)0x15, KeyEvent.VK_Y, KeyEvent.VK_Z), //
        SC1_U((byte)0x16, KeyEvent.VK_U), //
        SC1_I((byte)0x17, KeyEvent.VK_I), //
        SC1_O((byte)0x18, KeyEvent.VK_O), //
        SC1_P((byte)0x19, KeyEvent.VK_P), //
        SC1_OPEN_BRACKET((byte)0x1a, KeyEvent.VK_OPEN_BRACKET, 0x10000fc), //
        SC1_CLOSE_BRACKET((byte)0x1b, KeyEvent.VK_CLOSE_BRACKET, 0x209), //
        SC1_ENTER((byte)0x1c, KeyEvent.VK_ENTER), //

        SC1_CONTROL((byte)0x1d, KeyEvent.VK_CONTROL), //
        SC1_A((byte)0x1e, KeyEvent.VK_A), //
        SC1_S((byte)0x1f, KeyEvent.VK_S), //
        SC1_D((byte)0x20, KeyEvent.VK_D), //
        SC1_F((byte)0x21, KeyEvent.VK_F), //
        SC1_G((byte)0x22, KeyEvent.VK_G), //
        SC1_H((byte)0x23, KeyEvent.VK_H), //
        SC1_J((byte)0x24, KeyEvent.VK_J), //
        SC1_K((byte)0x25, KeyEvent.VK_K), //
        SC1_L((byte)0x26, KeyEvent.VK_L), //
        SC1_SEMICOLON((byte)0x27, KeyEvent.VK_SEMICOLON, 0x10000d6), //
        SC1_QUOTE((byte)0x28, KeyEvent.VK_QUOTE, 0x10000c4), //
        SC1_BACK_QUOTE((byte)0x29, KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_NUMBER_SIGN), //

        SC1_LSHIFT((byte)0x2a, KeyEvent.KEY_LOCATION_LEFT, KeyEvent.VK_SHIFT, KeyEvent.VK_SHIFT), //
        SC1_BACK_SLASH((byte)0x2b, KeyEvent.VK_BACK_SLASH, KeyEvent.VK_LESS), //
        SC1_Z((byte)0x2c, KeyEvent.VK_Z, KeyEvent.VK_Y), //
        SC1_X((byte)0x2d, KeyEvent.VK_X), //
        SC1_C((byte)0x2e, KeyEvent.VK_C), //
        SC1_V((byte)0x2f, KeyEvent.VK_V), //
        SC1_B((byte)0x30, KeyEvent.VK_B), //
        SC1_N((byte)0x31, KeyEvent.VK_N), //
        SC1_M((byte)0x32, KeyEvent.VK_M), //
        SC1_COMMA((byte)0x33, KeyEvent.VK_COMMA), //
        SC1_PERIOD((byte)0x34, KeyEvent.VK_PERIOD), //
        SC1_SLASH((byte)0x35, KeyEvent.VK_SLASH, KeyEvent.VK_MINUS), //
        SC1_RSHIFT((byte)0x36, KeyEvent.KEY_LOCATION_RIGHT, KeyEvent.VK_SHIFT, KeyEvent.VK_SHIFT), //

        SC1_KP_MULTI((byte)0x37, KeyEvent.KEY_LOCATION_NUMPAD, KeyEvent.VK_MULTIPLY, KeyEvent.VK_MULTIPLY), //

        SC1_ALT((byte)0x38, KeyEvent.VK_ALT), //
        SC1_SPACE((byte)0x39, KeyEvent.VK_SPACE), //
        SC1_CAPS_LOCK((byte)0x3a, KeyEvent.VK_CAPS_LOCK), //

        SC1_F1((byte)0x3b, KeyEvent.VK_F1), //
        SC1_F2((byte)0x3c, KeyEvent.VK_F2), //
        SC1_F3((byte)0x3d, KeyEvent.VK_F3), //
        SC1_F4((byte)0x3e, KeyEvent.VK_F4), //
        SC1_F5((byte)0x3f, KeyEvent.VK_F5), //
        SC1_F6((byte)0x40, KeyEvent.VK_F6), //
        SC1_F7((byte)0x41, KeyEvent.VK_F7), //
        SC1_F8((byte)0x42, KeyEvent.VK_F8), //
        SC1_F9((byte)0x43, KeyEvent.VK_F9), //
        SC1_F10((byte)0x44, KeyEvent.VK_F10), //

        //45 Missing Num-Lock - Java does not pick up
        SC1_NUM_LOCK((byte)0x45, KeyEvent.VK_NUM_LOCK), //
        SC1_SCROLL_LOCK((byte)0x46, KeyEvent.VK_SCROLL_LOCK), //

        //47-53 are Numpad keys

        //54-56 are not used

        SC1_F11((byte)0x57, KeyEvent.VK_F11), //
        SC1_F12((byte)0x58, KeyEvent.VK_F12), //

        //59-ff are unused (for normal keys)

        //Extended Keys
        //e0,1c KPad Enter
        //e0,1d R-Ctrl
        //e0,2a fake L-Shift
        //e0,35 KPad /
        //e0,36 fake R-Shift
        //e0,37 Ctrl + Print Screen
        SC1_ALT_GRAPH((byte)(0x38 | 0x80), KeyEvent.VK_ALT_GRAPH), //
        //e0,46 Ctrl + Break
        SC1_HOME((byte)(0x47 | 0x80), KeyEvent.VK_HOME), //
        SC1_UP((byte)(0x48 | 0x80), KeyEvent.VK_UP), //
        SC1_PAGE_UP((byte)(0x49 | 0x80), KeyEvent.VK_PAGE_UP), //
        SC1_LEFT((byte)(0x4b | 0x80), KeyEvent.VK_LEFT), //
        SC1_RIGHT((byte)(0x4d | 0x80), KeyEvent.VK_RIGHT), //
        SC1_END((byte)(0x4f | 0x80), KeyEvent.VK_END), //
        SC1_DOWN((byte)(0x50 | 0x80), KeyEvent.VK_DOWN), //
        SC1_PAGE_DOWN((byte)(0x51 | 0x80), KeyEvent.VK_PAGE_DOWN), //
        SC1_INSERT((byte)(0x52 | 0x80), KeyEvent.VK_INSERT), //
        SC1_DELETE((byte)(0x53 | 0x80), KeyEvent.VK_DELETE), //
        //e0,5b L-Win
        //e0,5c R-Win
        //e0,5d Context-Menu

        SC1_PAUSE((byte)0xFF, 19), // Pause
        ;

        private final byte scancode;
        private final int keyLocation;
        private final int vkUS;
        private final int vkDE;

        private ScanCodeSet1(byte scancode, int vk) {
            this(scancode, KeyEvent.KEY_LOCATION_UNKNOWN, vk, vk);
        }

        private ScanCodeSet1(byte scancode, int vkUS, int vkDE) {
            this(scancode, KeyEvent.KEY_LOCATION_UNKNOWN, vkUS, vkDE);
        }

        private ScanCodeSet1(byte scancode, int keyLocation, int vkUS, int vkDE) {
            this.scancode = scancode;
            this.keyLocation = keyLocation;
            this.vkUS = vkUS;
            this.vkDE = vkDE;
        }

        private int getKeyCode(Locale lang) {
            if (Locale.GERMANY.equals(lang)) {
                return vkDE;
            }
            return vkUS;
        }

        public static byte getScancode(Locale lang, int loc, int vk) {
            for (ScanCodeSet1 sc1 : ScanCodeSet1.values()) {
                if (sc1.getKeyCode(lang) == vk && (sc1.keyLocation == KeyEvent.KEY_LOCATION_UNKNOWN || sc1.keyLocation == loc)) {
                    return sc1.scancode;
                }
            }
            return (byte)0x00;
        }
    }

    public static byte getScancode(Locale lang, KeyboardKey key) {
        return ScanCodeSet1.getScancode(lang, key.getLocation(), key.getCode());
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
