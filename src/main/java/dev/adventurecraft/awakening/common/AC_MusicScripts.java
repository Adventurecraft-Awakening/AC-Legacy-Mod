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
        File musicEntryFile = new File(var1, "musicScripts.txt");
        if (!musicEntryFile.exists()) {
            return;
        }

        try {
            var reader = new BufferedReader(new FileReader(musicEntryFile));
            while (reader.ready()) {
                this.processLine(reader.readLine());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(",", 3);
        if (parts.length == 3) {
            this.musicEntries.add(new AC_MusicScriptEntry(parts[0].trim(), parts[1].trim(), parts[2].trim()));
        }
    }

    public AC_MusicScriptEntry executeMusic(CharSequence musicKey) {
        Iterator<AC_MusicScriptEntry> iterator = this.musicEntries.iterator();

        AC_MusicScriptEntry entry;
        do {
            if (!iterator.hasNext()) {
                return null;
            }
            entry = iterator.next();
        }
        while (!entry.musicKey.contentEquals(musicKey));

        this.handler.runScript(entry.scriptFile, this.scope);
        return entry;
    }
}
