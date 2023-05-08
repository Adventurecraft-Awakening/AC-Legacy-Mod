package dev.adventurecraft.awakening.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import dev.adventurecraft.awakening.script.Script;
import org.mozilla.javascript.Scriptable;

public class AC_MusicScripts {

    ArrayList<AC_MusicScriptEntry> musicEntries = new ArrayList<>();
    AC_JScriptHandler handler;
    public Scriptable scope;

    public AC_MusicScripts(Script var1, File var2, AC_JScriptHandler var3) {
        this.handler = var3;
        this.scope = var1.getNewScope();
        this.loadMusic(var2);
    }

    public void loadMusic(File var1) {
        this.musicEntries.clear();
        File var2 = new File(var1, "musicScripts.txt");
        if (var2.exists()) {
            try {
                BufferedReader var3 = new BufferedReader(new FileReader(var2));

                try {
                    while (var3.ready()) {
                        this.processLine(var3.readLine());
                    }
                } catch (IOException var5) {
                    var5.printStackTrace();
                }
            } catch (FileNotFoundException var6) {
                var6.printStackTrace();
            }
        }
    }

    private void processLine(String var1) {
        String[] var2 = var1.split(",", 3);
        if (var2.length == 3) {
            this.musicEntries.add(new AC_MusicScriptEntry(var2[0].trim(), var2[1].trim(), var2[2].trim()));
        }

    }

    public String executeMusic(String var1) {
        Iterator<AC_MusicScriptEntry> var2 = this.musicEntries.iterator();

        AC_MusicScriptEntry var3;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            var3 = var2.next();
        } while (!var3.music.equals(var1));

        this.handler.runScript(var3.scriptFile, this.scope);
        return var3.songName;
    }
}
