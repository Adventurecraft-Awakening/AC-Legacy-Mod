package dev.adventurecraft.awakening.client.options;

import dev.adventurecraft.awakening.ACMainThread;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
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

        logger.info("OS: {} ({}) version {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
        logger.info("Java: {}, {}", System.getProperty("java.version"), System.getProperty("java.vendor"));
        logger.info("VM: {} ({}), {}", System.getProperty("java.vm.name"), System.getProperty("java.vm.info"), System.getProperty("java.vm.vendor"));
        logger.info("LWJGL: {}", Sys.getVersion());
        logger.info("OpenGL GL_RENDERER: {}", GL11.glGetString(GL11.GL_RENDERER));
        logger.info("OpenGL GL_VERSION: {}", GL11.glGetString(GL11.GL_VERSION));
        logger.info("OpenGL GL_VENDOR: {}", GL11.glGetString(GL11.GL_VENDOR));

        var caps = GLContext.getCapabilities();
        int glVersion = getOpenGlVersion(caps);
        logger.info("OpenGL Version: {}.{}", glVersion / 10, glVersion % 10);
        logger.info("OpenGL Mipmap levels (GL12.GL_TEXTURE_MAX_LEVEL): {}", caps.OpenGL12);
        logger.info("OpenGL Fancy fog (GL_NV_fog_distance): {}", caps.GL_NV_fog_distance);
        logger.info("OpenGL Occlussion culling (GL_ARB_occlusion_query): {}", caps.GL_ARB_occlusion_query);

        if (ACMainThread.glDebug) {
            GL11.glEnable(37600);
            GL11.glEnable(33346);
            ARBDebugOutput.glDebugMessageCallbackARB(Config::debugMessageCallback, 0);
        }
    }

    private static void debugMessageCallback(
        int source, int type, int id, int severity, int length, long message, long userParam) {
        Logger glLog = ACMod.GL_LOGGER;
        String sSource = getSourceName(source);
        String sType = getTypeName(type);
        String sMessage = MemoryUtil.memUTF8(message, length);
        Exception ex = ACMainThread.glDebugTrace ? new Exception() : null;

        String format = "{}, {}, {}: {}";
        switch (severity) {
            case ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB -> glLog.error(format, sSource, sType, id, sMessage, ex);
            case ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB -> glLog.warn(format, sSource, sType, id, sMessage, ex);
            default -> glLog.info(format, sSource, sType, id, sMessage, ex);
        }
    }

    private static String getSourceName(int value) {
        return switch (value) {
            case ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB -> "API";
            case ARBDebugOutput.GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB -> "WindowSystem";
            case ARBDebugOutput.GL_DEBUG_SOURCE_SHADER_COMPILER_ARB -> "ShaderCompiler";
            case ARBDebugOutput.GL_DEBUG_SOURCE_THIRD_PARTY_ARB -> "ThirdParty";
            case ARBDebugOutput.GL_DEBUG_SOURCE_APPLICATION_ARB -> "Application";
            case ARBDebugOutput.GL_DEBUG_SOURCE_OTHER_ARB -> "Other";
            default -> Integer.toString(value);
        };
    }

    private static String getTypeName(int value) {
        return switch (value) {
            case ARBDebugOutput.GL_DEBUG_TYPE_ERROR_ARB -> "Error";
            case ARBDebugOutput.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB -> "DeprecatedBehavior";
            case ARBDebugOutput.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB -> "UndefinedBehavior";
            case ARBDebugOutput.GL_DEBUG_TYPE_PORTABILITY_ARB -> "Portability";
            case ARBDebugOutput.GL_DEBUG_TYPE_PERFORMANCE_ARB -> "Performance";
            case ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB -> "Other";
            default -> Integer.toString(value);
        };
    }

    private static int getOpenGlVersion(ContextCapabilities caps) {
        if (caps.OpenGL45) return 45;
        if (caps.OpenGL44) return 44;
        if (caps.OpenGL43) return 43;
        if (caps.OpenGL42) return 42;
        if (caps.OpenGL41) return 41;
        if (caps.OpenGL40) return 40;
        if (caps.OpenGL33) return 33;
        if (caps.OpenGL32) return 32;
        if (caps.OpenGL31) return 31;
        if (caps.OpenGL30) return 30;
        if (caps.OpenGL21) return 21;
        if (caps.OpenGL20) return 20;
        if (caps.OpenGL15) return 15;
        if (caps.OpenGL14) return 14;
        if (caps.OpenGL13) return 13;
        if (caps.OpenGL12) return 12;
        if (caps.OpenGL11) return 11;
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
