package dev.adventurecraft.awakening.filesystem;

import com.sun.jna.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class Win32FileIconRenderer extends FileIconRenderer {

    private static final int INITIAL_ONLY_FLAGS =
        IShellItemImageFactory.SIIGBF_ICONONLY | IShellItemImageFactory.SIIGBF_THUMBNAILONLY;

    private final WinDef.HDC hdc;

    public Win32FileIconRenderer(WinDef.HDC hdc) {
        this.hdc = hdc;
    }

    public @Override ImageBuffer getIcon(Path path, FileIconOptions options) {
        var size = new SIZEByValue(options.width() * options.scale(), options.height() * options.scale());
        if (size.cx == 0 || size.cy == 0) {
            return null;
        }

        int flags = IShellItemImageFactory.SIIGBF_BIGGERSIZEOK | INITIAL_ONLY_FLAGS;
        if (options.flags().contains(FileIconFlags.Icon)) {
            flags &= ~IShellItemImageFactory.SIIGBF_THUMBNAILONLY;
        }
        if (options.flags().contains(FileIconFlags.Thumbnail)) {
            flags &= ~IShellItemImageFactory.SIIGBF_ICONONLY;
        }
        if ((flags & INITIAL_ONLY_FLAGS) == INITIAL_ONLY_FLAGS) {
            return null; // neither flag was excluded
        }

        return getIconForFile(path.normalize().toString(), size, flags);
    }

    private ImageBuffer getIconForFile(String fileName, SIZEByValue size, int flags) {
        WinDef.HBITMAP hbitmap = getHBITMAPForFile(fileName, size, flags);
        if (hbitmap == null) {
            return null;
        }

        var gdi32 = GDI32.INSTANCE;
        var bitmap = new WinGDI.BITMAP();
        try {
            int written = gdi32.GetObject(hbitmap, bitmap.size(), bitmap.getPointer());
            if (written <= 0) {
                return null;
            }
            bitmap.read();
            int bmpW = bitmap.bmWidth.intValue();
            int bmpH = bitmap.bmHeight.intValue();

            final int usage = WinGDI.DIB_RGB_COLORS;

            var bitmapinfo = new WinGDI.BITMAPINFO();
            int bits1 = gdi32.GetDIBits(this.hdc, hbitmap, 0, 0, Pointer.NULL, bitmapinfo, usage);
            if (bits1 == 0) {
                throw new IllegalArgumentException("GetDIBits should not return 0");
            }
            bitmapinfo.read();
            var bitmapMemory = new Memory(bitmapinfo.bmiHeader.biSizeImage);
            bitmapinfo.bmiHeader.biCompression = WinGDI.BI_RGB;
            bitmapinfo.bmiHeader.biHeight = -bmpH;

            final int lines = bitmapinfo.bmiHeader.biHeight;
            int bits2 = gdi32.GetDIBits(this.hdc, hbitmap, 0, lines, bitmapMemory, bitmapinfo, usage);
            if (bits2 == 0) {
                bitmapMemory.close();
                throw new IllegalArgumentException("GetDIBits should not return 0");
            }

            ByteBuffer buffer = bitmapMemory.getByteBuffer(0, (long) bmpW * bmpH * 4);
            return ImageBuffer.wrap(buffer, bmpW, bmpH, ImageFormat.BGRA_U8);
        }
        finally {
            gdi32.DeleteObject(hbitmap);
        }
    }

    private static WinDef.HBITMAP getHBITMAPForFile(String fileName, SIZEByValue size, int flags) {
        if (!COMUtils.SUCCEEDED(Ole32.INSTANCE.CoInitialize(null))) {
            return null;
        }

        var shellItem = new PointerByReference();
        if (!COMUtils.SUCCEEDED(ExShell32.INSTANCE.SHCreateItemFromParsingName(
            new WString(fileName),
            null,
            new Guid.REFIID(new Guid.IID(IShellItemImageFactory.IID)),
            shellItem
        ))) {
            return null;
        }

        var factory = new IShellItemImageFactory(shellItem.getValue());
        try {
            var hbitmap = new PointerByReference();
            if (!COMUtils.SUCCEEDED(factory.GetImage(size, flags, hbitmap))) {
                return null;
            }
            return new WinDef.HBITMAP(hbitmap.getValue());
        }
        finally {
            factory.Release();
        }
    }

    interface ExShell32 extends Shell32 {
        ExShell32 INSTANCE = Native.load("shell32", ExShell32.class, W32APIOptions.DEFAULT_OPTIONS);

        WinNT.HRESULT SHCreateItemFromParsingName(
            WString path,
            Pointer pointer,
            Guid.REFIID guid,
            PointerByReference reference
        );
    }

    static class IShellItemImageFactory extends Unknown {
        public static final String IID = "BCC18B79-BA16-442F-80C4-8A59C30C463B";

        public static final int SIIGBF_BIGGERSIZEOK = 0x00000001;
        public static final int SIIGBF_ICONONLY = 0x00000004;
        public static final int SIIGBF_THUMBNAILONLY = 0x00000008;

        public IShellItemImageFactory(Pointer pvInstance) {
            super(pvInstance);
        }

        public WinNT.HRESULT GetImage(SIZEByValue size, int flags, PointerByReference bitmap) {
            var args = new Object[] {this.getPointer(), size, flags, bitmap};
            return (WinNT.HRESULT) _invokeNativeObject(3, args, WinNT.HRESULT.class);
        }
    }

    static class SIZEByValue extends WinUser.SIZE implements Structure.ByValue {
        public SIZEByValue(int w, int h) {
            super(w, h);
        }
    }
}
