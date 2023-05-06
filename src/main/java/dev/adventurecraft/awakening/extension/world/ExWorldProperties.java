package dev.adventurecraft.awakening.extension.world;

import dev.adventurecraft.awakening.common.WorldGenProperties;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

public interface ExWorldProperties {

    WorldGenProperties getWorldGenProps();

    CompoundTag getTriggerData();

    String getPlayingMusic();

    String getPlayerName();

    float[] getBrightness();

    double getTempOffset();

    void setTempOffset(double value);

    long getTimeOfDay();

    void addToTimeOfDay(float var1);

    void setTimeOfDay(float var1);

    float getTimeRate();

    void setTimeRate(float var1);

    boolean addReplacementTexture(String var1, String var2);

    void revertTextures();

    CompoundTag getTextureReplacementTags();

    void loadTextureReplacements(World var1);

    float getSpawnYaw();

    void setSpawnYaw(float value);

    boolean isOverrideFogColor();

    void setOverrideFogColor(boolean overrideFogColor);

    boolean getIceMelts();

    void setIceMelts(boolean value);

    boolean getLeavesDecay();

    void setLeavesDecay(boolean value);

    boolean getMobsBurn();

    void setMobsBurn(boolean value);

    float getFogR();

    void setFogR(float fogR);

    float getFogG();

    void setFogG(float fogG);

    float getFogB();

    void setFogB(float fogB);

    boolean isOverrideFogDensity();

    void setOverrideFogDensity(boolean overrideFogDensity);

    float getFogStart();

    void setFogStart(float fogStart);

    float getFogEnd();

    void setFogEnd(float fogEnd);

    String getOverlay();

    void setOverlay(String value);

    boolean isOriginallyFromAC();
}
