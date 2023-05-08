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
            File[] var1 = this.scriptDir.listFiles();
            for (File var4 : var1) {
                String var5 = var4.getName().toLowerCase();
                if (var5.endsWith(".js")) {
                    ACMod.LOGGER.info(String.format("Compiling %s", var5));
                    String var6 = this.readFile(var4);
                    this.scripts.put(var5, new AC_JScriptInfo(var4.getName(), ((ExWorld) this.world).getScript().compileString(var6, var4.getName())));
                }
            }
        }

    }

    public Object runScript(String var1, Scriptable var2) {
        return this.runScript(var1, var2, true);
    }

    public Object runScript(String var1, Scriptable var2, boolean var3) {
        AC_JScriptInfo var4 = this.scripts.get(var1.toLowerCase());
        if (var4 != null) {
            var1 = var1.toLowerCase();
            long var5 = System.nanoTime();

            Object var7;
            try {
                var7 = ((ExWorld) this.world).getScript().runScript(var4.compiledScript, var2);
            } finally {
                var4.addStat(System.nanoTime() - var5);
            }

            return var7;
        } else {
            if (var3) {
                Minecraft.instance.overlay.addChatMessage(String.format("Missing '%s'", var1));
            }

            return null;
        }
    }

    private String readFile(File var1) {
        try {
            BufferedReader var2 = new BufferedReader(new FileReader(var1));
            StringBuilder var3 = new StringBuilder();

            try {
                while (var2.ready()) {
                    var3.append(var2.readLine()).append("\n");
                }
            } catch (IOException var5) {
                var5.printStackTrace();
            }

            return var3.toString();
        } catch (FileNotFoundException var6) {
            var6.printStackTrace();
            return "";
        }
    }
}
