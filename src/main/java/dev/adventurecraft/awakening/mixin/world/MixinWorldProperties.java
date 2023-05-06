package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.common.AC_BlockEffect;
import dev.adventurecraft.awakening.common.WorldGenProperties;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.util.io.AbstractTag;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(WorldProperties.class)
public abstract class MixinWorldProperties implements ExWorldProperties {

    @Shadow
    private boolean raining;

    @Shadow
    private long time;

    public double tempOffset;
    private WorldGenProperties worldGenProps = new WorldGenProperties();
    public boolean iceMelts = true;
    public boolean leavesDecay = true;
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
    private void init(CompoundTag var1, CallbackInfo ci) {
        this.tempOffset = var1.getDouble("TemperatureOffset");
        if (var1.containsKey("IsPrecipitating")) {
            this.raining = var1.getBoolean("IsPrecipitating");
        }

        Entity.field_1590 = var1.getInt("nextEntityID");
        if (var1.containsKey("useImages")) {
            WorldGenProperties wgp = this.worldGenProps;
            wgp.useImages = var1.getBoolean("useImages");
            wgp.mapSize = var1.getDouble("mapSize");
            wgp.waterLevel = var1.getShort("waterLevel");
            wgp.fractureHorizontal = var1.getDouble("fractureHorizontal");
            wgp.fractureVertical = var1.getDouble("fractureVertical");
            wgp.maxAvgDepth = var1.getDouble("maxAvgDepth");
            wgp.maxAvgHeight = var1.getDouble("maxAvgHeight");
            wgp.volatility1 = var1.getDouble("volatility1");
            wgp.volatility2 = var1.getDouble("volatility2");
            wgp.volatilityWeight1 = var1.getDouble("volatilityWeight1");
            wgp.volatilityWeight2 = var1.getDouble("volatilityWeight2");
        }

        if (var1.containsKey("iceMelts")) {
            this.iceMelts = var1.getBoolean("iceMelts");
        }

        if (var1.containsKey("triggerAreas")) {
            this.triggerData = var1.getCompoundTag("triggerAreas");
        }

        if (var1.containsKey("timeRate")) {
            this.timeRate = var1.getFloat("timeRate");
        } else {
            this.timeRate = 1.0F;
        }

        if (var1.containsKey("timeOfDay")) {
            this.timeOfDay = var1.getFloat("timeOfDay");
        } else {
            this.timeOfDay = (float) this.time;
        }

        this.playingMusic = var1.getString("playingMusic");
        if (var1.containsKey("mobsBurn")) {
            this.mobsBurn = var1.getBoolean("mobsBurn");
        }

        this.overlay = var1.getString("overlay");
        if (var1.containsKey("textureReplacements")) {
            this.replacementTag = var1.getCompoundTag("textureReplacements");
        }

        this.onNewSaveScript = var1.getString("onNewSaveScript");
        this.onLoadScript = var1.getString("onLoadScript");
        this.onUpdateScript = var1.getString("onUpdateScript");
        if (var1.containsKey("playerName")) {
            this.playerName = var1.getString("playerName");
        }

        float var2 = 0.05F;

        for (int var3 = 0; var3 < 16; ++var3) {
            String var4 = String.format("brightness%d", var3);
            if (var1.containsKey(var4)) {
                this.brightness[var3] = var1.getFloat(var4);
            } else {
                float var5 = 1.0F - (float) var3 / 15.0F;
                this.brightness[var3] = (1.0F - var5) / (var5 * 3.0F + 1.0F) * (1.0F - var2) + var2;
            }
        }

        if (var1.containsKey("globalScope")) {
            this.globalScope = var1.getCompoundTag("globalScope");
        }

        if (var1.containsKey("worldScope")) {
            this.worldScope = var1.getCompoundTag("worldScope");
        }

        if (var1.containsKey("musicScope")) {
            this.musicScope = var1.getCompoundTag("musicScope");
        }

        if (var1.containsKey("originallyFromAC")) {
            this.originallyFromAC = var1.getBoolean("originallyFromAC");
        } else {
            this.originallyFromAC = var1.containsKey("TemperatureOffset");
        }

        if (var1.containsKey("allowsInventoryCrafting")) {
            this.allowsInventoryCrafting = var1.getBoolean("allowsInventoryCrafting");
        } else {
            this.allowsInventoryCrafting = true;
        }
    }

    @Inject(method = "<init>(JLjava/lang/String;)V", at = @At("TAIL"))
    private void init(long var1, String var2, CallbackInfo ci) {
        this.timeRate = 1.0F;
        float var4 = 0.05F;

        for (int var5 = 0; var5 < 16; ++var5) {
            float var6 = 1.0F - (float) var5 / 15.0F;
            this.brightness[var5] = (1.0F - var6) / (var6 * 3.0F + 1.0F) * (1.0F - var4) + var4;
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/WorldProperties;)V", at = @At("TAIL"))
    private void init(WorldProperties var1, CallbackInfo ci) {
        System.arraycopy(((MixinWorldProperties) (Object) var1).brightness, 0, this.brightness, 0, 16);
    }

    @Inject(method = "getPlayerTag", at = @At("RETURN"))
    private void addToPlayerTag(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag var1 = cir.getReturnValue();
        WorldGenProperties wgp = this.worldGenProps;
        var1.put("TemperatureOffset", this.tempOffset);
        var1.put("nextEntityID", Entity.field_1590);
        var1.put("useImages", wgp.useImages);
        var1.put("mapSize", wgp.mapSize);
        var1.put("waterLevel", (short) wgp.waterLevel);
        var1.put("fractureHorizontal", wgp.fractureHorizontal);
        var1.put("fractureVertical", wgp.fractureVertical);
        var1.put("maxAvgDepth", wgp.maxAvgDepth);
        var1.put("maxAvgHeight", wgp.maxAvgHeight);
        var1.put("volatility1", wgp.volatility1);
        var1.put("volatility2", wgp.volatility2);
        var1.put("volatilityWeight1", wgp.volatilityWeight1);
        var1.put("volatilityWeight2", wgp.volatilityWeight2);
        var1.put("iceMelts", this.iceMelts);
        if (Minecraft.instance.world != null) {
            ExWorld world = (ExWorld) Minecraft.instance.world;
            if (world.getTriggerManager() != null) {
                var1.put("triggerAreas", (AbstractTag) world.getTriggerManager().getTagCompound());
            }
        }

        var1.put("timeOfDay", this.timeOfDay);
        var1.put("timeRate", this.timeRate);
        if (!this.playingMusic.equals("")) {
            var1.put("playingMusic", this.playingMusic);
        }

        var1.put("mobsBurn", this.mobsBurn);
        if (!this.overlay.equals("")) {
            var1.put("overlay", this.overlay);
        }

        var1.put("textureReplacements", this.getTextureReplacementTags());
        if (!this.onNewSaveScript.equals("")) {
            var1.put("onNewSaveScript", this.onNewSaveScript);
        }

        if (!this.onLoadScript.equals("")) {
            var1.put("onLoadScript", this.onLoadScript);
        }

        if (!this.onUpdateScript.equals("")) {
            var1.put("onUpdateScript", this.onUpdateScript);
        }

        if (!this.playerName.equals("")) {
            var1.put("playerName", this.playerName);
        }

        for (int var3 = 0; var3 < 16; ++var3) {
            String var4 = String.format("brightness%d", var3);
            var1.put(var4, this.brightness[var3]);
        }

        if (this.globalScope != null) {
            var1.put("globalScope", (AbstractTag) this.globalScope);
        }

        if (this.worldScope != null) {
            var1.put("worldScope", (AbstractTag) this.worldScope);
        }

        if (this.musicScope != null) {
            var1.put("musicScope", (AbstractTag) this.musicScope);
        }

        var1.put("originallyFromAC", this.originallyFromAC);
        var1.put("allowsInventoryCrafting", this.allowsInventoryCrafting);
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
    public String getPlayerName() {
        return this.playerName;
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
    public boolean addReplacementTexture(String var1, String var2) {
        String var3 = this.replacementTextures.get(var1);
        if (var3 != null && var3.equals(var2)) {
            return false;
        } else {
            this.replacementTextures.put(var1, var2);
            return true;
        }
    }

    @Override
    public void revertTextures() {
        this.replacementTextures.clear();
    }

    @Override
    public CompoundTag getTextureReplacementTags() {
        CompoundTag var2 = new CompoundTag();

        for (Map.Entry<String, String> var4 : this.replacementTextures.entrySet()) {
            var2.put(var4.getKey(), var4.getValue());
        }

        return var2;
    }

    @Override
    public void loadTextureReplacements(World var1) {
        if (this.replacementTag != null) {
            this.replacementTextures.clear();

            for (String var3 : ((ExCompoundTag) this.replacementTag).getKeys()) {
                AC_BlockEffect.replaceTexture(var1, var3, this.replacementTag.getString(var3));
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
        return overrideFogColor;
    }

    @Override
    public void setOverrideFogColor(boolean overrideFogColor) {
        this.overrideFogColor = overrideFogColor;
    }

    @Override
    public boolean getIceMelts() {
        return this.iceMelts;
    }

    @Override
    public void setIceMelts(boolean iceMelts) {
        this.iceMelts = iceMelts;
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
    public void setMobsBurn(boolean mobsBurn) {
        this.mobsBurn = mobsBurn;
    }

    @Override
    public float getFogR() {
        return this.fogR;
    }

    @Override
    public void setFogR(float fogR) {
        this.fogR = fogR;
    }

    @Override
    public float getFogG() {
        return this.fogG;
    }

    @Override
    public void setFogG(float fogG) {
        this.fogG = fogG;
    }

    @Override
    public float getFogB() {
        return this.fogB;
    }

    @Override
    public void setFogB(float fogB) {
        this.fogB = fogB;
    }

    @Override
    public boolean isOverrideFogDensity() {
        return overrideFogDensity;
    }

    @Override
    public void setOverrideFogDensity(boolean overrideFogDensity) {
        this.overrideFogDensity = overrideFogDensity;
    }

    @Override
    public float getFogStart() {
        return fogStart;
    }

    @Override
    public void setFogStart(float fogStart) {
        this.fogStart = fogStart;
    }

    @Override
    public float getFogEnd() {
        return fogEnd;
    }

    @Override
    public void setFogEnd(float fogEnd) {
        this.fogEnd = fogEnd;
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
    public boolean isOriginallyFromAC() {
        return originallyFromAC;
    }
}
