package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;
import dev.adventurecraft.awakening.client.render.compat.LegacyGlConstants;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.image.ImageLoadOptions;
import dev.adventurecraft.awakening.image.ImageLoader;
import dev.adventurecraft.awakening.util.FutureUtil;

import net.minecraft.client.renderer.Textures;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AC_MapInfo {

    public String name;
    public String description1;
    public String description2;
    private File thumbnailFile;
    private Future<ImageBuffer> imageFuture;
    private RenderTexture thumbnailTexture;
    private boolean textureFailed;

    public AC_MapInfo(String name, String description1, String description2, File thumbnailFile) {
        this.name = name;
        this.description1 = description1;
        this.description2 = description2;
        if (this.description1 == null) {
            this.description1 = "";
        }

        if (this.description2 == null) {
            this.description2 = "";
        }

        this.thumbnailFile = thumbnailFile;
    }

    public boolean bindTexture(Textures textureManager, ExecutorService executor) {
        RenderTexture texture = this.texture(textureManager, executor);
        if (texture == null) return false;
        LegacyGlAdapter.bindTexture(LegacyGlConstants.TEXTURE_2D, LegacyGlAdapter.idForTexture(texture));
        return true;
    }

    public RenderTexture texture(Textures textureManager, ExecutorService executor) {
        if (this.imageFuture == null) {
            queueTextureLoad(executor);
        }

        if (this.thumbnailTexture == null && !this.textureFailed) {
            if (!this.imageFuture.isDone()) {
                return null;
            }

            ImageBuffer image = FutureUtil.getOrElse(this.imageFuture, null);
            if (image != null) {
                this.thumbnailTexture = ((ExTextureManager) textureManager).createTexture(image);
            } else {
                this.textureFailed = true;
            }
        }

        return this.thumbnailTexture == null
            ? ((ExTextureManager) textureManager).texture("/gui/unknown_pack.png") : this.thumbnailTexture.borrow();
    }

    public void releaseTexture(Textures textureManager) {
        if (this.thumbnailTexture != null) { this.thumbnailTexture.close(); this.thumbnailTexture = null; }
        this.textureFailed = false;
    }

    private void queueTextureLoad(ExecutorService executor) {
        this.imageFuture = executor.submit(() -> {
            try {
                if (thumbnailFile.exists()) {
                    return ImageLoader.load(
                        thumbnailFile, new ImageLoadOptions().withDesiredFormat(ImageFormat.RGBA_U8));
                }
            } catch (IOException ex) {
                ACMod.LOGGER.warn("Failed to read map thumbnail \"{}\".", thumbnailFile.getPath(), ex);
            }
            return null;
        });
    }
}
