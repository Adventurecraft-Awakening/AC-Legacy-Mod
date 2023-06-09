package dev.adventurecraft.awakening.extension.world;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.AC_JScriptHandler;
import dev.adventurecraft.awakening.common.AC_MusicScripts;
import dev.adventurecraft.awakening.common.AC_TriggerManager;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.script.Script;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionData;
import org.mozilla.javascript.Scriptable;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ExWorld {

    void initWorld(
        String mapName, DimensionData dimData, String saveName, long seed, Dimension dimension, ProgressListener progressListener);

    BufferedImage loadMapTexture(String var1);

    void updateChunkProvider();

    void loadBrightness();

    void loadMapTextures();

    void loadMapMusic();

    HitResult rayTraceBlocks2(Vec3d var1, Vec3d var2, boolean var3, boolean var4, boolean var5);

    boolean setBlockAndMetadataTemp(int var1, int var2, int var3, int var4, int var5);

    float getLightValue(int var1, int var2, int var3);

    void cancelBlockUpdate(int var1, int var2, int var3, int var4);

    Entity getEntityByID(int var1);

    float getFogStart(float var1, float var2);

    float getFogEnd(float var1, float var2);

    BlockEntity getBlockTileEntityDontCreate(int var1, int var2, int var3);

    double getTemperatureValue(int var1, int var2);

    void setTemperatureValue(int var1, int var2, double var3);

    void undo();

    void redo();

    void resetCoordOrder();

    File getLevelDir();

    String[] getScriptFiles();

    float getTimeOfDay();

    void setTimeOfDay(long var1);

    float getSpawnYaw();

    void setSpawnYaw(float var1);

    AC_UndoStack getUndoStack();

    String[] getMusicList();

    String[] getSoundList();

    AC_TriggerManager getTriggerManager();

    Script getScript();

    AC_JScriptHandler getScriptHandler();

    AC_MusicScripts getMusicScripts();

    Scriptable getScope();

    static World createWorld(
        String mapName, DimensionData dimData, String saveName, long seed, Dimension dimension, ProgressListener progressListener) {
        try {
            World world = (World) ACMod.UNSAFE.allocateInstance(World.class);
            ((ExWorld) world).initWorld(mapName, dimData, saveName, seed, dimension, progressListener);
            return world;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    static World createWorld(String mapName, DimensionData dimData, String saveName, long seed, ProgressListener progressListener) {
        return createWorld(mapName, dimData, saveName, seed, null, progressListener);
    }

    static World createWorld(DimensionData dimData, String saveName, long seed, ProgressListener progressListener) {
        return createWorld(null, dimData, saveName, seed, null, progressListener);
    }
}
