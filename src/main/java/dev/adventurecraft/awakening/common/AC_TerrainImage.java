package dev.adventurecraft.awakening.common;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class AC_TerrainImage {
    private static int[] biomeInfo;
    private static int[] terrainInfo;
    private static int[] waterInfo;
    private static int imageHeight;
    private static int imageWidth;
    private static int halfHeight;
    private static int halfWidth;
    public static boolean isLoaded;
    public static boolean isWaterLoaded;

    private static int getOffset(int var0, int var1) {
        var0 += halfWidth;
        var1 += halfHeight;
        if (var0 < 0) {
            var0 = 0;
        } else if (var0 >= imageWidth) {
            var0 = imageWidth - 1;
        }

        if (var1 < 0) {
            var1 = 0;
        } else if (var1 >= imageHeight) {
            var1 = imageHeight - 1;
        }

        return var0 + var1 * imageWidth;
    }

    public static int getTerrainInfo(int var0, int var1) {
        return terrainInfo[getOffset(var0, var1)];
    }

    public static int getBiomeInfo(int var0, int var1) {
        return biomeInfo[getOffset(var0, var1)];
    }

    public static int getWaterColor(int var0, int var1) {
        return isWaterLoaded ? waterInfo[getOffset(var0, var1)] : 4221183;
    }

    public static int getTerrainHeight(int var0, int var1) {
        if (!isLoaded) {
            return 64;
        } else {
            int var2 = getTerrainInfo(var0, var1);
            return (var2 >> 8 & 255) / 2;
        }
    }

    public static int getWaterHeight(int var0, int var1) {
        if (!isLoaded) {
            return 0;
        } else {
            int var2 = getTerrainInfo(var0, var1);
            return (var2 & 255) / 2;
        }
    }

    public static boolean hasSandNearWaterEdge(int var0, int var1) {
        if (!isLoaded) {
            return false;
        } else {
            int var2 = getTerrainInfo(var0, var1);
            return (var2 >> 16 & 255) > 127;
        }
    }

    public static double getTerrainHumidity(int var0, int var1) {
        return !isLoaded ? 0.25D : (double) (getBiomeInfo(var0, var1) & 255) / 255.0D;
    }

    public static double getTerrainTemperature(int var0, int var1) {
        return !isLoaded ? 0.75D : (double) (getBiomeInfo(var0, var1) >> 16 & 255) / 255.0D;
    }

    public static boolean loadBiomeMap(File var0) {
        try {
            BufferedImage var1 = ImageIO.read(var0);

            assert imageWidth == var1.getWidth() : "biomeMap.png width doesn't match the width of terrainMap.png";

            assert imageHeight == var1.getHeight() : "biomeMap.png height doesn't match the height of terrainMap.png";

            var1.getRGB(0, 0, imageWidth, imageHeight, biomeInfo, 0, imageWidth);
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean loadWaterMap(File var0) {
        try {
            BufferedImage var1 = ImageIO.read(var0);

            assert imageWidth == var1.getWidth() : "waterMap.png width doesn't match the width of terrainMap.png";

            assert imageHeight == var1.getHeight() : "waterMap.png height doesn't match the height of terrainMap.png";

            waterInfo = new int[imageWidth * imageHeight];
            var1.getRGB(0, 0, imageWidth, imageHeight, waterInfo, 0, imageWidth);
            isWaterLoaded = true;
            return true;
        } catch (Exception var2) {
            isWaterLoaded = false;
            return false;
        }
    }

    public static boolean loadMap(File var0) {
        biomeInfo = new int[0];
        terrainInfo = new int[0];
        waterInfo = new int[0];
        imageWidth = 0;
        imageHeight = 0;
        halfWidth = 0;
        halfHeight = 0;
        isWaterLoaded = false;

        try {
            File var1 = new File(var0, "terrainMap.png");
            System.out.printf("Exists: %b Path: %s\n", var1.exists(), var1.getCanonicalPath());
            BufferedImage var2 = ImageIO.read(var1);
            imageWidth = var2.getWidth();
            imageHeight = var2.getHeight();
            halfWidth = imageWidth / 2;
            halfHeight = imageHeight / 2;
            terrainInfo = new int[imageWidth * imageHeight];
            biomeInfo = new int[imageWidth * imageHeight];
            var2.getRGB(0, 0, imageWidth, imageHeight, terrainInfo, 0, imageWidth);
            File var3 = new File(var0, "biomeMap.png");
            if (!loadBiomeMap(var3)) {
                isLoaded = false;
                return false;
            } else {
                File var4 = new File(var0, "waterMap.png");
                loadWaterMap(var4);
                isLoaded = true;
                return true;
            }
        } catch (Exception var5) {
            isLoaded = false;
            return false;
        }
    }
}
