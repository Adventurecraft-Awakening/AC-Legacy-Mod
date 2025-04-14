package dev.adventurecraft.awakening.common;

import com.google.common.base.Stopwatch;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.util.ExProgressListener;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class AC_JScriptHandler {

    Level world;
    File scriptDir;
    private final Map<String, AC_JScriptInfo> scripts;
    private final List<AC_JScriptInfo> scriptList;

    public AC_JScriptHandler(Level var1, File var2) {
        this.world = var1;
        this.scriptDir = new File(var2, "scripts");
        this.scripts = new Object2ObjectOpenHashMap<>();
        this.scriptList = new ArrayList<>();
    }

    public Path[] getFiles() {
        if (this.scriptDir.exists()) {
            try (Stream<Path> stream = Files.walk(this.scriptDir.toPath(), 1)) {
                return stream.filter(Files::isRegularFile).toArray(Path[]::new);
            } catch (IOException ex) {
                ACMod.LOGGER.warn("Failed to load scripts.", ex);
            }
        }
        return new Path[]{};
    }

    public String[] getFileNames() {
        return Arrays.stream(this.getFiles())
            .map(path -> path.getFileName().toString())
            .toArray(String[]::new);
    }

    public void loadScripts(ProgressListener progressListener) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this.scripts.clear();
        this.scriptList.clear();

        if (progressListener != null)
            progressListener.progressStart("Loading scripts");

        Path[] filePaths = this.getFiles();
        if (filePaths.length == 0) {
            return;
        }

        try (var executor = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true)) {

            var cxFactory = ((ExWorld) this.world).getScript().getContext().getFactory();
            var queue = new LinkedBlockingQueue<ScriptLoadTask>();

            var taskCounter = new AtomicInteger();
            for (Path path : filePaths) {
                String fileName = path.getFileName().toString();
                String name = fileName.toLowerCase(); // TODO: don't lower-case? (depends on FS)
                if (!name.endsWith(".js")) {
                    return;
                }

                executor.execute(new ScriptLoadTask(path, cxFactory, queue));
                taskCounter.incrementAndGet();
            }

            final int taskCount = taskCounter.get();
            int takeCount = 0;
            while (takeCount < taskCount) {
                ScriptLoadTask task = queue.take();
                takeCount += 1;

                String fileName = task.path.getFileName().toString();
                String name = fileName.toLowerCase(); // TODO: don't lower-case?

                if (task.result != null) {
                    var info = new AC_JScriptInfo(fileName, task.result);
                    this.scripts.put(name, info);
                    this.scriptList.add(info);
                } else if (task.ex != null) {
                    Exception e = task.ex;

                    Minecraft.instance.gui.addMessage("JS: " + e.getMessage());
                    if (e instanceof IOException) {
                        ACMod.LOGGER.error("Failed to read script file \"{}\".", fileName, e);
                    } else if (e instanceof RhinoException) {
                        ACMod.LOGGER.error("Failed to parse script file \"{}\".", fileName, e);
                    }
                }

                if (progressListener instanceof ExProgressListener exProgressListener) {
                    String stage = String.format("%4d / %4d", takeCount, taskCount);
                    exProgressListener.notifyProgress(stage, takeCount / (double) taskCount, false);
                }
            }

            if (progressListener instanceof ExProgressListener exProgressListener) {
                String stage = String.format("%4d / %4d", taskCount, taskCount);
                exProgressListener.notifyProgress(stage, 1, true);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.scriptList.sort(Comparator.comparing(o -> o.name));

            stopwatch.stop();
            ACMod.LOGGER.info("Loaded {} scripts in {}.", this.scripts.size(), stopwatch);
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
                Minecraft.instance.gui.addMessage(String.format("(JS) Missing '%s'", name));
            }
            return null;
        }

        long time = System.nanoTime();
        Object result;
        try {
            result = ((ExWorld) this.world).getScript().runScript(scriptInfo.compiledScript, scope);
        } finally {
            scriptInfo.addTime(System.nanoTime() - time);
        }
        return result;
    }

    public Collection<AC_JScriptInfo> getScripts() {
        return this.scriptList;
    }

    private static class ScriptLoadTask extends ForkJoinTask<Void> {
        public final Path path;
        public final ContextFactory cxFactory;
        public final BlockingQueue<ScriptLoadTask> queue;

        public Script result;
        public Exception ex;

        private ScriptLoadTask(Path path, ContextFactory cxFactory, BlockingQueue<ScriptLoadTask> queue) {
            this.path = path;
            this.cxFactory = cxFactory;
            this.queue = queue;
        }

        @Override
        public Void getRawResult() {
            return null;
        }

        @Override
        protected void setRawResult(Void value) {
        }

        @Override
        protected boolean exec() {
            Context cx = Context.getCurrentContext();
            if (cx == null) {
                //noinspection resource
                cx = this.cxFactory.enterContext();
            }

            // TODO: update charset to UTF-8 in new maps?
            try (var reader = new FileReader(path.toFile(), StandardCharsets.ISO_8859_1)) {
                this.result = cx.compileReader(reader, path.toString(), 1, null);
            } catch (Exception ex) {
                this.ex = ex;
            }

            try {
                this.queue.put(this);
            } catch (InterruptedException ignored) {
            }
            return true;
        }
    }
}
