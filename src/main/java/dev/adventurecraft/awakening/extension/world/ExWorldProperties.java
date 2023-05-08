package dev.adventurecraft.awakening.extension.world;

import dev.adventurecraft.awakening.common.WorldGenProperties;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

import java.util.Map;

public interface ExWorldProperties {

    WorldGenProperties getWorldGenProps();

    CompoundTag getTriggerData();

    String getPlayingMusic();

    void setPlayingMusic(String value);

    String getPlayerName();

    void setPlayerName(String value);

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

    Map<String, String> getReplacementTextures();

    boolean isOriginallyFromAC();

    boolean getAllowsInventoryCrafting();

    void setAllowsInventoryCrafting(boolean value);

    String getOnNewSaveScript();

    void setOnNewSaveScript(String onNewSaveScript);

    String getOnLoadScript();

    void setOnLoadScript(String onLoadScript);

    String getOnUpdateScript();

    void setOnUpdateScript(String onUpdateScript);

    CompoundTag getGlobalScope();

    void setGlobalScope(CompoundTag value);

    CompoundTag getWorldScope();

    void setWorldScope(CompoundTag value);

    CompoundTag getMusicScope();

    void setMusicScope(CompoundTag value);
}
