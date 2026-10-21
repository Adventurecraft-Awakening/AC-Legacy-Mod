package org.lwjgl.input;

import org.lwjgl.LWJGLException;

import java.nio.IntBuffer;

/**
 * A class representing a native cursor. Instances of this
 * class can be used with Mouse.setCursor(), if available.
 *
 * @author elias_naur <elias_naur@users.sourceforge.net>
 * @version $Revision$
 * $Id$
 */

public class Cursor {
    public static final int CURSOR_ONE_BIT_TRANSPARENCY = 1;
    public static final int CURSOR_8_BIT_ALPHA = 2;
    public static final int CURSOR_ANIMATION = 4;

    public Cursor(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, IntBuffer delays) throws LWJGLException {
        if ((getCapabilities() & CURSOR_ONE_BIT_TRANSPARENCY) == 0)
            throw new LWJGLException("Native cursors not supported");
    }

    public static int getMinCursorSize() {
        if (!Mouse.isCreated())
            throw new IllegalStateException("Mouse must be created.");
        return 0;
    }

    public static int getMaxCursorSize() {
        if (!Mouse.isCreated())
            throw new IllegalStateException("Mouse must be created.");
        return 0;
    }

    public static int getCapabilities() {
        return 0;
    }

    public void destroy() {
    }
}
