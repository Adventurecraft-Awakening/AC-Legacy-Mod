package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.WorldEventRenderer;
import dev.adventurecraft.awakening.common.AC_BlockEffect;
import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class ScriptEffect {

    World world;
    WorldEventRenderer renderGlobal;

    ScriptEffect(World var1, WorldEventRenderer var2) {
        this.world = var1;
        this.renderGlobal = var2;
    }

    public ScriptEntity spawnParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12) {
        return ScriptEntity.getEntityClass(((ExWorldEventRenderer) this.renderGlobal).spawnParticleR(var1, var2, var4, var6, var8, var10, var12));
    }

    public boolean replaceTexture(String var1, String var2) {
        return AC_BlockEffect.replaceTexture(this.world, var1, var2);
    }

    public String getReplaceTexture(String key) {
        String value = ((ExWorldProperties) this.world.properties).getReplacementTextures().get(key);
        return value == null ? key : value;
    }

    public void revertTextures() {
        AC_BlockEffect.revertTextures(this.world);
    }

    public String getOverlay() {
        return ((ExWorldProperties) this.world.properties).getOverlay();
    }

    public void setOverlay(String var1) {
        ((ExWorldProperties) this.world.properties).setOverlay(var1);
    }

    public void clearOverlay() {
        setOverlay("");
    }

    public void setFogColor(float var1, float var2, float var3) {
        var props = (ExWorldProperties) this.world.properties;
        props.setFogR(var1);
        props.setFogG(var2);
        props.setFogB(var3);
        props.setOverrideFogColor(true);
    }

    public void revertFogColor() {
        ((ExWorldProperties) this.world.properties).setOverrideFogColor(false);
    }

    public void setFogDensity(float var1, float var2) {
        var props = (ExWorldProperties) this.world.properties;
        props.setFogStart(var1);
        props.setFogEnd(var2);
        props.setOverrideFogDensity(true);
    }

    public void revertFogDensity() {
        ((ExWorldProperties) this.world.properties).setOverrideFogDensity(false);
    }

    public float getLightRampValue(int var1) {
        return ((ExWorldProperties) this.world.properties).getBrightness()[var1];
    }

    public void setLightRampValue(int var1, float var2) {
        ((ExWorldProperties) this.world.properties).getBrightness()[var1] = var2;
        ((ExWorld) this.world).loadBrightness();
        ((ExWorldEventRenderer) Minecraft.instance.worldRenderer).updateAllTheRenderers();
    }

    public void resetLightRampValues() {
        float var1 = 0.05F;

        float[] brightness = ((ExWorldProperties) this.world.properties).getBrightness();
        for (int var2 = 0; var2 < 16; ++var2) {
            float var3 = 1.0F - (float) var2 / 15.0F;
            brightness[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * (1.0F - var1) + var1;
        }

        ((ExWorld) this.world).loadBrightness();
        ((ExWorldEventRenderer) Minecraft.instance.worldRenderer).updateAllTheRenderers();
    }

    public void registerTextureAnimation(String var1, String var2, String var3, int var4, int var5, int var6, int var7) {
        ((ExTextureManager) Minecraft.instance.textureManager).registerTextureAnimation(var1, new AC_TextureAnimated(var2, var3, var4, var5, var6, var7));
    }

    public void unregisterTextureAnimation(String var1) {
        ((ExTextureManager) Minecraft.instance.textureManager).unregisterTextureAnimation(var1);
    }

    public void explode(ScriptEntity var1, double var2, double var4, double var6, float var8, boolean var9) {
        this.world.createExplosion(var1.entity, var2, var4, var6, var8, var9);
    }

    public float getFovModifier() {
        return Minecraft.instance.gameRenderer.field_2365;
    }

    public void setFovModifier(float var1) {
        Minecraft.instance.gameRenderer.field_2365 = var1;
    }

    public void cancelCutscene() {
        ((ExMinecraft) Minecraft.instance).setCameraActive(false);
    }
}
