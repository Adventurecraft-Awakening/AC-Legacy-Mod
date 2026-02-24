package dev.adventurecraft.awakening;

import dev.adventurecraft.awakening.util.CustomForkJoinWorkerThreadFactory;
import dev.adventurecraft.awakening.util.FabricUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.gson.*;

import java.io.StringReader;
import java.util.HashMap;
import java.util.concurrent.*;

public class ACMod implements ModInitializer {

    public static final String MOD_ID = "adventurecraft";

    public static boolean chunkIsNotPopulating = true;

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("AC");
    public static final Logger CHAT_LOGGER = LoggerFactory.getLogger("Chat");
    public static final Logger JS_LOGGER = LoggerFactory.getLogger("JS");
    public static final Logger GL_LOGGER = LoggerFactory.getLogger("GL");

    public static @Nullable ModContainer MOD_CONTAINER;
    public static @Nullable GitMetadata GIT_META;

    public static ExecutorService WORLD_IO_EXECUTOR;
    public static ExecutorService WORLD_GEN_EXECUTOR;
    public static ExecutorService CHUNK_MESH_EXECUTOR;

    static {
        // Over-allocating threads should be fine since single-player has to wait
        // for tasks to finish no matter what, so going at max speed should be optimal.
        int processors = Math.max(1, Runtime.getRuntime().availableProcessors());
        {
            // IO is expected to do many blocking operations - virtual threads should be optimal for this.
            ThreadFactory factory = Thread.ofVirtual().name("World-IO-Worker", 0).factory();
            WORLD_IO_EXECUTOR = Executors.newFixedThreadPool(processors, factory);
        }
        {
            var factory = new CustomForkJoinWorkerThreadFactory()
                .name("World-Gen-Worker", 0)
                .priority(ACMainThread.WORKER_PRIORITY - 1);
            WORLD_GEN_EXECUTOR = new ForkJoinPool(processors, factory, null, true);
        }
        {
            var factory = new CustomForkJoinWorkerThreadFactory()
                .name("Chunk-Mesh-Worker", 0)
                .priority(ACMainThread.WORKER_PRIORITY + 1);
            CHUNK_MESH_EXECUTOR = new ForkJoinPool(processors, factory, null, true);
        }
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null);

        this.setupGitMetadata();
    }

    private void setupGitMetadata() {
        if (MOD_CONTAINER == null) {
            LOGGER.error("Missing mod container for ID {}.", MOD_ID);
            return;
        }

        var customValues = new HashMap<String, JsonElement>();
        for (var entry : MOD_CONTAINER.getMetadata().getCustomValues().entrySet()) {
            customValues.put(entry.getKey(), FabricUtil.toJson(entry.getValue()));
        }
        LOGGER.info("Mod container custom metadata: {}", customValues);

        try {
            String gitElement = String.valueOf(customValues.get("git"));
            GIT_META = new Gson().fromJson(new StringReader(gitElement), GitMetadata.class);
        }
        catch (Exception e) {
            LOGGER.error("Failed to parse Git metadata.", e);
        }
    }

    public static String getResourceName(String name) {
        return "/assets/adventurecraft/" + name;
    }
}

