package dev.adventurecraft.awakening.extension.client;

import dev.adventurecraft.awakening.common.AC_TextureAnimated;

import java.io.IOException;
import java.util.stream.Stream;

import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.texture.TextureAsset;
import dev.adventurecraft.awakening.client.render.texture.TextureCatalog;
import net.minecraft.client.renderer.ptexture.DynamicTexture;

public interface ExTextureManager {

    TextureCatalog textureCatalog();

    /** Borrowed stable asset references; callers never own catalog storage. */
    RenderTexture texture(String name);

    RenderTexture texture(TextureAsset asset);

    /** Material channels are linear RGBA data, even when a manifest uses arbitrary filenames. */
    default RenderTexture materialTexture(String name, dev.adventurecraft.awakening.client.render.AC_TextureBinder.MaterialChannel channel) {
        return texture(name);
    }

    /** True when the map or selected pack can resolve the path (without uploading it). */
    boolean hasTexture(String name);

    /** HTTP remains an inherited asset boundary; a missing image with no fallback returns null. */
    @org.jetbrains.annotations.Nullable
    RenderTexture skin(String url, String fallback);

    /** Caller owns the returned generated image. */
    RenderTexture createTexture(ImageBuffer image);

    <T extends DynamicTexture> Stream<T> getTextureBinders(Class<T> type);

    ImageBuffer getTextureImage(String name) throws IOException;

    void clearTextureAnimations();

    void registerTextureAnimation(String animationName, AC_TextureAnimated animation);

    void unregisterTextureAnimation(String animationName);

    void replaceTexture(String keyName, String replacementName);

    void revertTextures();
}
