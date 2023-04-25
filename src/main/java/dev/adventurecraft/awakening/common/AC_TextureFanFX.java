package dev.adventurecraft.awakening.common;

import java.awt.image.BufferedImage;
import java.io.IOException;

import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextureBinder;

public class AC_TextureFanFX extends TextureBinder {
    static int numFrames;
    int curFrame = 0;
    private static int[] fanImage;
    private static int width;
    private static int height;

    public AC_TextureFanFX() {
        super(AC_Blocks.fan.texture);
        loadImage();
    }

    public static void loadImage() {
        try {
            BufferedImage var0 = null;
            if (Minecraft.instance.world != null) {
                var0 = ((ExWorld) Minecraft.instance.world).loadMapTexture("/misc/fan.png");
            }

            if (var0 == null) {
                var0 = ((ExTextureManager) Minecraft.instance.textureManager).getTextureImage("/misc/fan.png");
            }

            width = var0.getWidth();
            height = var0.getHeight();
            numFrames = var0.getWidth() / var0.getHeight();
            fanImage = new int[var0.getWidth() * var0.getHeight()];
            var0.getRGB(0, 0, var0.getWidth(), var0.getHeight(), fanImage, 0, var0.getWidth());
        } catch (IOException var1) {
            var1.printStackTrace();
            fanImage = new int[256];
            numFrames = 1;
            width = 16;
            height = 16;
        }

    }

    public void onTick(Vec2 var1) {
        int var2 = this.curFrame * height;
        int var3 = 0;

        for (int var4 = 0; var4 < height; ++var4) {
            for (int var5 = 0; var5 < height; ++var5) {
                int var6 = fanImage[var4 + var5 * width + var2];
                this.grid[var3 * 4 + 0] = (byte) (var6 >> 16 & 255);
                this.grid[var3 * 4 + 1] = (byte) (var6 >> 8 & 255);
                this.grid[var3 * 4 + 2] = (byte) (var6 & 255);
                this.grid[var3 * 4 + 3] = (byte) (var6 >> 24 & 255);
                ++var3;
            }
        }

        this.curFrame = (this.curFrame + 1) % numFrames;
    }
}
