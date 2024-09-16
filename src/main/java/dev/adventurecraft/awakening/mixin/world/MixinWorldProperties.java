package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.common.AC_BlockEffect;
import dev.adventurecraft.awakening.common.LightHelper;
import dev.adventurecraft.awakening.common.WorldGenProperties;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LevelData.class)
public abstract class MixinWorldProperties implements ExWorldProperties {

    @Shadow
    private boolean raining;

    @Shadow
    private long time;

    private boolean hudEnabled = true;
    public double tempOffset;
    private WorldGenProperties worldGenProps = new WorldGenProperties();
    public boolean iceMelts = true;
    public boolean leavesDecay = false;
    public CompoundTag triggerData = null;
    float timeOfDay;
    float timeRate;
    public String playingMusic = "";
    public boolean mobsBurn = true;
    public boolean overrideFogColor = false;
    public float fogR;
    public float fogG;
    public float fogB;
    public boolean overrideFogDensity = false;
    public float fogStart;
    public float fogEnd;
    public String overlay = "";
    CompoundTag replacementTag = null;
    public HashMap<String, String> replacementTextures = new HashMap<>();
    public String onNewSaveScript = "";
    public String onLoadScript = "";
    public String onUpdateScript = "";
    public String playerName = "ACPlayer";
    public float[] brightness = new float[16];
    public float spawnYaw;
    public CompoundTag globalScope = null;
    public CompoundTag worldScope = null;
    public CompoundTag musicScope = null;
    public boolean originallyFromAC = false;
    public boolean allowsInventoryCrafting = false;

    @Inject(method = "<init>(Lnet/minecraft/util/io/CompoundTag;)V", at = @At("TAIL"))
    private void init(CompoundTag tag, CallbackInfo ci) {
        this.tempOffset = tag.getDouble("TemperatureOffset");
        if (tag.hasKey("IsPrecipitating")) {
            this.raining = tag.getBoolean("IsPrecipitating");
        }

        Entity.ENTITY_COUNTER = tag.getInt("nextEntityID");
        if (tag.hasKey("useImages")) {
            WorldGenProperties wgp = this.worldGenProps;
            wgp.useImages = tag.getBoolean("useImages");
            wgp.mapSize = tag.getDouble("mapSize");
            wgp.waterLevel = tag.getShort("waterLevel");
            wgp.fractureHorizontal = tag.getDouble("fractureHorizontal");
            wgp.fractureVertical = tag.getDouble("fractureVertical");
            wgp.maxAvgDepth = tag.getDouble("maxAvgDepth");
            wgp.maxAvgHeight = tag.getDouble("maxAvgHeight");
            wgp.volatility1 = tag.getDouble("volatility1");
            wgp.volatility2 = tag.getDouble("volatility2");
            wgp.volatilityWeight1 = tag.getDouble("volatilityWeight1");
            wgp.volatilityWeight2 = tag.getDouble("volatilityWeight2");
        }

        if (tag.hasKey("iceMelts")) {
            this.iceMelts = tag.getBoolean("iceMelts");
        }

        if (tag.hasKey("leavesDecay")) {
            this.leavesDecay = tag.getBoolean("leavesDecay");
        }

        if (tag.hasKey("triggerAreas")) {
            this.triggerData = tag.getCompoundTag("triggerAreas");
        }

        if (tag.hasKey("timeRate")) {
            this.timeRate = tag.getFloat("timeRate");
        } else {
            this.timeRate = 1.0F;
        }

        if (tag.hasKey("timeOfDay")) {
            this.timeOfDay = tag.getFloat("timeOfDay");
        } else {
            this.timeOfDay = (float) this.time;
        }

        this.playingMusic = tag.getString("playingMusic");
        if (tag.hasKey("mobsBurn")) {
            this.mobsBurn = tag.getBoolean("mobsBurn");
        }

        this.overlay = tag.getString("overlay");
        if (tag.hasKey("textureReplacements")) {
            this.replacementTag = tag.getCompoundTag("textureReplacements");
        }

        this.onNewSaveScript = tag.getString("onNewSaveScript");
        this.onLoadScript = tag.getString("onLoadScript");
        this.onUpdateScript = tag.getString("onUpdateScript");
        if (tag.hasKey("playerName")) {
            this.playerName = tag.getString("playerName");
        }

        for (int i = 0; i < 16; ++i) {
            String id = String.format("brightness%d", i);
            if (tag.hasKey(id)) {
                this.brightness[i] = tag.getFloat(id);
            } else {
                this.brightness[i] = LightHelper.getDefaultLightAtIndex(i);
            }
        }

        if (tag.hasKey("globalScope")) {
            this.globalScope = tag.getCompoundTag("globalScope");
        }

        if (tag.hasKey("worldScope")) {
            this.worldScope = tag.getCompoundTag("worldScope");
        }

        if (tag.hasKey("musicScope")) {
            this.musicScope = tag.getCompoundTag("musicScope");
        }

        if (tag.hasKey("originallyFromAC")) {
            this.originallyFromAC = tag.getBoolean("originallyFromAC");
        } else {
            this.originallyFromAC = tag.hasKey("TemperatureOffset");
        }

        if (tag.hasKey("allowsInventoryCrafting")) {
            this.allowsInventoryCrafting = tag.getBoolean("allowsInventoryCrafting");
        } else {
            this.allowsInventoryCrafting = true;
        }

        if (tag.hasKey("hudEnabled")) {
            this.hudEnabled = tag.getBoolean("hudEnabled");
        } else {
            this.hudEnabled = true;
        }
    }

    @Inject(method = "<init>(JLjava/lang/String;)V", at = @At("TAIL"))
    private void init(long var1, String var2, CallbackInfo ci) {
        this.timeRate = 1.0F;

        for (int i = 0; i < 16; ++i) {
            this.brightness[i] = LightHelper.getDefaultLightAtIndex(i);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/WorldProperties;)V", at = @At("TAIL"))
    private void init(LevelData var1, CallbackInfo ci) {
        System.arraycopy(((MixinWorldProperties) (Object) var1).brightness, 0, this.brightness, 0, 16);
    }

    @Inject(method = "updateProperties", at = @At("TAIL"))
    private void insertWorldPropsForAC(CompoundTag tag, CompoundTag playerTag, CallbackInfo ci) {
        WorldGenProperties wgp = this.worldGenProps;
        tag.putDouble("TemperatureOffset", this.tempOffset);
        tag.putInt("nextEntityID", Entity.ENTITY_COUNTER);
        tag.putBoolean("useImages", wgp.useImages);
        tag.putDouble("mapSize", wgp.mapSize);
        tag.putShort("waterLevel", (short) wgp.waterLevel);
        tag.putDouble("fractureHorizontal", wgp.fractureHorizontal);
        tag.putDouble("fractureVertical", wgp.fractureVertical);
        tag.putDouble("maxAvgDepth", wgp.maxAvgDepth);
        tag.putDouble("maxAvgHeight", wgp.maxAvgHeight);
        tag.putDouble("volatility1", wgp.volatility1);
        tag.putDouble("volatility2", wgp.volatility2);
        tag.putDouble("volatilityWeight1", wgp.volatilityWeight1);
        tag.putDouble("volatilityWeight2", wgp.volatilityWeight2);
        tag.putBoolean("iceMelts", this.iceMelts);
        tag.putBoolean("leavesDecay", this.leavesDecay);
        if (Minecraft.instance.level != null) {
            ExWorld world = (ExWorld) Minecraft.instance.level;
            if (world.getTriggerManager() != null) {
                tag.putTag("triggerAreas", (Tag) world.getTriggerManager().getTagCompound());
            }
        }

        tag.putFloat("timeOfDay", this.timeOfDay);
        tag.putFloat("timeRate", this.timeRate);
        if (!this.playingMusic.equals("")) {
            tag.putString("playingMusic", this.playingMusic);
        }

        tag.putBoolean("mobsBurn", this.mobsBurn);
        if (!this.overlay.equals("")) {
            tag.putString("overlay", this.overlay);
        }

        tag.putCompoundTag("textureReplacements", this.getTextureReplacementTags());
        if (!this.onNewSaveScript.equals("")) {
            tag.putString("onNewSaveScript", this.onNewSaveScript);
        }

        if (!this.onLoadScript.equals("")) {
            tag.putString("onLoadScript", this.onLoadScript);
        }

        if (!this.onUpdateScript.equals("")) {
            tag.putString("onUpdateScript", this.onUpdateScript);
        }

        if (!this.playerName.equals("")) {
            tag.putString("playerName", this.playerName);
        }

        for (int var3 = 0; var3 < 16; ++var3) {
            String var4 = String.format("brightness%d", var3);
            tag.putFloat(var4, this.brightness[var3]);
        }

        if (this.globalScope != null) {
            tag.putTag("globalScope", (Tag) this.globalScope);
        }

        if (this.worldScope != null) {
            tag.putTag("worldScope", (Tag) this.worldScope);
        }

        if (this.musicScope != null) {
            tag.putTag("musicScope", (Tag) this.musicScope);
        }

        tag.putBoolean("originallyFromAC", this.originallyFromAC);
        tag.putBoolean("allowsInventoryCrafting", this.allowsInventoryCrafting);
        tag.putBoolean("hudEnabled", this.hudEnabled);
    }

    @Override
    public WorldGenProperties getWorldGenProps() {
        return this.worldGenProps;
    }

    @Override
    public CompoundTag getTriggerData() {
        return this.triggerData;
    }

    @Override
    public String getPlayingMusic() {
        return this.playingMusic;
    }

    @Override
    public void setPlayingMusic(String value) {
        this.playingMusic = value;
    }

    @Override
    public String getPlayerName() {
        return this.playerName;
    }

    @Override
    public void setPlayerName(String value) {
        this.playerName = value;
    }

    @Override
    public float[] getBrightness() {
        return this.brightness;
    }

    @Override
    public double getTempOffset() {
        return this.tempOffset;
    }

    @Override
    public void setTempOffset(double value) {
        this.tempOffset = value;
    }

    @Override
    public long getTimeOfDay() {
        return (long) this.timeOfDay;
    }

    @Override
    public void addToTimeOfDay(float var1) {
        this.timeOfDay += var1;
        while (this.timeOfDay < 0.0F) {
            this.timeOfDay += 24000.0F;
        }

        while (this.timeOfDay > 24000.0F) {
            this.timeOfDay -= 24000.0F;
        }
    }

    @Override
    public void setTimeOfDay(float var1) {
        this.timeOfDay = var1;
        while (this.timeOfDay < 0.0F) {
            this.timeOfDay += 24000.0F;
        }

        while (this.timeOfDay > 24000.0F) {
            this.timeOfDay -= 24000.0F;
        }
    }

    @Override
    public float getTimeRate() {
        return this.timeRate;
    }

    @Override
    public void setTimeRate(float var1) {
        this.timeRate = var1;
    }

    @Override
    public boolean addReplacementTexture(String key, String value) {
        String found = this.replacementTextures.get(key);
        if (found != null && found.equals(value)) {
            return false;
        } else {
            this.replacementTextures.put(key, value);
            return true;
        }
    }

    @Override
    public void revertTextures() {
        this.replacementTextures.clear();
    }

    @Override
    public CompoundTag getTextureReplacementTags() {
        var tag = new CompoundTag();

        for (Map.Entry<String, String> entry : this.replacementTextures.entrySet()) {
            tag.putString(entry.getKey(), entry.getValue());
        }

        return tag;
    }

    @Override
    public void loadTextureReplacements(Level world) {
        if (this.replacementTag != null) {
            this.replacementTextures.clear();

            for (String replacementKey : ((ExCompoundTag) this.replacementTag).getKeys()) {
                AC_BlockEffect.replaceTexture(world, replacementKey, this.replacementTag.getString(replacementKey));
            }
        }
    }

    @Override
    public float getSpawnYaw() {
        return this.spawnYaw;
    }

    @Override
    public void setSpawnYaw(float value) {
        this.spawnYaw = value;
    }

    @Override
    public boolean isOverrideFogColor() {
        return this.overrideFogColor;
    }

    @Override
    public void setOverrideFogColor(boolean value) {
        this.overrideFogColor = value;
    }

    @Override
    public boolean getIceMelts() {
        return this.iceMelts;
    }

    @Override
    public void setIceMelts(boolean value) {
        this.iceMelts = value;
    }

    @Override
    public boolean getLeavesDecay() {
        return this.leavesDecay;
    }

    @Override
    public void setLeavesDecay(boolean value) {
        this.leavesDecay = value;
    }

    @Override
    public boolean getMobsBurn() {
        return this.mobsBurn;
    }

    @Override
    public void setMobsBurn(boolean value) {
        this.mobsBurn = value;
    }

    @Override
    public float getFogR() {
        return this.fogR;
    }

    @Override
    public void setFogR(float value) {
        this.fogR = value;
    }

    @Override
    public float getFogG() {
        return this.fogG;
    }

    @Override
    public void setFogG(float value) {
        this.fogG = value;
    }

    @Override
    public float getFogB() {
        return this.fogB;
    }

    @Override
    public void setFogB(float value) {
        this.fogB = value;
    }

    @Override
    public boolean isOverrideFogDensity() {
        return this.overrideFogDensity;
    }

    @Override
    public void setOverrideFogDensity(boolean value) {
        this.overrideFogDensity = value;
    }

    @Override
    public float getFogStart() {
        return this.fogStart;
    }

    @Override
    public void setFogStart(float value) {
        this.fogStart = value;
    }

    @Override
    public float getFogEnd() {
        return this.fogEnd;
    }

    @Override
    public void setFogEnd(float value) {
        this.fogEnd = value;
    }

    @Override
    public String getOverlay() {
        return this.overlay;
    }

    @Override
    public void setOverlay(String value) {
        this.overlay = value;
    }

    @Override
    public Map<String, String> getReplacementTextures() {
        return this.replacementTextures;
    }

    @Override
    public boolean isOriginallyFromAC() {
        return this.originallyFromAC;
    }

    @Override
    public boolean getAllowsInventoryCrafting() {
        return this.allowsInventoryCrafting;
    }

    @Override
    public void setAllowsInventoryCrafting(boolean value) {
        this.allowsInventoryCrafting = value;
    }

    @Override
    public String getOnNewSaveScript() {
        return this.onNewSaveScript;
    }

    @Override
    public void setOnNewSaveScript(String value) {
        this.onNewSaveScript = value;
    }

    @Override
    public String getOnLoadScript() {
        return this.onLoadScript;
    }

    @Override
    public void setOnLoadScript(String value) {
        this.onLoadScript = value;
    }

    @Override
    public String getOnUpdateScript() {
        return this.onUpdateScript;
    }

    @Override
    public void setOnUpdateScript(String value) {
        this.onUpdateScript = value;
    }

    @Override
    public CompoundTag getGlobalScope() {
        return this.globalScope;
    }

    @Override
    public void setGlobalScope(CompoundTag value) {
        this.globalScope = value;
    }

    @Override
    public CompoundTag getWorldScope() {
        return this.worldScope;
    }

    @Override
    public void setWorldScope(CompoundTag value) {
        this.worldScope = value;
    }

    @Override
    public CompoundTag getMusicScope() {
        return this.musicScope;
    }

    @Override
    public void setMusicScope(CompoundTag value) {
        this.musicScope = value;
    }

    public void setHudEnabled(boolean arg){
        this.hudEnabled = arg;
    }

    public boolean getHudEnabled(){
        return this.hudEnabled;
    }
}
