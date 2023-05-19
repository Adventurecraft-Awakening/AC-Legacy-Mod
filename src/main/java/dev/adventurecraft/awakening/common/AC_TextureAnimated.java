package dev.adventurecraft.awakening.common;

import java.awt.image.BufferedImage;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;

public class AC_TextureAnimated {
    public byte[] imageData;
    public int x;
    public int y;
    public int width;
    public int height;
    public String texName;
    public int curFrame;
    public int numFrames;
    public int[] frameImages;
    public boolean hasImages;

    public AC_TextureAnimated(String texName, String imageName, int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.imageData = new byte[this.width * this.height * 4];
        this.texName = texName;
        this.loadImage(imageName);
    }

    public void loadImage(String name) {
        this.hasImages = false;
        BufferedImage var2 = null;
        if (Minecraft.instance.world != null) {
            var2 = ((ExWorld) Minecraft.instance.world).loadMapTexture(name);
        }

        this.curFrame = 0;
        if (var2 == null) {
            Minecraft.instance.overlay.addChatMessage(String.format("Unable to load texture '%s'", name));
        } else if (this.width != var2.getWidth()) {
            Minecraft.instance.overlay.addChatMessage(String.format("Animated texture width of %d didn't match the specified width of %d", var2.getWidth(), this.width));
        } else if (0 != var2.getHeight() % this.height) {
            Minecraft.instance.overlay.addChatMessage(String.format("Animated texture height of %d isn't a multiple of the specified height of %d", var2.getHeight(), this.height));
        } else {
            this.numFrames = var2.getHeight() / this.height;
            this.frameImages = new int[var2.getWidth() * var2.getHeight()];
            var2.getRGB(0, 0, var2.getWidth(), var2.getHeight(), this.frameImages, 0, var2.getWidth());
            this.hasImages = true;
        }
    }

    public void onTick() {
        if (!this.hasImages) {
            return;
        }

        int var1 = this.curFrame * this.width * this.height;
        int var2 = 0;

        for (int var3 = 0; var3 < this.height; ++var3) {
            for (int var4 = 0; var4 < this.width; ++var4) {
                int var5 = this.frameImages[var4 + var3 * this.width + var1];
                this.imageData[var2 + 0] = (byte) (var5 >> 16 & 255);
                this.imageData[var2 + 1] = (byte) (var5 >> 8 & 255);
                this.imageData[var2 + 2] = (byte) (var5 & 255);
                this.imageData[var2 + 3] = (byte) (var5 >> 24 & 255);
                var2 += 4;
            }
        }

        this.curFrame = (this.curFrame + 1) % this.numFrames;
    }

    public String getTexture() {
        return this.texName;
    }
}
