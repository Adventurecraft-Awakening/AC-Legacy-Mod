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
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;
import dev.adventurecraft.awakening.common.AC_BlockEffect;

@SuppressWarnings("unused")
public class ScriptEffect {

    Level world;
    LevelRenderer renderGlobal;

    ScriptEffect(Level world, LevelRenderer renderer) {
        this.world = world;
        this.renderGlobal = renderer;
    }

    public ScriptEntity spawnParticle(String type, double x, double y, double z, double vX, double vY, double vZ) {
        Particle particle = ((ExWorldEventRenderer) this.renderGlobal).spawnParticleR(type, x, y, z, vX, vY, vZ);
        return ScriptEntity.getEntityClass(particle);
    }

    public boolean replaceTexture(String keyName, String var2) {
        return AC_BlockEffect.replaceTexture(this.world, keyName, var2);
    }

    public String getReplaceTexture(String keyName) {
        String value = ((ExWorldProperties) this.world.levelData).getReplacementTextures().get(keyName);
        return value == null ? keyName : value;
    }

    public void revertTextures() {
        AC_BlockEffect.revertTextures(this.world);
    }

    public String getOverlay() {
        return ((ExWorldProperties) this.world.levelData).getOverlay();
    }

    public void setOverlay(String var1) {
        ((ExWorldProperties) this.world.levelData).setOverlay(var1);
    }

    public void clearOverlay() {
        setOverlay("");
    }

    public void setFogColor(float r, float g, float b) {
        var props = (ExWorldProperties) this.world.levelData;
        props.setFogR(r);
        props.setFogG(g);
        props.setFogB(b);
        props.setOverrideFogColor(true);
    }

    public void revertFogColor() {
        ((ExWorldProperties) this.world.levelData).setOverrideFogColor(false);
    }

    public void setFogDensity(float start, float end) {
        var props = (ExWorldProperties) this.world.levelData;
        props.setFogStart(start);
        props.setFogEnd(end);
        props.setOverrideFogDensity(true);
    }

    public void revertFogDensity() {
        ((ExWorldProperties) this.world.levelData).setOverrideFogDensity(false);
    }

    public float getLightRampValue(int index) {
        float[] brightness = ((ExWorldProperties) this.world.levelData).getBrightness();
        return brightness[index];
    }

    public void setLightRampValue(int index, float value) {
        float[] brightness = ((ExWorldProperties) this.world.levelData).getBrightness();
        if (brightness[index] != value) {
            brightness[index] = value;
            ((ExWorld) this.world).loadBrightness();
            ((ExWorldEventRenderer) Minecraft.instance.levelRenderer).updateAllTheRenderers();
        }
    }

    public void resetLightRampValues() {
        float[] brightness = ((ExWorldProperties) this.world.levelData).getBrightness();
        for (int i = 0; i < 16; ++i) {
            brightness[i] = LightHelper.getDefaultLightAtIndex(i);
        }

        ((ExWorld) this.world).loadBrightness();
        ((ExWorldEventRenderer) Minecraft.instance.levelRenderer).updateAllTheRenderers();
    }

    public void registerTextureAnimation(String animationName, String texName, String imageName, int x, int y, int width, int height) {
        var animation = new AC_TextureAnimated(texName, x, y, width, height);
        ((AC_TextureBinder) animation).loadImage(imageName, world);
        ((ExTextureManager) Minecraft.instance.textures).registerTextureAnimation(animationName, animation);
    }

    public void unregisterTextureAnimation(String animationName) {
        ((ExTextureManager) Minecraft.instance.textures).unregisterTextureAnimation(animationName);
    }

    public void explode(ScriptEntity entity, double x, double y, double z, float power, boolean causeFires) {
        this.world.explode(entity.entity, x, y, z, power, causeFires);
    }

    public float getFovModifier() {
        return Minecraft.instance.gameRenderer.fov;
    }

    public void setFovModifier(float var1) {
        Minecraft.instance.gameRenderer.fov = var1;
    }

    public void cancelCutscene() {
        ((ExMinecraft) Minecraft.instance).setCameraActive(false);
    }

    public boolean getIsCameraActive(){
        return ((ExMinecraft) Minecraft.instance).isCameraActive();
    }
}
