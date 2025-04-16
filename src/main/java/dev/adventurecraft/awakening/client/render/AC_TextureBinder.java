package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;

import java.nio.IntBuffer;

import dev.adventurecraft.awakening.image.ImageBuffer;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.world.level.Level;

public interface AC_TextureBinder {

    void animate();

    void onTick(Vec2 size);

    void loadImage(String name, Level world);

    void loadImage(String name, ImageBuffer image);

    String getTexture();

    IntBuffer getBufferAtCurrentFrame();

    int getWidth();

    void setWidth(int width);

    int getHeight();

    void setHeight(int height);

    static <T extends DynamicTexture> void loadImages(ExTextureManager texManager, Class<T> type, Level world) {
        AC_TextureBinder.loadImages(texManager, type, null, world);
    }

    static <T extends DynamicTexture> void loadImages(ExTextureManager texManager, Class<T> type, String name, Level world) {
        texManager.getTextureBinders(type).forEach(b -> ((AC_TextureBinder) b).loadImage(name, world));
    }
}
