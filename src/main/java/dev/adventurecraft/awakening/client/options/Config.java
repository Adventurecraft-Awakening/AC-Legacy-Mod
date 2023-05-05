package dev.adventurecraft.awakening.client.options;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.slf4j.Logger;

public class Config {
    private static GameOptions gameSettings = null;
    private static Minecraft minecraft = null;
    private static float lightLevel0 = 0;
    private static float lightLevel1 = 0;
    private static boolean fontRendererUpdated = false;
    public static final boolean DEF_FOG_FANCY = true;
    public static final float DEF_FOG_START = 0.2F;
    public static final boolean DEF_OPTIMIZE_RENDER_DISTANCE = false;
    public static final boolean DEF_OCCLUSION_ENABLED = false;
    public static final int DEF_MIPMAP_LEVEL = 0;
    public static final int DEF_MIPMAP_TYPE = 9984;
    public static final float DEF_ALPHA_FUNC_LEVEL = 0.1F;
    public static final boolean DEF_LOAD_CHUNKS_FAR = false;
    public static final int DEF_PRELOADED_CHUNKS = 0;
    public static final int DEF_CHUNKS_LIMIT = 25;
    public static final int DEF_UPDATES_PER_FRAME = 3;
    public static final boolean DEF_DYNAMIC_UPDATES = false;

    public static void logOpenGlCaps() {
        Logger logger = ACMod.LOGGER;

        logger.info("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
        logger.info("Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        logger.info("VM: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        logger.info("LWJGL: " + Sys.getVersion());
        logger.info("OpenGL GL_RENDERER: " + GL11.glGetString(GL11.GL_RENDERER));
        logger.info("OpenGL GL_VERSION: " + GL11.glGetString(GL11.GL_VERSION));
        logger.info("OpenGL GL_VENDOR: " + GL11.glGetString(GL11.GL_VENDOR));

        int var0 = getOpenGlVersion();
        String var1 = var0 / 10 + "." + var0 % 10;
        logger.info("OpenGL Version: " + var1);
        if (!GLContext.getCapabilities().OpenGL12) {
            logger.info("OpenGL Mipmap levels: Not available (GL12.GL_TEXTURE_MAX_LEVEL)");
        }

        if (!GLContext.getCapabilities().GL_NV_fog_distance) {
            logger.info("OpenGL Fancy fog: Not available (GL_NV_fog_distance)");
        }

        if (!GLContext.getCapabilities().GL_ARB_occlusion_query) {
            logger.info("OpenGL Occlussion culling: Not available (GL_ARB_occlusion_query)");
        }
    }

    public static boolean isFancyFogAvailable() {
        return GLContext.getCapabilities().GL_NV_fog_distance;
    }

    public static boolean isOcclusionAvailable() {
        return GLContext.getCapabilities().GL_ARB_occlusion_query;
    }

    private static int getOpenGlVersion() {
        if (!GLContext.getCapabilities().OpenGL11) return 10;
        if (!GLContext.getCapabilities().OpenGL12) return 11;
        if (!GLContext.getCapabilities().OpenGL13) return 12;
        if (!GLContext.getCapabilities().OpenGL14) return 13;
        if (!GLContext.getCapabilities().OpenGL15) return 14;
        if (!GLContext.getCapabilities().OpenGL20) return 15;
        if (!GLContext.getCapabilities().OpenGL21) return 20;
        if (!GLContext.getCapabilities().OpenGL30) return 21;
        if (!GLContext.getCapabilities().OpenGL31) return 30;
        if (!GLContext.getCapabilities().OpenGL32) return 31;
        if (!GLContext.getCapabilities().OpenGL33) return 32;
        return !GLContext.getCapabilities().OpenGL40 ? 33 : 40;
    }

    public static void setGameSettings(GameOptions var0) {
        gameSettings = var0;
    }

    public static boolean isUseMipmaps() {
        int var0 = getMipmapLevel();
        return var0 > 0;
    }

    public static int getMipmapLevel() {
        return gameSettings == null ? DEF_MIPMAP_LEVEL : ((ExGameOptions) gameSettings).ofMipmapLevel();
    }

    public static int getMipmapType() {
        if (gameSettings == null)
            return DEF_MIPMAP_TYPE;
        return ((ExGameOptions) gameSettings).ofMipmapLinear() ? 9986 : 9984;
    }

    public static boolean isUseAlphaFunc() {
        float var0 = getAlphaFuncLevel();
        return var0 > DEF_ALPHA_FUNC_LEVEL + 1.0E-5F;
    }

    public static float getAlphaFuncLevel() {
        return DEF_ALPHA_FUNC_LEVEL;
    }

    public static boolean isFogFancy() {
        if (!GLContext.getCapabilities().GL_NV_fog_distance)
            return false;
        return gameSettings != null && ((ExGameOptions) gameSettings).ofFogFancy();
    }

    public static float getFogStart() {
        return gameSettings == null ? DEF_FOG_START : ((ExGameOptions) gameSettings).ofFogStart();
    }

    public static boolean isOcclusionEnabled() {
        return gameSettings == null ? DEF_OCCLUSION_ENABLED : gameSettings.advancedOpengl;
    }

    public static boolean isOcclusionFancy() {
        if (!isOcclusionEnabled())
            return false;
        return gameSettings != null && ((ExGameOptions) gameSettings).ofOcclusionFancy();
    }

    public static boolean isLoadChunksFar() {
        return gameSettings == null ? DEF_LOAD_CHUNKS_FAR : ((ExGameOptions) gameSettings).ofLoadFar();
    }

    public static int getPreloadedChunks() {
        return gameSettings == null ? DEF_PRELOADED_CHUNKS : ((ExGameOptions) gameSettings).ofPreloadedChunks();
    }

    public static int getUpdatesPerFrame() {
        return gameSettings != null ? ((ExGameOptions) gameSettings).ofChunkUpdates() : 1;
    }

    public static boolean isDynamicUpdates() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofChunkUpdatesDynamic();
    }

    public static boolean isRainFancy() {
        if (((ExGameOptions) gameSettings).ofRain() == 0)
            return gameSettings.fancyGraphics;
        return ((ExGameOptions) gameSettings).ofRain() == 2;
    }

    public static boolean isWaterFancy() {
        if (((ExGameOptions) gameSettings).ofWater() == 0)
            return gameSettings.fancyGraphics;
        return ((ExGameOptions) gameSettings).ofWater() == 2;
    }

    public static boolean isRainOff() {
        return ((ExGameOptions) gameSettings).ofRain() == 3;
    }

    public static boolean isCloudsFancy() {
        if (((ExGameOptions) gameSettings).ofClouds() == 0)
            return gameSettings.fancyGraphics;
        return ((ExGameOptions) gameSettings).ofClouds() == 2;
    }

    public static boolean isCloudsOff() {
        return ((ExGameOptions) gameSettings).ofClouds() == 3;
    }

    public static boolean isLeavesFancy() {
        if (gameSettings == null)
            return false;
        if (((ExGameOptions) gameSettings).ofLeaves() == 0)
            return gameSettings.fancyGraphics;
        return ((ExGameOptions) gameSettings).ofLeaves() == 2;
    }

    public static boolean isGrassFancy() {
        if (gameSettings == null)
            return false;
        if (((ExGameOptions) gameSettings).ofGrass() == 0)
            return gameSettings.fancyGraphics;
        return ((ExGameOptions) gameSettings).ofGrass() == 2;
    }

    public static int limit(int var0, int var1, int var2) {
        return var0 < var1 ? var1 : Math.min(var0, var2);
    }

    public static float limit(float var0, float var1, float var2) {
        return var0 < var1 ? var1 : Math.min(var0, var2);
    }

    public static boolean isAnimatedWater() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedWater() != 2;
    }

    public static boolean isGeneratedWater() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedWater() == 1;
    }

    public static boolean isAnimatedPortal() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedPortal();
    }

    public static boolean isAnimatedLava() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedLava() != 2;
    }

    public static boolean isGeneratedLava() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedLava() == 1;
    }

    public static boolean isAnimatedFire() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedFire();
    }

    public static boolean isAnimatedRedstone() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedRedstone();
    }

    public static boolean isAnimatedExplosion() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedExplosion();
    }

    public static boolean isAnimatedFlame() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedFlame();
    }

    public static boolean isAnimatedSmoke() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedSmoke();
    }

    public static float getAmbientOcclusionLevel() {
        return gameSettings != null ? ((ExGameOptions) gameSettings).ofAoLevel() : 0.0F;
    }

    public static float fixAoLight(float var0, float var1) {
        if (var0 > lightLevel0) {
            return var0;
        } else if (var1 <= lightLevel1) {
            return var0;
        } else {
            float var4 = 1.0F - getAmbientOcclusionLevel();
            return var0 + (var1 - var0) * var4;
        }
    }

    public static void setLightLevels(float[] var0) {
        lightLevel0 = var0[0];
        lightLevel1 = var0[1];
    }

    public static void setMinecraft(Minecraft var0) {
        minecraft = var0;
    }

    public static Minecraft getMinecraft() {
        return minecraft;
    }

    public static ConnectedGrassOption getConnectedGrassOption() {
        if (gameSettings != null) {
            return ((ExGameOptions) gameSettings).ofConnectedGrass();
        }
        return ConnectedGrassOption.OFF;
    }

    public static boolean isFontRendererUpdated() {
        return fontRendererUpdated;
    }

    public static void setFontRendererUpdated(boolean var0) {
        fontRendererUpdated = var0;
    }

    public static boolean isWeatherEnabled() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofWeather();
    }

    public static boolean isSkyEnabled() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofSky();
    }

    public static boolean isStarsEnabled() {
        return gameSettings == null || ((ExGameOptions) gameSettings).ofStars();
    }

    public static boolean isFarView() {
        return gameSettings != null && ((ExGameOptions) gameSettings).ofFarView();
    }

    public static void sleep(long var0) {
        try {
            Thread.currentThread();
            Thread.sleep(var0);
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }
    }

    public static boolean isTimeDayOnly() {
        return gameSettings != null && ((ExGameOptions) gameSettings).ofTime() == 1;
    }

    public static boolean isTimeNightOnly() {
        return gameSettings != null && ((ExGameOptions) gameSettings).ofTime() == 2;
    }

    public static boolean isClearWater() {
        return gameSettings != null && ((ExGameOptions) gameSettings).ofClearWater();
    }

    public static int getAnisotropicFilterLevel() {
        return gameSettings == null ? 1 : ((ExGameOptions) gameSettings).ofAfLevel();
    }

    public static int getAntialiasingLevel() {
        return gameSettings == null ? 0 : ((ExGameOptions) gameSettings).ofAaLevel();
    }
}
