package dev.adventurecraft.awakening.util;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class DesktopUtil {

    public static void browseFileDirectory(Path path) throws IOException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (!openSystem(os, path.toAbsolutePath().toString()) && !openDesktopAwt(path.toFile())) {
            throw new IOException("Unable to open file on OS " + os);
        }
    }

    private static boolean openSystem(String os, String path) {
        String escapedPath = "\"" + path + "\"";
        if (os.contains("win")) {
            return run("explorer /select," + escapedPath).equals(Optional.of(1));
        }
        if (os.contains("mac")) {
            return run("open " + escapedPath).equals(Optional.of(0));
        }
        return run("kde-open " + escapedPath)
            .or(() -> run("gnome-open " + escapedPath))
            .or(() -> run("xdg-open " + escapedPath))
            .equals(Optional.of(0));
    }

    private static boolean openDesktopAwt(File file) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            Desktop.getDesktop().browseFileDirectory(file);
            return true;
        }
        return false;
    }

    private static Optional<Integer> run(String command) {
        try {
            var p = new ProcessBuilder(command.split(" ")).start();
            if (p.waitFor(1, TimeUnit.SECONDS)) {
                return Optional.of(p.exitValue());
            }
        } catch (IOException | InterruptedException ignored) {
        }
        return Optional.empty();
    }
}
