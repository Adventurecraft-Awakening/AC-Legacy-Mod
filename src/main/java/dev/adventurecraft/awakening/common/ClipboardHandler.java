package dev.adventurecraft.awakening.common;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardHandler {
    public static String getClipboard() {
        String var0 = "";
        Clipboard var1 = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable var2 = var1.getContents(null);
        boolean var3 = var2 != null && var2.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (var3) {
            try {
                var0 = (String) var2.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException var5) {
                var5.printStackTrace();
            } catch (IOException var6) {
                var6.printStackTrace();
            }
        }

        return var0;
    }

    public static void setClipboard(String var0) {
        Clipboard var1 = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection var2 = new StringSelection(var0);
        var1.setContents(var2, var2);
    }
}
