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

    private static boolean fontRendererUpdated = false;
    public static final boolean DEF_FOG_FANCY = true;
    public static final float DEF_FOG_START = 0.2F;
    public static final boolean DEF_OPTIMIZE_RENDER_DISTANCE = false;
    public static final boolean DEF_OCCLUSION_ENABLED = false;
    public static final int DEF_MIPMAP_LEVEL = 0;
    public static final int DEF_MIPMAP_TYPE = GL11.GL_NEAREST_MIPMAP_NEAREST;
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

    private static int getOpenGlVersion() {
        var caps = GLContext.getCapabilities();
        if (caps.OpenGL11) return 11;
        if (caps.OpenGL12) return 12;
        if (caps.OpenGL13) return 13;
        if (caps.OpenGL14) return 14;
        if (caps.OpenGL15) return 15;
        if (caps.OpenGL20) return 20;
        if (caps.OpenGL21) return 21;
        if (caps.OpenGL30) return 30;
        if (caps.OpenGL31) return 31;
        if (caps.OpenGL32) return 32;
        if (caps.OpenGL33) return 33;
        if (caps.OpenGL40) return 40;
        if (caps.OpenGL41) return 41;
        if (caps.OpenGL42) return 42;
        if (caps.OpenGL43) return 43;
        if (caps.OpenGL44) return 44;
        if (caps.OpenGL45) return 45;
        return 10;
    }

    private static GameOptions getOptions() {
        var instance = Minecraft.instance;
        if (instance == null) {
            return null;
        }
        return instance.options;
    }

    public static boolean isUseAlphaFunc() {
        float var0 = getAlphaFuncLevel();
        return var0 > DEF_ALPHA_FUNC_LEVEL + 1.0E-5F;
    }

    public static float getAlphaFuncLevel() {
        return DEF_ALPHA_FUNC_LEVEL;
    }

    public static int limit(int var0, int var1, int var2) {
        return var0 < var1 ? var1 : Math.min(var0, var2);
    }

    public static float limit(float var0, float var1, float var2) {
        return var0 < var1 ? var1 : Math.min(var0, var2);
    }

    public static boolean isAnimatedWater() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedWater() != 2;
    }

    public static boolean isGeneratedWater() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedWater() == 1;
    }

    public static boolean isAnimatedPortal() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedPortal();
    }

    public static boolean isAnimatedLava() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedLava() != 2;
    }

    public static boolean isGeneratedLava() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedLava() == 1;
    }

    public static boolean isAnimatedFire() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedFire();
    }

    public static boolean isAnimatedRedstone() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedRedstone();
    }

    public static boolean isAnimatedExplosion() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedExplosion();
    }

    public static boolean isAnimatedFlame() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedFlame();
    }

    public static boolean isAnimatedSmoke() {
        var gameSettings = getOptions();
        return gameSettings == null || ((ExGameOptions) gameSettings).ofAnimatedSmoke();
    }

    public static boolean isFontRendererUpdated() {
        return fontRendererUpdated;
    }

    public static void setFontRendererUpdated(boolean var0) {
        fontRendererUpdated = var0;
    }
}
