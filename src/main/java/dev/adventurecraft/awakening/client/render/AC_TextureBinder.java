package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.extension.client.ExTextureManager;

import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.layout.Size;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface AC_TextureBinder {

    Frame EMPTY_FRAME = new Frame(IntRect.zero, ImageBuffer.create(0, 0, ImageFormat.RGBA_U8));

    void animate();

    void onTick(Size size);

    void loadImage(String name, Level world);

    void loadImage(String name, ImageBuffer image);

    String getTexture();

    IntRect getCurrentFrameRect();

    @Nullable
    Frame getCurrentFrame();

    void setTileSize(int width, int height);

    static <T extends DynamicTexture> void loadImages(ExTextureManager texManager, Class<T> type, Level world) {
        AC_TextureBinder.loadImages(texManager, type, null, world);
    }

    static <T extends DynamicTexture> void loadImages(
        ExTextureManager texManager,
        Class<T> type,
        String name,
        Level world
    ) {
        texManager.getTextureBinders(type).forEach(b -> ((AC_TextureBinder) b).loadImage(name, world));
    }

    record Frame(IntRect targetRect, ImageBuffer image) {
    }
}
