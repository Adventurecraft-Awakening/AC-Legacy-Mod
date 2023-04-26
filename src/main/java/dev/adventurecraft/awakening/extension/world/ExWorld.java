package dev.adventurecraft.awakening.extension.world;

import dev.adventurecraft.awakening.common.AC_TriggerManager;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import net.minecraft.class_366;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionData;
import net.minecraft.world.dimension.McRegionDimensionFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

public interface ExWorld {

    void initWorld(String var1, DimensionData var2, String var3);

    BufferedImage loadMapTexture(String var1);

    void updateChunkProvider();

    void loadBrightness();

    void loadMapTextures();

    void loadMapMusic();

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

    float getTimeOfDay();

    void setTimeOfDay(long var1);

    float getSpawnYaw();

    void setSpawnYaw(float var1);

    AC_UndoStack getUndoStack();

    String[] getMusicList();

    String[] getSoundList();

    AC_TriggerManager getTriggerManager();

    static World createWorld(String mapName, DimensionData var2, String var3, long var4, Dimension var6) {
        World world = new World(var2, var3, var4, var6);
        ((ExWorld) world).initWorld(mapName, var2, var3);
        return world;
    }

    static World createWorld(String mapName, DimensionData var2, String var3, long var4) {
        return createWorld(mapName, var2, var3, var4, null);
    }
}
