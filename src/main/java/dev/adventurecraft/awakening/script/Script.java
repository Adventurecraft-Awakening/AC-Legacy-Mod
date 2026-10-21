package dev.adventurecraft.awakening.script;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.Closeable;

/** Existing game/script integration facade; the world runtime owns execution and state. */
@SuppressWarnings("unused")
public class Script implements Closeable {
    public static class CustomContextFactory extends WorldScriptRuntime.CustomContextFactory { }
    private final WorldScriptRuntime runtime;
    public final Scriptable globalScope;
    public final ScriptKeyboard keyboard;

    public Script(Level world) { this(new WorldScriptRuntime(world, Minecraft.instance)); }
    public Script(WorldScriptRuntime runtime) {
        this.runtime = java.util.Objects.requireNonNull(runtime);
        this.globalScope = runtime.globalScope();
        this.keyboard = runtime.keyboard();
    }
    public WorldScriptRuntime runtime() { return this.runtime; }
    public Context getContext() { return this.runtime.context(); }
    public void addObject(String name, Object value) { this.runtime.bind(name, value); }
    public void initPlayer(LocalPlayer player) { this.runtime.initPlayer(player); }
    public String runString(String source, String name) { return this.runtime.runString(source, name); }
    public org.mozilla.javascript.Script compileString(String source, String name) { return this.runtime.compileString(source, name); }
    public Scriptable getNewScope() { return this.runtime.newScope(); }
    public Object runScript(org.mozilla.javascript.Script script, Scriptable scope) { return this.runtime.runScript(script, scope); }
    public void processContinuations() { this.runtime.tick(); }
    public void sleep(float seconds) { this.runtime.sleep(seconds); }
    @Override public void close() { this.runtime.close(); }
}
