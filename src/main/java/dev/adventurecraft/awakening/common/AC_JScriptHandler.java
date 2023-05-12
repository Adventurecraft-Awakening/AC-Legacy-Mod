package dev.adventurecraft.awakening.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import org.mozilla.javascript.Scriptable;

public class AC_JScriptHandler {

    World world;
    File scriptDir;
    public HashMap<String, AC_JScriptInfo> scripts;

    public AC_JScriptHandler(World var1, File var2) {
        this.world = var1;
        this.scriptDir = new File(var2, "scripts");
        this.scripts = new HashMap<>();
        this.loadScripts();
    }

    public void loadScripts() {
        this.scripts.clear();
        if (this.scriptDir.exists()) {
            File[] files = this.scriptDir.listFiles();
            for (File file : files) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".js")) {
                    ACMod.LOGGER.info(String.format("Compiling %s", name));
                    String contents = this.readFile(file);
                    this.scripts.put(name, new AC_JScriptInfo(
                        file.getName(),
                        ((ExWorld) this.world).getScript().compileString(contents, file.getName())));
                }
            }
        }
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

    private String readFile(File var1) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(var1));
            StringBuilder builder = new StringBuilder();

            try {
                while (reader.ready()) {
                    builder.append(reader.readLine()).append("\n");
                }
            } catch (IOException var5) {
                var5.printStackTrace();
            }

            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
