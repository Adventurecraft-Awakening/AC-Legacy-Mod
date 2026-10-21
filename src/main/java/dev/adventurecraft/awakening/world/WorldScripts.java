package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.common.AC_JScriptHandler;
import dev.adventurecraft.awakening.common.AC_MusicScripts;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.script.Script;
import dev.adventurecraft.awakening.script.ScriptAttachment;
import dev.adventurecraft.awakening.script.ScriptState;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.Scriptable;

import java.io.File;

/** Loads, persists and closes the three world script scopes as one lifecycle. */
public final class WorldScripts implements AutoCloseable {
    private final File directory;
    private final Script script;
    private final AC_JScriptHandler handler;
    private final ScriptAttachment world;
    private AC_MusicScripts music;

    public WorldScripts(Level level, File directory) {
        this.directory = directory;
        this.script = new Script(level);
        this.handler = new AC_JScriptHandler(this.script.runtime(), directory);
        this.world = this.script.runtime().attachment();
    }

    // Publish script()/handler() to the vanilla facade before loading map scripts.
    public void load(ExWorldProperties properties, ProgressListener progress) {
        ScriptState.load(this.script.globalScope, properties.getGlobalScope());
        this.handler.loadScripts(progress);
        this.music = new AC_MusicScripts(this.script, this.directory, this.handler);
        this.music.attachment().load(properties.getMusicScope());
        this.world.load(properties.getWorldScope());
        this.world.attach(this.script);
    }

    public Script script() { return this.script; }
    public AC_JScriptHandler handler() { return this.handler; }
    public AC_MusicScripts music() { return this.music; }
    public Scriptable scope() { return this.world.attach(this.script); }

    public void save(ExWorldProperties properties) {
        properties.setGlobalScope(ScriptState.save(this.script.globalScope));
        properties.setWorldScope(this.world.save());
        properties.setMusicScope(this.music.attachment().save());
    }

    @Override public void close() { this.script.close(); }
}
