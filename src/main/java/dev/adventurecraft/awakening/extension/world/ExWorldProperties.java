package dev.adventurecraft.awakening.extension.world;

import dev.adventurecraft.awakening.common.WorldGenProperties;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

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

    boolean addReplacementTexture(String key, String value);

    void revertTextures();

    CompoundTag getTextureReplacementTags();

    void loadTextureReplacements(Level world);

    float getSpawnYaw();

    void setSpawnYaw(float value);

    boolean isOverrideFogColor();

    void setOverrideFogColor(boolean value);

    boolean getIceMelts();

    void setIceMelts(boolean value);

    boolean getLeavesDecay();

    void setLeavesDecay(boolean value);

    boolean getMobsBurn();

    void setMobsBurn(boolean value);

    float getFogR();

    void setFogR(float value);

    float getFogG();

    void setFogG(float value);

    float getFogB();

    void setFogB(float value);

    boolean isOverrideFogDensity();

    void setOverrideFogDensity(boolean value);

    float getFogStart();

    void setFogStart(float value);

    float getFogEnd();

    void setFogEnd(float value);

    String getOverlay();

    void setOverlay(String value);

    Map<String, String> getReplacementTextures();

    boolean isOriginallyFromAC();

    boolean getAllowsInventoryCrafting();

    void setAllowsInventoryCrafting(boolean value);

    String getOnNewSaveScript();

    void setOnNewSaveScript(String value);

    String getOnLoadScript();

    void setOnLoadScript(String value);

    String getOnUpdateScript();

    void setOnUpdateScript(String value);

    CompoundTag getGlobalScope();

    void setGlobalScope(CompoundTag value);

    CompoundTag getWorldScope();

    void setWorldScope(CompoundTag value);

    CompoundTag getMusicScope();

    void setMusicScope(CompoundTag value);

    void setHudEnabled(boolean arg);

    boolean getHudEnabled();

    void setCanSleep(boolean arg);

    boolean getCanSleep();
  
    void setCanUseHoe(boolean arg);

    boolean getCanUseHoe();
}
