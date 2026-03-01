package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardHandler {

    private static Clipboard getSystemClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    public static String getClipboard() {
        Transferable transferable = getSystemClipboard().getContents(null);
        boolean isString = transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (isString) {
            try {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
            catch (UnsupportedFlavorException | IOException ex) {
                ACMod.LOGGER.warn("Failed to decode clipboard contents.", ex);
            }
        }
        return "";
    }

    public static void setClipboard(String text) {
        var transferable = new StringSelection(text);
        getSystemClipboard().setContents(transferable, transferable);
    }
}
