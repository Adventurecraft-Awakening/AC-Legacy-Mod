package dev.adventurecraft.awakening.common;

import java.awt.image.BufferedImage;
import java.io.IOException;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.world.World;

public class AC_TextureFanFX extends TextureBinder implements AC_TextureBinder {

    int numFrames;
    int curFrame = 0;
    int[] fanImage;
    int width;
    int height;

    public AC_TextureFanFX() {
        super(AC_Blocks.fan.texture);
    }

    @Override
    public String getTexture() {
        // TODO: fix texture management (see MixinTextureManager)
        if (this.renderMode == 0) return "/terrain.png";
        //if (this.renderMode == 1) return "/gui/items.png";
        return "/gui/items.png";
    }

    @Override
    public void loadImage(World world) {
        this.loadImage("/misc/fan.png", world);
    }

    @Override
    public void loadImage(String name, World world) {
        try {
            BufferedImage var0 = null;
            if (world != null) {
                var0 = ((ExWorld) world).loadMapTexture(name);
            }

            if (var0 == null) {
                var0 = ((ExTextureManager) Minecraft.instance.textureManager).getTextureImage(name);
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

    @Override
    public void onTick(Vec2 var1) {
        if (this.fanImage == null) {
            return;
        }

        int var2 = this.curFrame * height;
        int var3 = 0;

        for (int var4 = 0; var4 < height; ++var4) {
            for (int var5 = 0; var5 < height; ++var5) {
                int var6 = this.fanImage[var4 + var5 * width + var2];
                this.grid[var3 * 4 + 0] = (byte) (var6 >> 16 & 255);
                this.grid[var3 * 4 + 1] = (byte) (var6 >> 8 & 255);
                this.grid[var3 * 4 + 2] = (byte) (var6 & 255);
                this.grid[var3 * 4 + 3] = (byte) (var6 >> 24 & 255);
                ++var3;
            }
        }

        this.curFrame = (this.curFrame + 1) % this.numFrames;
    }
}
