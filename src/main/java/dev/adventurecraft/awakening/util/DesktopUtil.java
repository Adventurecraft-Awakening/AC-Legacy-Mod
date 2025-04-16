package dev.adventurecraft.awakening.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DesktopUtil {

    public static void browseFileDirectory(Path path) throws IOException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (!openSystem(os, path.toAbsolutePath().toString())) {
            throw new IOException("Unable to open file on OS " + os);
        }
    }

    private static boolean openSystem(String os, String path) {
        if (os.contains("win")) {
            // Windows command parsing is cursed.
            return run(("explorer /select," + "\"" + path + "\"").split(" ")) == 1;
        }

        // TODO: this needs testing
        if (os.contains("mac")) {
            return run("open", "-R", path) == 0;
        }

        if (run(
            "dbus-send",
            "--session",
            "--print-reply",
            "--dest=org.freedesktop.FileManager1",
            "--type=method_call",
            "/org/freedesktop/FileManager1",
            "org.freedesktop.FileManager1.ShowItems",
            "array:string:file://" + path,
            "string:java-launch"
        ) == 0) {
            return true;
        }

        return run("xdg-open", path) == 0;
    }

    private static int run(String... args) {
        try {
            var p = new ProcessBuilder(args).start();
            if (p.waitFor(1, TimeUnit.SECONDS)) {
                return p.exitValue();
            }
            return -1;
        } catch (IOException | InterruptedException ignored) {
            return -2;
        }
    }
}
