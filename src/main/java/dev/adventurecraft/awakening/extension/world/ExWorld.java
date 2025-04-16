package dev.adventurecraft.awakening.extension.world;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.script.Script;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.storage.LevelIO;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.util.ArrayList;

public interface ExWorld {

    void initWorld(
        String mapName, LevelIO dimData, String saveName, long seed, Dimension dimension, ProgressListener progressListener);

    ImageBuffer loadMapTexture(String name);

    void updateChunkProvider();

    void loadBrightness();

    void loadMapTextures();

    void loadMapMusic();

    void ac$preTick();

    HitResult rayTraceBlocks2(
        Vec3 pointA, Vec3 pointB,
        boolean blockCollidableFlag, boolean useCollisionShapes, boolean collideWithClip);

    HitResult rayTraceBlocksCore(
        Vec3 pointA, Vec3 pointB,
        boolean blockCollidableFlag, boolean useCollisionShapes, boolean collideWithClip);

    void recordRayDebugList(
        double aX, double aY, double aZ, double bX, double bY, double bZ, HitResult hit);

    boolean setBlockAndMetadataTemp(int x, int y, int z, int id, int meta);

    float getLightValue(int x, int y, int z);

    void cancelBlockUpdate(int x, int y, int z, int var4);

    Entity getEntityByID(int id);

    float getFogStart(float var1, float var2);

    float getFogEnd(float var1, float var2);

    TileEntity getBlockTileEntityDontCreate(int x, int y, int z);

    double getTemperatureValue(int x, int z);

    void setTemperatureValue(int x, int z, double value);

    void resetCoordOrder();

    File getLevelDir();

    String[] getScriptFiles();

    float getTimeOfDay();

    void setTimeOfDay(long value);

    float getSpawnYaw();

    void setSpawnYaw(float value);

    AC_UndoStack getUndoStack();

    ArrayList<String> getMusicList();

    ArrayList<String> getSoundList();

    AC_TriggerManager getTriggerManager();

    Script getScript();

    AC_JScriptHandler getScriptHandler();

    AC_MusicScripts getMusicScripts();

    Scriptable getScope();

    ArrayList<CollisionList> getCollisionDebugLists();

    ArrayList<RayDebugList> getRayDebugLists();

    static Level createWorld(
        String mapName, LevelIO dimData, String saveName, long seed, Dimension dimension, ProgressListener progressListener) {
        try {
            var world = (Level) ACMod.UNSAFE.allocateInstance(Level.class);
            ((ExWorld) world).initWorld(mapName, dimData, saveName, seed, dimension, progressListener);
            return world;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    static Level createWorld(String mapName, LevelIO dimData, String saveName, long seed, ProgressListener progressListener) {
        return createWorld(mapName, dimData, saveName, seed, null, progressListener);
    }

    static Level createWorld(LevelIO dimData, String saveName, long seed, ProgressListener progressListener) {
        return createWorld(null, dimData, saveName, seed, null, progressListener);
    }
}
