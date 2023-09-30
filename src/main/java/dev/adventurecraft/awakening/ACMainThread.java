package dev.adventurecraft.awakening;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameStartupError;
import net.minecraft.client.Minecraft;

import java.io.File;

@Environment(value = EnvType.CLIENT)
public final class ACMainThread extends Minecraft {

    public static File mapsDirectory;

    public static boolean glDebugContext;
    public static GlDebugTraceSeverity glDebugTrace = GlDebugTraceSeverity.High;

    public ACMainThread(int width, int height, boolean fullScreen) {
        super(null, null, null, width, height, fullScreen);
    }

    @Override
    public void showGameStartupError(GameStartupError arg) {
        // TODO:
    }

    public static File getMapsDirectory() {
        if (mapsDirectory == null) {
            mapsDirectory = new File(getGameDirectory(), "../maps");
        }
        return mapsDirectory;
    }

    public enum GlDebugTraceSeverity {
        Ignore,
        High,
        Medium,
        Low,
        Info,
        All,
    }
}