package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

public interface AC_TextureBinder {

    void setAtlasRect(int x, int y, int width, int height);

    void onTick(Vec2 size);

    void loadImage(String name, World world);

    void loadImage(String name, BufferedImage image);

    String getTexture();

    IntBuffer getBufferAtCurrentFrame();

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    static <T extends TextureBinder> void loadImages(ExTextureManager texManager, Class<T> type, World world) {
        AC_TextureBinder.loadImages(texManager, type, null, world);
    }

    static <T extends TextureBinder> void loadImages(ExTextureManager texManager, Class<T> type, String name, World world) {
        texManager.getTextureBinders(type).forEach(b -> ((AC_TextureBinder) b).loadImage(name, world));
    }
}
