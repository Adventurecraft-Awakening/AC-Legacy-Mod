package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.extension.nbt.ExTag;
import dev.adventurecraft.awakening.tile.AC_BlockEffect;
import dev.adventurecraft.awakening.common.LightHelper;
import dev.adventurecraft.awakening.common.WorldGenProperties;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.world.GameRules;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(LevelData.class)
public abstract class MixinWorldProperties implements ExWorldProperties {

    @Shadow private boolean raining;
    @Shadow private long time;

    private boolean hudEnabled = true;
    public float tempOffset;
    private WorldGenProperties worldGenProps = new WorldGenProperties();
    public CompoundTag triggerData = null;
    float timeOfDay;
    float timeRate;
    public String playingMusic = "";
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
    private GameRules gameRules = new GameRules();

    @Inject(
        method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
    private void init(CompoundTag tag, CallbackInfo ci) {
        var exTag = (ExCompoundTag) tag;

        this.tempOffset = (float) (double) exTag.findDouble("TemperatureOffset").orElse(0.0);
        exTag.findBool("IsPrecipitating").ifPresent(b -> this.raining = b);

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

        exTag.findCompound("triggerAreas").ifPresent(c -> this.triggerData = c);

        this.timeRate = exTag.findFloat("timeRate").orElse(1.0F);
        this.timeOfDay = exTag.findFloat("timeOfDay").orElse((float) this.time);
        this.playingMusic = tag.getString("playingMusic");

        this.overlay = tag.getString("overlay");
        exTag.findCompound("textureReplacements").ifPresent(c -> this.replacementTag = c);

        this.onNewSaveScript = tag.getString("onNewSaveScript");
        this.onLoadScript = tag.getString("onLoadScript");
        this.onUpdateScript = tag.getString("onUpdateScript");

        exTag.findString("playerName").ifPresent(this::setPlayerName);

        for (int i = 0; i < 16; ++i) {
            Optional<Float> value = exTag.findFloat("brightness" + i);
            if (value.isPresent()) {
                this.brightness[i] = value.get();
            }
            else {
                this.brightness[i] = LightHelper.getDefaultLightAtIndex(i);
            }
        }

        exTag.findCompound("globalScope").ifPresent(this::setGlobalScope);
        exTag.findCompound("worldScope").ifPresent(this::setWorldScope);
        exTag.findCompound("musicScope").ifPresent(this::setMusicScope);

        this.originallyFromAC = exTag.findBool("originallyFromAC").orElseGet(() -> tag.hasKey("TemperatureOffset"));

        this.hudEnabled = exTag.findBool("hudEnabled").orElse(true);

        exTag
            .findCompound("gameRules")
            .ifPresentOrElse(t -> this.gameRules.load(t), () -> this.convertToGameRules(exTag));
    }

    /**
     * Recover rule properties from existing saves.
     */
    @Unique
    private void convertToGameRules(ExCompoundTag tag) {
        // TODO: proper versioning

        this.setGameRuleFrom(tag, "iceMelts", GameRules.MELT_ICE);
        this.setGameRuleFrom(tag, "leavesDecay", GameRules.DECAY_LEAVES);
        this.setGameRuleFrom(tag, "mobsBurn", GameRules.SUNBURN_UNDEAD);

        this.setGameRuleFrom(tag, "allowsInventoryCrafting", GameRules.ALLOW_INVENTORY_CRAFTING);
        this.setGameRuleFrom(tag, "canSleep", GameRules.ALLOW_BED);
        this.setGameRuleFrom(tag, "canUseHoe", GameRules.ALLOW_HOE);
        this.setGameRuleFrom(tag, "canUseBonemeal", GameRules.ALLOW_BONEMEAL);
    }

    @Unique
    private <R extends GameRules.Rule<R>> void setGameRuleFrom(ExCompoundTag tag, String key, GameRules.Key<R> rule) {
        tag.findTag(key).ifPresent(t -> this.gameRules.find(rule).setFromTag(t));
    }

    @Inject(
        method = "<init>(JLjava/lang/String;)V",
        at = @At("TAIL")
    )
    private void init(long var1, String var2, CallbackInfo ci) {
        this.timeRate = 1.0F;

        for (int i = 0; i < 16; ++i) {
            this.brightness[i] = LightHelper.getDefaultLightAtIndex(i);
        }
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/storage/LevelData;)V",
        at = @At("TAIL")
    )
    private void init(LevelData var1, CallbackInfo ci) {
        System.arraycopy(((MixinWorldProperties) (Object) var1).brightness, 0, this.brightness, 0, 16);
    }

    @Inject(
        method = "save(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
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
        if (Minecraft.instance.level != null) {
            var world = (ExWorld) Minecraft.instance.level;
            if (world.getTriggerManager() != null) {
                tag.putTag("triggerAreas", world.getTriggerManager().getTagCompound());
            }
        }

        tag.putFloat("timeOfDay", this.timeOfDay);
        tag.putFloat("timeRate", this.timeRate);
        if (!this.playingMusic.isEmpty()) {
            tag.putString("playingMusic", this.playingMusic);
        }

        if (!this.overlay.isEmpty()) {
            tag.putString("overlay", this.overlay);
        }

        tag.putCompoundTag("textureReplacements", this.getTextureReplacementTags());
        if (!this.onNewSaveScript.isEmpty()) {
            tag.putString("onNewSaveScript", this.onNewSaveScript);
        }

        if (!this.onLoadScript.isEmpty()) {
            tag.putString("onLoadScript", this.onLoadScript);
        }

        if (!this.onUpdateScript.isEmpty()) {
            tag.putString("onUpdateScript", this.onUpdateScript);
        }

        if (!this.playerName.isEmpty()) {
            tag.putString("playerName", this.playerName);
        }

        for (int i = 0; i < 16; ++i) {
            tag.putFloat("brightness" + i, this.brightness[i]);
        }

        if (this.globalScope != null) {
            tag.putTag("globalScope", this.globalScope);
        }

        if (this.worldScope != null) {
            tag.putTag("worldScope", this.worldScope);
        }

        if (this.musicScope != null) {
            tag.putTag("musicScope", this.musicScope);
        }

        tag.putBoolean("originallyFromAC", this.originallyFromAC);
        tag.putBoolean("hudEnabled", this.hudEnabled);

        var gameRuleTag = new CompoundTag();
        this.gameRules.save(gameRuleTag);
        tag.putTag("gameRules", gameRuleTag);
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
    public float getTempOffset() {
        return this.tempOffset;
    }

    @Override
    public void setTempOffset(float value) {
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
        }
        else {
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
        this.replacementTextures.forEach(tag::putString);
        return tag;
    }

    @Override
    public void loadTextureReplacements(Level world) {
        if (this.replacementTag == null) {
            return;
        }
        this.replacementTextures.clear();

        ((ExCompoundTag) this.replacementTag).forEach((key, tag) -> ((ExTag) tag)
            .getString()
            .ifPresent(name -> AC_BlockEffect.replaceTexture(world, key, name)));
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

    @Override
    public void setHudEnabled(boolean arg) {
        this.hudEnabled = arg;
    }

    @Override
    public boolean getHudEnabled() {
        return this.hudEnabled;
    }

    public @Override GameRules getGameRules() {
        return this.gameRules;
    }
}
