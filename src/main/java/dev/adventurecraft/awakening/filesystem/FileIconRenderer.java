package dev.adventurecraft.awakening.filesystem;

import ch.bailu.gtk.gdk.Display;
import ch.bailu.gtk.gtk.Gtk;
import dev.adventurecraft.awakening.image.ImageBuffer;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class FileIconRenderer implements AutoCloseable {
    public abstract ImageBuffer getIcon(Path path, FileIconOptions options);

    public abstract Future<ImageBuffer> getIconAsync(Path path, FileIconOptions options, ExecutorService executor);

    public static @Nullable FileIconRenderer create() {
        if (Gtk.initCheck()) {
            return new GtkFileIconRenderer(Display.getDefault());
        }
        return null;
    }

    public @Override void close() {
    }
}
