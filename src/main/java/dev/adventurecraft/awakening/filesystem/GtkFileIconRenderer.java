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

/**
 * GTK-based file icon renderer that uses GIO and GTK libraries to render file icons.
 * This implementation supports standard icons, symbolic icons, and preview thumbnails
 * through the GTK icon theme system.
 * 
 * @author Adventurecraft Team
 * @since 0.5.5
 */
public class GtkFileIconRenderer extends FileIconRenderer {

    /** GIO attribute for standard file icons */
    private static final String ATTR_ICON = GioConstants.FILE_ATTRIBUTE_STANDARD_ICON;
    
    /** GIO attribute for symbolic file icons */
    private static final String ATTR_SYM_ICON = GioConstants.FILE_ATTRIBUTE_STANDARD_SYMBOLIC_ICON;
    
    /** GIO attribute for preview icons/thumbnails */
    private static final String ATTR_PREVIEW_ICON = GioConstants.FILE_ATTRIBUTE_PREVIEW_ICON;

    /** The GTK display used for rendering */
    private final Display display;

    /** Signal handler for icon theme changes */
    private SignalHandler iconThemeChangedSignal;
    
    /** Current icon theme instance */
    private IconTheme iconTheme;

    /**
     * Creates a new GTK file icon renderer.
     * 
     * @param display the GTK display to use for rendering
     */
    public GtkFileIconRenderer(Display display) {
        this.display = display;
    }

    /**
     * Gets the current icon theme for the display.
     * Lazily initializes the theme and sets up change notifications.
     * 
     * @return the current icon theme
     */
    public IconTheme getIconTheme() {
        if (this.iconTheme == null) {
            this.iconTheme = IconTheme.getForDisplay(display);
            this.iconThemeChangedSignal = this.iconTheme.onChanged(() -> {
                this.iconTheme = IconTheme.getForDisplay(this.display);
            });
        }
        return this.iconTheme;
    }

    /**
     * Synchronously retrieves and renders a file icon.
     * 
     * @param path the file path to get an icon for
     * @param options rendering options including size, format, and icon type flags
     * @return the rendered icon as an ImageBuffer, or null if no icon could be retrieved
     * @throws RuntimeException if a GIO allocation error occurs
     */
    @Override
    public ImageBuffer getIcon(Path path, FileIconOptions options) {
        var file = newFile(path);
        try {
            var icon = queryIcon(file, options, null);
            return renderIcon(icon, options);
        }
        catch (AllocationError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asynchronously retrieves and renders a file icon.
     * 
     * @param path the file path to get an icon for
     * @param options rendering options including size, format, and icon type flags
     * @param executor the executor service to run the operation on
     * @return a Future that will contain the rendered icon, or null if no icon could be retrieved
     */
    @Override
    public Future<ImageBuffer> getIconAsync(Path path, FileIconOptions options, ExecutorService executor) {
        var file = newFile(path);
        var cancellable = new Cancellable();

        return new ProxyFuture<>(executor.submit(() -> {
            try {
                var icon = queryIcon(file, options, cancellable);
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

    /**
     * Renders a GIO Icon to an ImageBuffer using the GTK rendering pipeline.
     * 
     * @param icon the GIO Icon to render
     * @param options rendering options including size, format, and scale
     * @return the rendered icon as an ImageBuffer
     * @throws AllocationError if a GIO allocation error occurs during rendering
     */
    private ImageBuffer renderIcon(Icon icon, FileIconOptions options)
        throws AllocationError {
        var theme = this.getIconTheme();

        int size = Math.max(options.width(), options.height());
        var themeIcon = theme.lookupByGicon(icon, size, options.scale(), TextDirection.LTR, IconLookupFlags.NONE);

        var renderer = new ch.bailu.gtk.gsk.CairoRenderer();
        renderer.realizeForDisplay(display);

        var snapshot = new Snapshot();
        themeIcon.asPaintable().snapshot(snapshot, size, size);

        var node = snapshot.freeToNode();
        var texture = renderer.renderTexture(node, null);

        var downloader = new TextureDownloader(texture);
        downloader.setFormat(MemoryFormat.R8G8B8A8);

        return createImageBufferFromTexture(downloader, texture, options.format());
    }

    /**
     * Queries file information and retrieves the appropriate icon based on the requested flags.
     * Supports standard icons, symbolic icons, and preview thumbnails.
     * 
     * @param file the GIO File to query
     * @param options options specifying which types of icons to retrieve
     * @param cancellable optional cancellable for the operation, may be null
     * @return the first available icon matching the requested flags, or null if none found
     * @throws AllocationError if a GIO allocation error occurs during the query
     */
    @Nullable
    public static Icon queryIcon(ch.bailu.gtk.gio.File file, FileIconOptions options, @Nullable Cancellable cancellable)
        throws AllocationError {
        String attributes = String.join(",", ATTR_ICON, ATTR_SYM_ICON, ATTR_PREVIEW_ICON);
        FileInfo info = file.queryInfo(attributes, FileQueryInfoFlags.NONE, cancellable);

        for (FileIconFlags flag : options.flags()) {
            Icon icon = switch (flag) {
                case Icon -> getIcon(info);
                case Symbolic -> getSymIcon(info);
                case Thumbnail -> getPreviewIcon(info);
            };
            if (icon != null) {
                return icon;
            }
        }
        return null;
    }

    /**
     * Retrieves the standard icon from file information.
     * 
     * @param info the FileInfo containing file attributes
     * @return the standard icon if available, null otherwise
     */
    private static Icon getIcon(FileInfo info) {
        return info.hasAttribute(ATTR_ICON) ? info.getIcon() : null;
    }

    /**
     * Retrieves the symbolic icon from file information.
     * 
     * @param info the FileInfo containing file attributes
     * @return the symbolic icon if available, null otherwise
     */
    private static Icon getSymIcon(FileInfo info) {
        return info.hasAttribute(ATTR_SYM_ICON) ? info.getSymbolicIcon() : null;
    }

    /**
     * Retrieves the preview icon/thumbnail from file information.
     * This provides a preview representation of the file content when available.
     * 
     * @param info the FileInfo containing file attributes
     * @return the preview icon if available, null otherwise
     */
    private static Icon getPreviewIcon(FileInfo info) {
        if (!info.hasAttribute(ATTR_PREVIEW_ICON)) {
            return null;
        }
        Object previewObj = info.getAttributeObject(ATTR_PREVIEW_ICON);
        return previewObj instanceof Icon ? (Icon) previewObj : null;
    }

    /**
     * Creates a GIO File from a Java Path.
     * 
     * @param path the file path to convert
     * @return a GIO File representing the path
     */
    private static File newFile(Path path) {
        return File.newForUri(new Str(path.toUri().toString()));
    }

    /**
     * Creates an ImageBuffer from a GTK Texture by downloading the texture data
     * and copying it to a managed buffer.
     * 
     * @param downloader the texture downloader to use
     * @param texture the source texture
     * @param format the desired image format for the output buffer
     * @return an ImageBuffer containing the texture data
     */
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

    /**
     * Closes the renderer and cleans up resources.
     * Disconnects the icon theme change signal handler.
     */
    @Override
    public void close() {
        if (this.iconThemeChangedSignal != null) {
            this.iconThemeChangedSignal.disconnect();
        }
    }
}
