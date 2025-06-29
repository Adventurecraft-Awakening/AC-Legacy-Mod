package dev.adventurecraft.awakening.filesystem;

import ch.bailu.gtk.gdk.Display;
import ch.bailu.gtk.gtk.Gtk;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.image.ImageBuffer;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class FileIconRenderer implements AutoCloseable {

    public abstract @Nullable ImageBuffer getIcon(Path path, FileIconOptions options);

    public Future<ImageBuffer> getIconAsync(Path path, FileIconOptions options, ExecutorService executor) {
        return executor.submit(() -> this.getIcon(path, options));
    }

    public static @Nullable FileIconRenderer create() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                return createWin32();
            }
        }
        catch (NoClassDefFoundError | ClassNotFoundException ex) {
            logMissingClass(ex, os);
        }

        try {
            return createGtk();
        }
        catch (NoClassDefFoundError | ClassNotFoundException ex) {
            logMissingClass(ex, "GTK");
        }
        return null;
    }

    private static void logMissingClass(Throwable ex, String kind) {
        ACMod.LOGGER.warn("Missing class for optional feature ({}) \"{}\": {}", kind, FileIconRenderer.class.getName(), ex.getMessage());
    }

    private static FileIconRenderer createWin32()
        throws ClassNotFoundException, NoClassDefFoundError {
        WinDef.HDC hdc = User32.INSTANCE.GetDC(null);
        return new Win32FileIconRenderer(hdc);
    }

    private static @Nullable GtkFileIconRenderer createGtk()
        throws ClassNotFoundException, NoClassDefFoundError {
        try {
            if (Gtk.initCheck()) {
                return new GtkFileIconRenderer(Display.getDefault());
            }
        }
        catch (UnsatisfiedLinkError ex) {
            ACMod.LOGGER.warn("Failed to initialize GTK: ", ex);
        }
        return null;
    }

    public @Override void close() {
    }
}
