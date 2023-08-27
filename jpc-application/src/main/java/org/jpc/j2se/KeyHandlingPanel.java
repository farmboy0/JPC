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

import java.awt.AWTError;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author Rhys Newman
 * @author Chris Dennis
 */
public class KeyHandlingPanel extends JPanel implements KeyListener, FocusListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final Logger LOGGING = Logger.getLogger(KeyHandlingPanel.class.getName());

    public static final String MOUSE_CAPTURE = "Mouse Capture";

    private static Robot robot;
    private static Cursor emptyCursor;

    private int currentButtons;
    private double mouseSensitivity = 0.5;
    private Set<KeyboardKey> keyPressedSet;

    private boolean inputsLocked = false, mouseCaptureEnabled = true;
    private int lastMouseX, lastMouseY;

    static {
        try {
            ImageIcon emptyIcon = new ImageIcon(new byte[0]);
            emptyCursor = Toolkit.getDefaultToolkit().createCustomCursor(emptyIcon.getImage(), new Point(0, 0), "emptyCursor");
        } catch (AWTError e) {
            LOGGING.log(Level.WARNING, "Could not get AWT Toolkit, not even headless", e);
            emptyCursor = Cursor.getDefaultCursor();
        } catch (HeadlessException e) {
            LOGGING.log(Level.WARNING, "Headless environment could not create invisible cursor, using default.", e);
            emptyCursor = Cursor.getDefaultCursor();
        }
    }

    public KeyHandlingPanel() {
        init();
    }

    public KeyHandlingPanel(LayoutManager mgr) {
        super(mgr);
        init();
    }

    public void setMouseCaptureEnabled(boolean value) {
        mouseCaptureEnabled = value;
        if (mouseCaptureEnabled) {
            try {
                robot = new Robot();
                robot.setAutoDelay(5);
            } catch (Exception e) {
            }
        } else
            robot = null;
    }

    public boolean mouseCaptureEnabled() {
        return mouseCaptureEnabled && robot != null;
    }

    protected void init() {
        keyPressedSet = new HashSet<KeyboardKey>();
        setMouseCaptureEnabled(true);

        addFocusListener(this);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setFocusable(true);
        setRequestFocusEnabled(true);
        setFocusTraversalKeysEnabled(false);
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, Collections.EMPTY_SET);
    }

    protected void keyPress(KeyboardKey key) {
    }

    protected void keyRelease(KeyboardKey key) {
    }

    protected void repeatedKeyPress(KeyboardKey key) {
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        Set<KeyboardKey> keysDown;

        synchronized (this) {
            keysDown = keyPressedSet;
            keyPressedSet = new HashSet<KeyboardKey>();
        }

        for (KeyboardKey key : keysDown)
            keyRelease(key);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        boolean isRepeat;

        KeyboardKey key = new KeyboardKey(e);
        synchronized (this) {
            isRepeat = !keyPressedSet.add(key);
        }

        if (isRepeat)
            repeatedKeyPress(key);
        else
            keyPress(key);

        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        KeyboardKey key = new KeyboardKey(e);
        synchronized (this) {
            keyPressedSet.remove(key);
        }

        keyRelease(key);
        e.consume();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    public void ensureParentsFocussable() {
        for (Component comp = getParent(); comp != null; comp = comp.getParent())
            comp.setFocusable(true);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (e.getButton() == MouseEvent.BUTTON1)
                lockInputs();
            else if (e.getButton() == MouseEvent.BUTTON3)
                unlockInputs();
        }

        requestFocusInWindow();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (!inputsLocked || robot != null)
            return;

        int tolerance = 20;
        int rate = 256;

        int x = e.getX();
        int y = e.getY();
        Dimension s = getSize();

        if (x < tolerance)
            mouseEventReceived(-rate, 0, 0, currentButtons);
        if (y < tolerance)
            mouseEventReceived(0, -rate, 0, currentButtons);
        if (x > s.width - tolerance)
            mouseEventReceived(rate, 0, 0, currentButtons);
        if (y > s.height - tolerance)
            mouseEventReceived(0, rate, 0, currentButtons);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (!inputsLocked || robot != null) {
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!inputsLocked)
            return;

        switch (e.getButton()) {
        case MouseEvent.BUTTON1:
            currentButtons |= 1;
            break;
        case MouseEvent.BUTTON3:
            currentButtons |= 2;
            break;
        case MouseEvent.BUTTON2:
            currentButtons |= 4;
            break;
        }

        mouseEventReceived(0, 0, 0, currentButtons);

        int mask = InputEvent.BUTTON3_DOWN_MASK | InputEvent.CTRL_DOWN_MASK;
        if ((e.getModifiersEx() & mask) == mask)
            unlockInputs();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!inputsLocked)
            return;

        switch (e.getButton()) {
        case MouseEvent.BUTTON1:
            currentButtons &= ~1;
            break;
        case MouseEvent.BUTTON3:
            currentButtons &= ~2;
            break;
        case MouseEvent.BUTTON2:
            currentButtons &= ~4;
            break;
        }

        mouseEventReceived(0, 0, 0, currentButtons);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        movedMouse(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        movedMouse(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!inputsLocked)
            return;

        mouseEventReceived(0, 0, e.getWheelRotation(), currentButtons);
    }

    public boolean mouseCaptured() {
        return inputsLocked;
    }

    public void lockInputs() {
        if (emptyCursor != null)
            setCursor(emptyCursor);
        inputsLocked = true;
        firePropertyChange(MOUSE_CAPTURE, false, true);
    }

    public void unlockInputs() {
        if (emptyCursor != null)
            setCursor(Cursor.getDefaultCursor());
        inputsLocked = false;
        firePropertyChange(MOUSE_CAPTURE, true, false);
    }

    public void setMouseSensitivity(double factor) {
        mouseSensitivity = factor;
    }

    private void movedMouse(MouseEvent e) {
        if (!inputsLocked)
            return;

        int mx = 0, my = 0;

        if (robot == null) {
            mx = e.getX() - lastMouseX;
            my = e.getY() - lastMouseY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        } else {
            Point origin = getLocationOnScreen();
            int win_x = origin.x;
            int win_y = origin.y;
            int win_w2 = getWidth() / 2;
            int win_h2 = getHeight() / 2;

            mx = e.getX() - win_w2;
            my = e.getY() - win_h2;

            if (mx == 0 && my == 0)
                return;
            robot.mouseMove(win_x + win_w2, win_y + win_h2);
        }

        if (mx > 0)
            mx = Math.max((int)(mx * mouseSensitivity), 1);
        else if (mx < 0)
            mx = Math.min((int)(mx * mouseSensitivity), -1);

        if (my > 0)
            my = Math.max((int)(my * mouseSensitivity), 1);
        else if (my < 0)
            my = Math.min((int)(my * mouseSensitivity), -1);

        mouseEventReceived(mx, my, 0, currentButtons);
    }

    protected void mouseEventReceived(int dx, int dy, int dz, int buttons) {
    }
}
