package dev.adventurecraft.awakening;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CrashReport;
import net.minecraft.client.Minecraft;

import java.io.File;

/**
 * Extended Minecraft client implementation for Adventurecraft Legacy Mod.
 * This class provides additional functionality and configuration options
 * specific to the Adventurecraft mod, including custom crash handling,
 * and maps directory management.
 * 
 * @author Adventurecraft Team
 */
@Environment(value = EnvType.CLIENT)
public final class ACMainThread extends Minecraft {

    public static Thread MAIN_THREAD;

    public static final int MAIN_PRIORITY = 8;

    // Keep this constant between 3 and 7 - some places use an offset.
    public static final int WORKER_PRIORITY = 4;

    /**
     * Directory where custom maps are stored.
     * Initialized lazily via {@link #getMapsDirectory()}.
     */
    public static File mapsDirectory;

    /**
     * Constructs a new ACMainThread instance with the specified display parameters.
     * 
     * @param width The width of the game window in pixels
     * @param height The height of the game window in pixels
     * @param fullScreen Whether to start the game in fullscreen mode
     */
    public ACMainThread(int width, int height, boolean fullScreen) {
        super(null, null, null, width, height, fullScreen);
        MAIN_THREAD = Thread.currentThread();
        MAIN_THREAD.setPriority(MAIN_PRIORITY);
    }

    /**
     * Called to display native dialog to user after the game loop handles a critical error.
     * 
     * @param crashReport The crash report containing details about the error
     */
    @Override
    public void onCrash(CrashReport crashReport) {
        //noinspection StringConcatenationArgumentToLogCall: propagate throwable
        ACMod.LOGGER.error("Game crashed - " + crashReport.title, crashReport.e);
    }

    /**
     * Gets the directory where custom maps are stored.
     * The directory is located relative to the working directory at "../maps".
     * This method uses lazy initialization to create the File object only when needed.
     * 
     * @return The maps directory as a File object
     */
    public static File getMapsDirectory() {
        if (mapsDirectory == null) {
            mapsDirectory = new File(getWorkingDirectory(), "../maps");
        }
        return mapsDirectory;
    }

}
