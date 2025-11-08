package dev.adventurecraft.awakening.extension.client;

import dev.adventurecraft.awakening.common.AC_TextureAnimated;

import java.io.IOException;
import java.util.stream.Stream;

import dev.adventurecraft.awakening.image.ImageBuffer;
import net.minecraft.client.renderer.ptexture.DynamicTexture;

public interface ExTextureManager {

    <T extends DynamicTexture> Stream<T> getTextureBinders(Class<T> type);

    ImageBuffer getTextureImage(String name) throws IOException;

    void loadTexture(ImageBuffer image, int texId);

    void loadTexture(int id, String name);

    int loadTexture(ImageBuffer image);

    void releaseTexture(int id);

    void clearTextureAnimations();

    void registerTextureAnimation(String animationName, AC_TextureAnimated animation);

    void unregisterTextureAnimation(String animationName);

    void replaceTexture(String keyName, String replacementName);

    void revertTextures();
}
