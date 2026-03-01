package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.image.*;
import dev.adventurecraft.awakening.layout.Size;
import dev.adventurecraft.awakening.util.MathF;

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

    private static int getOffset(int x, int z) {
        x += imageWidth >> 1;
        z += imageHeight >> 1;

        x = MathF.clamp(x, 0, imageWidth - 1);
        z = MathF.clamp(z, 0, imageHeight - 1);

        return x + z * imageWidth;
    }

    public static int getTerrainInfo(int x, int z) {
        return terrainInfo[getOffset(x, z)];
    }

    public static int getBiomeInfo(int x, int z) {
        return biomeInfo[getOffset(x, z)];
    }

    public static int getWaterColor(int x, int z) {
        if (waterInfo != null) {
            return waterInfo[getOffset(x, z)];
        }
        return 4221183;
    }

    public static int getTerrainHeight(int x, int z) {
        if (!isLoaded) {
            return 64;
        }
        int info = getTerrainInfo(x, z);
        return ((info >> 8) & 255) / 2;
    }

    public static int getWaterHeight(int x, int z) {
        if (!isLoaded) {
            return 0;
        }
        int info = getTerrainInfo(x, z);
        return (info & 255) / 2;
    }

    public static boolean hasSandNearWaterEdge(int x, int z) {
        if (!isLoaded) {
            return false;
        }
        int info = getTerrainInfo(x, z);
        return ((info >> 16) & 255) > 127;
    }

    public static float getTerrainHumidity(int x, int z) {
        if (!isLoaded) {
            return 0.25f;
        }
        return (getBiomeInfo(x, z) & 255) / 255f;
    }

    public static float getTerrainTemperature(int x, int z) {
        if (!isLoaded) {
            return 0.75f;
        }
        return ((getBiomeInfo(x, z) >> 16) & 255) / 255f;
    }

    private static ImageBuffer loadMapImage(File file) {
        ImageBuffer image = null;
        try {
            image = ImageLoader.load(file, ImageLoadOptions.withFormat(ImageFormat.RGBA_U8));
        }
        catch (FileNotFoundException ex) {
            ACMod.LOGGER.warn("Missing map texture \"{}\".", file.getPath());
        }
        catch (IOException ex) {
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
            file.getPath(),
            size,
            terrainSize
        );
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
