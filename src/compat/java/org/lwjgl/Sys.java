package org.lwjgl;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by gudenau on 5/30/2017.
 * <p>
 * LWJGL3
 */
public class Sys {
    private static final long timerOffset;
    public static final String VERSION = Version.getVersion();

    private Sys() {
    }


    static {
        timerOffset = System.nanoTime();
    }

    /**
     * Obtains the number of ticks that the hires timer does in a second.
     *
     * @return timer resolution in ticks per second.
     */
    public static long getTimerResolution() {
        return 1000000000;
    }

    /**
     * Gets the current value of the hires timer, in ticks. When the Sys class is first loaded
     * the hires timer is reset to 0. If no hires timer is present then this method will always
     * return 0.<p><strong>NOTEZ BIEN</strong> that the hires timer WILL wrap around.
     *
     * @return the current hires time, in ticks (always >= 0)
     */
    public static long getTime() {
        return (System.nanoTime() - timerOffset) & 0x7FFFFFFFFFFFFFFFL;
    }

    /**
     * Return the version of the core LWJGL libraries as a String.
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Initialization. This is just a dummy method to trigger the static constructor.
     */
    public static void initialize() {
    }

    public static boolean openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
            return true;
        } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
            return false;
        }
    }
}
