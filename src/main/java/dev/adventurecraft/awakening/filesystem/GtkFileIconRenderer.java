package dev.adventurecraft.awakening.filesystem;

import ch.bailu.gtk.gdk.Display;
import ch.bailu.gtk.gdk.MemoryFormat;
import ch.bailu.gtk.gdk.Texture;
import ch.bailu.gtk.gdk.TextureDownloader;
import ch.bailu.gtk.gio.*;
import ch.bailu.gtk.glib.Bytes;
import ch.bailu.gtk.glib.Glib;
import ch.bailu.gtk.gtk.IconLookupFlags;
import ch.bailu.gtk.gtk.IconTheme;
import ch.bailu.gtk.gtk.Snapshot;
import ch.bailu.gtk.gtk.TextDirection;
import ch.bailu.gtk.lib.handler.SignalHandler;
import ch.bailu.gtk.type.Int64;
import ch.bailu.gtk.type.Pointer;
import ch.bailu.gtk.type.Str;
import ch.bailu.gtk.type.exception.AllocationError;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.util.ProxyFuture;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class GtkFileIconRenderer extends FileIconRenderer {
    private static final String ATTR_ICON = GioConstants.FILE_ATTRIBUTE_STANDARD_ICON;
    private static final String ATTR_SYM_ICON = GioConstants.FILE_ATTRIBUTE_STANDARD_SYMBOLIC_ICON;

    private final Display display;

    private SignalHandler iconThemeChangedSignal;
    private IconTheme iconTheme;

    public GtkFileIconRenderer(Display display) {
        this.display = display;
    }

    public IconTheme getIconTheme() {
        if (this.iconTheme == null) {
            this.iconTheme = IconTheme.getForDisplay(display);
            this.iconThemeChangedSignal = this.iconTheme.onChanged(() -> {
                this.iconTheme = IconTheme.getForDisplay(this.display);
            });
        }
        return this.iconTheme;
    }

    @Override
    public ImageBuffer getIcon(Path path, FileIconOptions options) {
        var file = newFile(path);
        try {
            var icon = queryIcon(file, options.symbolic(), null);
            return renderIcon(icon, options);
        }
        catch (AllocationError e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Future<ImageBuffer> getIconAsync(Path path, FileIconOptions options, ExecutorService executor) {
        var file = newFile(path);
        var cancellable = new Cancellable();

        return new ProxyFuture<>(executor.submit(() -> {
            try {
                var icon = queryIcon(file, options.symbolic(), cancellable);
                return renderIcon(icon, options);
            }
            catch (AllocationError e) {
                throw new RuntimeException(e);
            }
        })) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (mayInterruptIfRunning) {
                    cancellable.cancel();
                }
                return super.cancel(mayInterruptIfRunning);
            }
        };
    }

    private ImageBuffer renderIcon(Icon icon, FileIconOptions options)
        throws AllocationError {
        var theme = this.getIconTheme();

        int size = options.size();
        var themeIcon = theme.lookupByGicon(icon, size, options.scale(), TextDirection.LTR, IconLookupFlags.NONE);

        var renderer = new ch.bailu.gtk.gsk.CairoRenderer();
        renderer.realizeForDisplay(display);

        var snapshot = new Snapshot();
        themeIcon.asPaintable().snapshot(snapshot, size, size);

        var node = snapshot.freeToNode();
        var texture = renderer.renderTexture(node, null);

        var downloader = new TextureDownloader(texture);
        downloader.setFormat(MemoryFormat.R8G8B8A8);

        var image = createImageBufferFromTexture(downloader, texture, options.format());

        return image;
    }

    @Nullable
    public static Icon queryIcon(ch.bailu.gtk.gio.File file, boolean symbolic, @Nullable Cancellable cancellable)
        throws AllocationError {
        String attributes = String.join(",", ATTR_ICON, ATTR_SYM_ICON);
        FileInfo info = file.queryInfo(attributes, FileQueryInfoFlags.NONE, cancellable);

        if (symbolic) {
            Icon icon = getSymIcon(info);
            return icon != null ? icon : getIcon(info);
        }
        else {
            Icon icon = getIcon(info);
            return icon != null ? icon : getSymIcon(info);
        }
    }

    private static Icon getIcon(FileInfo info) {
        return info.hasAttribute(ATTR_ICON) ? info.getIcon() : null;
    }

    private static Icon getSymIcon(FileInfo info) {
        return info.hasAttribute(ATTR_SYM_ICON) ? info.getSymbolicIcon() : null;
    }

    private static File newFile(Path path) {
        return File.newForUri(new Str(path.toUri().toString()));
    }

    private static ImageBuffer createImageBufferFromTexture(
        TextureDownloader downloader,
        Texture texture,
        ImageFormat format
    ) {
        var stride = new Int64();
        Bytes bytes = downloader.downloadBytes(stride);

        var length = new Int64();
        Pointer dataPtr = bytes.unrefToData(length);
        try {
            var image = ImageBuffer.create(texture.getWidth(), texture.getHeight(), (int) stride.get(), format);

            long dataAddress = dataPtr.asCPointer();
            long imageAddress = MemoryUtil.memAddress(image.getBuffer());
            MemoryUtil.memCopy(dataAddress, imageAddress, length.get());

            return image;
        }
        finally {
            Glib.free(dataPtr);
        }
    }

    @Override
    public void close() {
        if (this.iconThemeChangedSignal != null) {
            this.iconThemeChangedSignal.disconnect();
        }
    }
}
