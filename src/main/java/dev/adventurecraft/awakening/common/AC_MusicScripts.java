package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.script.Script;
import dev.adventurecraft.awakening.script.ScriptAttachment;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public class AC_MusicScripts {
    private final Map<String, AC_MusicScriptEntry> entries = new LinkedHashMap<>();
    private final Script script;
    private final AC_JScriptHandler handler;
    private final ScriptAttachment attachment;
    public Scriptable scope;

    public AC_MusicScripts(Script script, File mapDirectory, AC_JScriptHandler handler) {
        this.script = script;
        this.handler = handler;
        this.attachment = script.runtime().attachment();
        this.scope = this.attachment.attach(script);
        this.loadMusic(mapDirectory);
    }

    public ScriptAttachment attachment() { return this.attachment; }

    public void loadMusic(File mapDirectory) {
        this.entries.clear();
        var path = mapDirectory.toPath().resolve("musicScripts.txt");
        if (!Files.isRegularFile(path)) return;
        try (var reader = Files.newBufferedReader(path, Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    var entry = new AC_MusicScriptEntry(parts[0].trim(), parts[1].trim(), parts[2].trim());
                    this.entries.putIfAbsent(entry.musicKey, entry);
                }
            }
        } catch (IOException error) {
            ACMod.LOGGER.warn("Failed to load music scripts from {}.", path, error);
        }
    }

    public AC_MusicScriptEntry executeMusic(CharSequence musicKey) {
        AC_MusicScriptEntry entry = this.entries.get(musicKey.toString());
        if (entry == null) return null;
        this.scope = this.attachment.attach(this.script);
        this.handler.runScript(entry.scriptFile, this.scope);
        return entry;
    }
}
