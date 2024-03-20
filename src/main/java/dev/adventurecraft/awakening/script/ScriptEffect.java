package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.common.LightHelper;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.particle.ParticleEntity;
import net.minecraft.client.render.WorldEventRenderer;
import dev.adventurecraft.awakening.common.AC_BlockEffect;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class ScriptEffect {

    World world;
    WorldEventRenderer renderGlobal;

    ScriptEffect(World world, WorldEventRenderer renderer) {
        this.world = world;
        this.renderGlobal = renderer;
    }

    public ScriptEntity spawnParticle(String type, double x, double y, double z, double vX, double vY, double vZ) {
        ParticleEntity particle = ((ExWorldEventRenderer) this.renderGlobal).spawnParticleR(type, x, y, z, vX, vY, vZ);
        return ScriptEntity.getEntityClass(particle);
    }

    public boolean replaceTexture(String keyName, String var2) {
        return AC_BlockEffect.replaceTexture(this.world, keyName, var2);
    }

    public String getReplaceTexture(String keyName) {
        String value = ((ExWorldProperties) this.world.properties).getReplacementTextures().get(keyName);
        return value == null ? keyName : value;
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

    public void setFogColor(float r, float g, float b) {
        var props = (ExWorldProperties) this.world.properties;
        props.setFogR(r);
        props.setFogG(g);
        props.setFogB(b);
        props.setOverrideFogColor(true);
    }

    public void revertFogColor() {
        ((ExWorldProperties) this.world.properties).setOverrideFogColor(false);
    }

    public void setFogDensity(float start, float end) {
        var props = (ExWorldProperties) this.world.properties;
        props.setFogStart(start);
        props.setFogEnd(end);
        props.setOverrideFogDensity(true);
    }

    public void revertFogDensity() {
        ((ExWorldProperties) this.world.properties).setOverrideFogDensity(false);
    }

    public float getLightRampValue(int index) {
        float[] brightness = ((ExWorldProperties) this.world.properties).getBrightness();
        return brightness[index];
    }

    public void setLightRampValue(int index, float value) {
        float[] brightness = ((ExWorldProperties) this.world.properties).getBrightness();
        if (brightness[index] != value) {
            brightness[index] = value;
            ((ExWorld) this.world).loadBrightness();
            ((ExWorldEventRenderer) Minecraft.instance.worldRenderer).updateAllTheRenderers();
        }
    }

    public void resetLightRampValues() {
        float[] brightness = ((ExWorldProperties) this.world.properties).getBrightness();
        for (int i = 0; i < 16; ++i) {
            brightness[i] = LightHelper.getDefaultLightAtIndex(i);
        }

        ((ExWorld) this.world).loadBrightness();
        ((ExWorldEventRenderer) Minecraft.instance.worldRenderer).updateAllTheRenderers();
    }

    public void registerTextureAnimation(String animationName, String texName, String imageName, int x, int y, int width, int height) {
        var animation = new AC_TextureAnimated(texName, x, y, width, height);
        ((AC_TextureBinder) animation).loadImage(imageName, world);
        ((ExTextureManager) Minecraft.instance.textureManager).registerTextureAnimation(animationName, animation);
    }

    public void unregisterTextureAnimation(String animationName) {
        ((ExTextureManager) Minecraft.instance.textureManager).unregisterTextureAnimation(animationName);
    }

    public void explode(ScriptEntity entity, double x, double y, double z, float power, boolean causeFires) {
        this.world.createExplosion(entity.entity, x, y, z, power, causeFires);
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
