package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.World;
import org.mozilla.javascript.Scriptable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class AC_JScriptHandler {

    World world;
    File scriptDir;
    public HashMap<String, AC_JScriptInfo> scripts;

    public AC_JScriptHandler(World var1, File var2) {
        this.world = var1;
        this.scriptDir = new File(var2, "scripts");
        this.scripts = new HashMap<>();
    }

    public void loadScripts(ProgressListener progressListener) {
        this.scripts.clear();

        if (progressListener != null)
            progressListener.notifyIgnoreGameRunning("Loading scripts");

        File[] files = this.scriptDir.listFiles();
        if (files == null) {
            return;
        }

        long lastNotifyTime = 0;

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String fileName = file.getName();
            String name = fileName.toLowerCase();

            if (name.endsWith(".js")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    var script = ((ExWorld) this.world).getScript().compileReader(reader, fileName);
                    this.scripts.put(name, new AC_JScriptInfo(fileName, script));
                } catch (IOException e) {
                    ACMod.LOGGER.error("Failed to read script file \"{}\".", fileName, e);
                }
            }

            if (progressListener != null) {
                long currTime = System.currentTimeMillis();
                if (currTime - lastNotifyTime > 25L) {
                    lastNotifyTime = currTime;

                    progressListener.notifyProgress(String.format("%4d / %4d", i + 1, files.length));
                    progressListener.progressStagePercentage((int) ((100.0 * (double) i / files.length)));
                }
            }
        }

        if (progressListener != null) {
            progressListener.notifyProgress(String.format("%4d / %4d", files.length, files.length));
            progressListener.progressStagePercentage(100);
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
                Minecraft.instance.overlay.addChatMessage(String.format("Missing '%s'", name));
            }

            return null;
        }

        long time = System.nanoTime();
        Object result;
        try {
            result = ((ExWorld) this.world).getScript().runScript(scriptInfo.compiledScript, scope);
        } finally {
            scriptInfo.addStat(System.nanoTime() - time);
        }
        return result;
    }
}
