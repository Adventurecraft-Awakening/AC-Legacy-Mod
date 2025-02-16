package dev.adventurecraft.awakening.common;

import com.google.common.base.Stopwatch;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.util.ExProgressListener;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class AC_JScriptHandler {

    Level world;
    File scriptDir;
    private final Map<String, AC_JScriptInfo> scripts;

    public AC_JScriptHandler(Level var1, File var2) {
        this.world = var1;
        this.scriptDir = new File(var2, "scripts");
        this.scripts = new Object2ObjectOpenHashMap<>();
    }

    public Stream<Path> getFiles() throws IOException {
        if (!this.scriptDir.exists()) {
            return null;
        }
        //noinspection resource
        return Files.walk(this.scriptDir.toPath(), 1).filter(Files::isRegularFile);
    }

    public String[] getFileNames() {
        try {
            Stream<Path> files = this.getFiles();
            if (files == null) {
                return null;
            }
            return files.map(path -> path.getFileName().toString()).toArray(String[]::new);
        } catch (IOException e) {
            return null;
        }
    }

    public void loadScripts(ProgressListener progressListener) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            this.scripts.clear();

            if (progressListener != null)
                progressListener.progressStart("Loading scripts");

            File[] files;
            try {
                Stream<Path> filePaths = this.getFiles();
                if (filePaths == null) {
                    return;
                }
                files = filePaths.map(Path::toFile).toArray(File[]::new);
            } catch (IOException ex) {
                ACMod.LOGGER.warn("Failed to load scripts.", ex);
                return;
            }

            if (files.length == 0) {
                return;
            }

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                String name = fileName.toLowerCase();

                if (name.endsWith(".js")) {
                    // TODO: update charset to UTF-8 in new maps?
                    try (var reader = new FileReader(file, StandardCharsets.ISO_8859_1)) {
                        var script = ((ExWorld) this.world).getScript().compileReader(reader, fileName);
                        this.scripts.put(name, new AC_JScriptInfo(fileName, script));
                    } catch (IOException e) {
                        Minecraft.instance.gui.addMessage("JS: " + e.getMessage());
                        ACMod.LOGGER.error("Failed to read script file \"{}\".", fileName, e);
                    } catch (RhinoException e) {
                        Minecraft.instance.gui.addMessage("JS: " + e.getMessage());
                        ACMod.LOGGER.error("Failed to parse script file \"{}\".", fileName, e);
                    }
                }

                if (progressListener instanceof ExProgressListener exProgressListener) {
                    String stage = String.format("%4d / %4d", i + 1, files.length);
                    exProgressListener.notifyProgress(stage, (double) i / files.length, false);
                }
            }

            if (progressListener instanceof ExProgressListener exProgressListener) {
                String stage = String.format("%4d / %4d", files.length, files.length);
                exProgressListener.notifyProgress(stage, 1, true);
            }
        } finally {
            stopwatch.stop();
            ACMod.LOGGER.info("Loaded {} scripts in {}.", this.scripts.size(), stopwatch);
        }
    }

    public void loadScripts() {
        this.loadScripts(null);
    }

    public Object runScript(String name, Scriptable scope) {
        return this.runScript(name, scope, true);
    }

    public Object runScript(String name, Scriptable scope, boolean printMissing) {
        AC_JScriptInfo scriptInfo = this.scripts.get(name.toLowerCase());
        if (scriptInfo == null) {
            if (printMissing) {
                Minecraft.instance.gui.addMessage(String.format("(JS) Missing '%s'", name));
            }
            return null;
        }

        long time = System.nanoTime();
        Object result;
        try {
            result = ((ExWorld) this.world).getScript().runScript(scriptInfo.compiledScript, scope);
        } finally {
            scriptInfo.addTime(System.nanoTime() - time);
        }
        return result;
    }

    public Collection<AC_JScriptInfo> getScripts() {
        return scripts.values();
    }
}
