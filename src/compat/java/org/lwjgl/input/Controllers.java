package org.lwjgl.input;

import org.lwjgl.LWJGLException;

/**
 * The collection of controllers currently connected.
 *
 * @author Kevin Glass
 */
public class Controllers {

    public static void create() throws LWJGLException {
    }

    public static int getControllerCount() {
        return 0;
    }

    public static void poll() {
    }

    public static void clearEvents() {
    }

    public static boolean next() {
        return false;
    }

    public static boolean isCreated() {
        return false;
    }

    public static void destroy() {
    }

    public static int getEventControlIndex() {
        return 0;
    }

    public static boolean isEventButton() {
        return false;
    }

    public static boolean isEventAxis() {
        return false;
    }

    public static boolean isEventXAxis() {
        return false;
    }

    public static boolean isEventYAxis() {
        return false;
    }

    public static boolean isEventPovX() {
        return false;
    }

    public static boolean isEventPovY() {
        return false;
    }

    public static long getEventNanoseconds() {
        return 0;
    }

    public static boolean getEventButtonState() {
        return false;
    }

    public static float getEventXAxisValue() {
        return 0;
    }

    public static float getEventYAxisValue() {
        return 0;
    }
}
