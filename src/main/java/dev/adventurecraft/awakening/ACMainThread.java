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
 * maps directory management, and OpenGL debugging capabilities.
 * 
 * @author Adventurecraft Team
 * @since 1.0.0
 */
@Environment(value = EnvType.CLIENT)
public final class ACMainThread extends Minecraft {

    /**
     * Directory where custom maps are stored.
     * Initialized lazily via {@link #getMapsDirectory()}.
     */
    public static File mapsDirectory;

    /**
     * Flag to enable OpenGL debug context for development and debugging.
     */
    public static boolean glDebugContext;
    
    /**
     * Severity level for OpenGL debug logging.
     * Controls which OpenGL debug messages are logged to the console.
     */
    public static GlDebugSeverity glDebugLogSeverity = GlDebugSeverity.All;
    
    /**
     * Severity level for OpenGL debug stack traces.
     * Controls which OpenGL debug messages trigger stack trace output.
     */
    public static GlDebugSeverity glDebugTraceSeverity = GlDebugSeverity.High;

    /**
     * Constructs a new ACMainThread instance with the specified display parameters.
     * 
     * @param width The width of the game window in pixels
     * @param height The height of the game window in pixels
     * @param fullScreen Whether to start the game in fullscreen mode
     */
    public ACMainThread(int width, int height, boolean fullScreen) {
        super(null, null, null, width, height, fullScreen);
    }

    /**
     * Handles crash scenarios by performing emergency cleanup and saving operations.
     * This method is called when the game encounters a critical error that requires
     * immediate shutdown with data preservation.
     * 
     * @param crashReport The crash report containing details about the error
     * @since 1.0.0
     */
    @Override
    public void onCrash(CrashReport crashReport) {
        try {
            // Perform emergency save to prevent data loss
            this.emergencySave();
            
            // Log crash information for debugging
            System.err.println("[ACMainThread] Game crashed with report: " + crashReport.toString());
            System.err.println("[ACMainThread] Emergency save completed");
            
        } catch (Exception e) {
            // Prevent crash handler from crashing
            System.err.println("[ACMainThread] Error during crash handling: " + e.getMessage());
            e.printStackTrace();
        }
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

    /**
     * Enumeration of OpenGL debug severity levels.
     * Used to control the verbosity of OpenGL debug output and stack traces.
     * 
     * @since 1.0.0
     */
    public enum GlDebugSeverity {
        /** Ignore all debug messages */
        Ignore,
        /** Only high severity messages (critical errors) */
        High,
        /** Medium severity messages (warnings and errors) */
        Medium,
        /** Low severity messages (minor warnings) */
        Low,
        /** Informational messages */
        Info,
        /** All debug messages regardless of severity */
        All,
    }
}