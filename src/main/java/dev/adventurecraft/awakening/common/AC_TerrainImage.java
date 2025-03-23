package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.image.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.IntBuffer;

public class AC_TerrainImage {
    private static int[] biomeInfo;
    private static int[] terrainInfo;
    private static int[] waterInfo;
    private static int imageHeight;
    private static int imageWidth;
    public static boolean isLoaded;

    public static boolean isWaterLoaded() {
        return waterInfo != null;
    }

    private static int getOffset(int x, int y) {
        x += imageWidth >> 1;
        y += imageHeight >> 1;

        if (x < 0) {
            x = 0;
        } else if (x >= imageWidth) {
            x = imageWidth - 1;
        }

        if (y < 0) {
            y = 0;
        } else if (y >= imageHeight) {
            y = imageHeight - 1;
        }

        return x + y * imageWidth;
    }

    public static int getTerrainInfo(int var0, int var1) {
        return terrainInfo[getOffset(var0, var1)];
    }

    public static int getBiomeInfo(int var0, int var1) {
        return biomeInfo[getOffset(var0, var1)];
    }

    public static int getWaterColor(int var0, int var1) {
        return waterInfo != null ? waterInfo[getOffset(var0, var1)] : 4221183;
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

    private static ImageBuffer loadMapImage(File file) {
        ImageBuffer image = null;
        try {
            image = ImageLoader.load(file, ImageLoadOptions.withFormat(ImageFormat.RGBA_U8));
        } catch (FileNotFoundException ex) {
            ACMod.LOGGER.warn("Missing map texture \"{}\".", file.getPath());
        } catch (IOException ex) {
            ACMod.LOGGER.error("Failed to load map texture \"{}\".", file.getPath(), ex);
        }
        return image;
    }

    private static void checkMapSize(Size size, File file) {
        var terrainSize = new Size(imageWidth, imageHeight);
        if (terrainSize.equals(size)) {
            return;
        }

        ACMod.LOGGER.warn(
            "\"{}\" size does not match the size of \"terrainMap.png\". {} vs {}",
            file.getPath(), size, terrainSize);
    }

    public static boolean loadBiomeMap(File file) {
        ImageBuffer image = loadMapImage(file);
        if (image == null) {
            return false;
        }
        checkMapSize(image.getSize(), file);

        image.copyTo(IntBuffer.wrap(biomeInfo), ImageFormat.BGRA_U8);
        return true;
    }

    public static boolean loadWaterMap(File file) {
        ImageBuffer image = loadMapImage(file);
        if (image == null) {
            waterInfo = null;
            return false;
        }
        checkMapSize(image.getSize(), file);

        waterInfo = new int[imageWidth * imageHeight];
        image.copyTo(IntBuffer.wrap(waterInfo), ImageFormat.BGRA_U8);
        return true;
    }

    public static boolean loadMap(File levelDir) {
        biomeInfo = null;
        terrainInfo = null;
        waterInfo = null;
        imageWidth = 0;
        imageHeight = 0;
        isLoaded = false;

        var terrainFile = new File(levelDir, "terrainMap.png");
        if (!terrainFile.exists()) {
            return false;
        }

        ImageBuffer terrainImage = loadMapImage(terrainFile);
        if (terrainImage == null) {
            return false;
        }

        imageWidth = terrainImage.getWidth();
        imageHeight = terrainImage.getHeight();
        terrainInfo = new int[imageWidth * imageHeight];
        biomeInfo = new int[imageWidth * imageHeight];

        terrainImage.copyTo(IntBuffer.wrap(terrainInfo), ImageFormat.BGRA_U8);

        var biomeFile = new File(levelDir, "biomeMap.png");
        if (loadBiomeMap(biomeFile)) {
            var waterFile = new File(levelDir, "waterMap.png");
            if (waterFile.exists()) {
                loadWaterMap(waterFile);
            }
            isLoaded = true;
            return true;
        }
        return false;
    }
}
