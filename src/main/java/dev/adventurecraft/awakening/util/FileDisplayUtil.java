package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.text.ByteSizeFormat;

import java.nio.file.Path;

public final class FileDisplayUtil {

    public static StringBuilder colorizePath(StringBuilder path, String separator) {
        final String separatorStyle = "§7";
        final String extensionStyle = "§e";
        final String resetStyle = "§r";
        
        int offset = 0;
        while ((offset = path.indexOf(separator, offset)) != -1) {
            path.insert(offset, separatorStyle);
            offset += separatorStyle.length() + separator.length();

            path.insert(offset, resetStyle);
            offset += resetStyle.length();
        }

        int extensionIndex = path.lastIndexOf(".");
        if (extensionIndex != -1) {
            path.insert(extensionIndex, extensionStyle);
        }
        return path;
    }

    public static String colorizePath(CharSequence path, String separator) {
        return colorizePath(new StringBuilder(path), separator).toString();
    }

    public static String colorizePath(Path path) {
        return colorizePath(path.toString(), path.getFileSystem().getSeparator());
    }

    public static String readableLength(Number length) {
        return ByteSizeFormat.getMetricInstance().format(length);
    }
}

