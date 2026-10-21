package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.script.ScriptCatalog;
import dev.adventurecraft.awakening.script.WorldScriptRuntime;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

/** Compatibility facade for a world's shared script catalog and execution statistics. */
public class AC_JScriptHandler {
    private final WorldScriptRuntime runtime;
    private final ScriptCatalog catalog;
    public AC_JScriptHandler(Level world, File mapDirectory) {
        this(((ExWorld) world).getScript().runtime(), mapDirectory);
    }

    public AC_JScriptHandler(WorldScriptRuntime runtime, File mapDirectory) {
        this.runtime = runtime;
        this.catalog = this.runtime.catalog();
        this.catalog.directory(mapDirectory.toPath().resolve("scripts"));
    }
    public Path[] getFiles() { return this.catalog.files(); }
    public String[] getFileNames() { return this.catalog.fileNames(); }
    public void loadScripts(ProgressListener progress) { this.catalog.load(progress); }
    public void loadScripts() { this.loadScripts(null); }
    public Object runScript(String name, Scriptable scope) { return this.runScript(name, scope, true); }
    public Object runScript(String name, Scriptable scope, boolean printMissing) { return this.catalog.run(name, scope, printMissing); }
    public Collection<AC_JScriptInfo> getScripts() { return this.runtime.statistics(); }
}
