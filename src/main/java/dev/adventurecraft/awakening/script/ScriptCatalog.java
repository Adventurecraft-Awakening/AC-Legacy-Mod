package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.AC_JScriptInfo;
import dev.adventurecraft.awakening.extension.util.ExProgressListener;
import net.minecraft.util.ProgressListener;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

/** Source discovery/compilation for a world, with profiling/execution owned by its runtime. */
public final class ScriptCatalog {
    private final WorldScriptRuntime runtime;
    private final Map<String, AC_JScriptInfo> scripts = new LinkedHashMap<>();
    private Path directory;

    ScriptCatalog(WorldScriptRuntime runtime) { this.runtime = runtime; }
    public void directory(Path directory) { this.directory = Objects.requireNonNull(directory); }
    public Path[] files() {
        if (this.directory == null || !Files.isDirectory(this.directory)) return new Path[0];
        try (var files = Files.list(this.directory)) {
            return files.filter(Files::isRegularFile).sorted().toArray(Path[]::new);
        } catch (IOException error) {
            ACMod.LOGGER.warn("Failed to load scripts.", error);
            return new Path[0];
        }
    }
    public String[] fileNames() { return Arrays.stream(this.files()).map(path -> path.getFileName().toString()).toArray(String[]::new); }
    public void clear() {
        this.scripts.forEach(this.runtime::forget);
        this.scripts.clear();
    }

    public void load(ProgressListener progress) {
        long start = System.nanoTime();
        this.clear();
        if (progress != null) progress.progressStart("Loading scripts");
        List<Path> files = Arrays.stream(this.files()).filter(path -> WorldScriptRuntime.key(path.getFileName().toString()).endsWith(".js")).toList();
        var tasks = new ExecutorCompletionService<LoadedScript>(ACMod.WORLD_IO_EXECUTOR);
        ContextFactory factory = this.runtime.context().getFactory();
        for (Path file : files) tasks.submit(() -> compile(file, factory));
        try {
            for (int count = 1; count <= files.size(); count++) {
                LoadedScript loaded = tasks.take().get();
                String name = loaded.path.getFileName().toString();
                if (loaded.error != null) this.runtime.loadFailure(name, loaded.error);
                else this.scripts.put(WorldScriptRuntime.key(name), this.runtime.register(name, loaded.script));
                if (progress instanceof ExProgressListener listener) {
                    listener.notifyProgress(String.format("%4d / %4d", count, files.size()), count / (double) files.size(), false);
                }
            }
            if (progress instanceof ExProgressListener listener && !files.isEmpty()) {
                listener.notifyProgress(String.format("%4d / %4d", files.size(), files.size()), 1, true);
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted loading map scripts", error);
        } catch (ExecutionException error) {
            throw new IllegalStateException("Failed loading map scripts", error.getCause());
        } finally {
            ACMod.LOGGER.info("Loaded {} scripts in {} ms.", this.scripts.size(), (System.nanoTime() - start) / 1_000_000);
        }
    }

    public Object run(String name, Scriptable scope, boolean printMissing) {
        AC_JScriptInfo script = this.scripts.get(WorldScriptRuntime.key(name));
        if (script != null) return this.runtime.runScript(script, scope);
        if (printMissing) this.runtime.missing(name);
        return null;
    }

    private static LoadedScript compile(Path path, ContextFactory factory) {
        // Balance the context entry on every worker, including when it already has one.
        try (Context context = factory.enterContext(); var reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            return new LoadedScript(path, context.compileReader(reader, path.getFileName().toString(), 1, null), null);
        } catch (Exception error) {
            return new LoadedScript(path, null, error);
        }
    }
    private record LoadedScript(Path path, org.mozilla.javascript.Script script, Exception error) { }
}
