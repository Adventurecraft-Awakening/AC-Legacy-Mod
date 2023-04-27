package dev.adventurecraft.awakening.client.options;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private static int iconWidthTerrain = 16;
    private static int iconWidthItems = 16;
    private static Map<String, Class<?>> foundClassesMap = new HashMap<>();
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

    private static String getVersion() {
        return "OptiFine_1.7.3_HD_AA_G4";
    }

    public static void logOpenGlCaps() {
        Logger logger = ACMod.LOGGER;

        logger.info("");
        logger.info(getVersion());
        logger.info("" + new Date());
        logger.info("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
        logger.info("Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        logger.info("VM: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        logger.info("LWJGL: " + Sys.getVersion());
        logger.info("OpenGL: " + GL11.glGetString(GL11.GL_RENDERER) + " version " + GL11.glGetString(GL11.GL_VERSION) + ", " + GL11.glGetString(GL11.GL_VENDOR));
        int var0 = getOpenGlVersion();
        String var1 = "" + var0 / 10 + "." + var0 % 10;
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

    public static boolean isTreesFancy() {
        if (gameSettings == null)
            return false;
        if (((ExGameOptions) gameSettings).ofTrees() == 0)
            return gameSettings.fancyGraphics;
        return ((ExGameOptions) gameSettings).ofTrees() == 2;
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

    public static boolean callBoolean(String var0, String var1, Object... var2) {
        try {
            Class<?> var3 = getClass(var0);
            if (var3 == null) {
                return false;
            } else {
                Method var4 = getMethod(var3, var1, var2);
                if (var4 == null) {
                    return false;
                } else {
                    Boolean var5 = (Boolean) var4.invoke(null, var2);
                    return var5;
                }
            }
        } catch (Throwable var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public static void callVoid(String var0, String var1, Object... var2) {
        try {
            Class<?> var3 = getClass(var0);
            if (var3 == null) {
                return;
            }

            Method var4 = getMethod(var3, var1, var2);
            if (var4 == null) {
                return;
            }

            var4.invoke((Object) null, var2);
        } catch (Throwable var5) {
            var5.printStackTrace();
        }

    }

    public static void callVoid(Object var0, String var1, Object... var2) {
        try {
            if (var0 == null) {
                return;
            }

            Class<?> var3 = var0.getClass();
            if (var3 == null) {
                return;
            }

            Method var4 = getMethod(var3, var1, var2);
            if (var4 == null) {
                return;
            }

            var4.invoke(var0, var2);
        } catch (Throwable var5) {
            var5.printStackTrace();
        }

    }

    public static Object getFieldValue(String var0, String var1) {
        try {
            Class<?> var2 = getClass(var0);
            if (var2 == null) {
                return null;
            } else {
                Field var3 = var2.getDeclaredField(var1);
                if (var3 == null) {
                    return null;
                } else {
                    Object var4 = var3.get((Object) null);
                    return var4;
                }
            }
        } catch (Throwable var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public static Object getFieldValue(Object var0, String var1) {
        try {
            if (var0 == null) {
                return null;
            } else {
                Class<?> var2 = var0.getClass();
                if (var2 == null) {
                    return null;
                } else {
                    Field var3 = var2.getField(var1);
                    if (var3 == null) {
                        return null;
                    } else {
                        Object var4 = var3.get(var0);
                        return var4;
                    }
                }
            }
        } catch (Throwable var5) {
            var5.printStackTrace();
            return null;
        }
    }

    private static Method getMethod(Class<?> var0, String var1, Object... var2) {
        Method[] var3 = var0.getMethods();

        for (int var4 = 0; var4 < var3.length; ++var4) {
            Method var5 = var3[var4];
            if (var5.getName().equals(var1) && var5.getParameterTypes().length == var2.length) {
                return var5;
            }
        }

        ACMod.LOGGER.info("No method found for: " + var0.getName() + "." + var1 + "(" + arrayToString(var2) + ")");
        return null;
    }

    public static String arrayToString(Object[] var0) {
        StringBuffer var1 = new StringBuffer(var0.length * 5);

        for (int var2 = 0; var2 < var0.length; ++var2) {
            Object var3 = var0[var2];
            if (var2 > 0) {
                var1.append(", ");
            }

            var1.append(String.valueOf(var3));
        }

        return var1.toString();
    }

    public static boolean hasModLoader() {
        Class<?> var0 = getClass("ModLoader");
        return var0 != null;
    }

    private static Class<?> getClass(String var0) {
        Class<?> var1 = foundClassesMap.get(var0);
        if (var1 != null) {
            return var1;
        } else if (foundClassesMap.containsKey(var0)) {
            return null;
        } else {
            try {
                var1 = Class.forName(var0);
            } catch (ClassNotFoundException var3) {
                ACMod.LOGGER.info("Class not found: " + var0);
            } catch (Throwable var4) {
                var4.printStackTrace();
            }

            foundClassesMap.put(var0, var1);
            return var1;
        }
    }

    public static void setMinecraft(Minecraft var0) {
        minecraft = var0;
    }

    public static Minecraft getMinecraft() {
        return minecraft;
    }

    public static int getIconWidthTerrain() {
        return iconWidthTerrain;
    }

    public static int getIconWidthItems() {
        return iconWidthItems;
    }

    public static void setIconWidthItems(int var0) {
        iconWidthItems = var0;
    }

    public static void setIconWidthTerrain(int var0) {
        iconWidthTerrain = var0;
    }

    public static int getMaxDynamicTileWidth() {
        return 64;
    }

    public static BetterGrassOption getBetterGrassOption() {
        if (gameSettings != null) {
            return ((ExGameOptions) gameSettings).ofBetterGrass();
        }
        return BetterGrassOption.OFF;
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

    public static boolean between(int var0, int var1, int var2) {
        return var0 >= var1 && var0 <= var2;
    }

    public static boolean isTerrainIconClamped(int var0) {
        return !between(var0, 0, 2) && !between(var0, 4, 10) && !between(var0, 16, 21) && !between(var0, 32, 37) && !between(var0, 40, 40) && !between(var0, 48, 53) && !between(var0, 64, 67) && !between(var0, 69, 75) && !between(var0, 86, 87) && !between(var0, 102, 107) && !between(var0, 109, 110) && !between(var0, 113, 114) && !between(var0, 116, 121) && !between(var0, 129, 133) && !between(var0, 144, 147) && !between(var0, 160, 165) && !between(var0, 176, 181) && !between(var0, 192, 195) && !between(var0, 205, 207) && !between(var0, 208, 210) && !between(var0, 222, 223) && !between(var0, 225, 225) && !between(var0, 237, 239) && !between(var0, 240, 249) && !between(var0, 254, 255);
    }

    public static boolean isMultiTexture() {
        return true;
    }
}
