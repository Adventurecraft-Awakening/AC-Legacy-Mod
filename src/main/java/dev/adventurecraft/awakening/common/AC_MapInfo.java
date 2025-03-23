package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageLoader;
import dev.adventurecraft.awakening.util.FutureUtil;
import org.lwjgl.opengl.GL11;

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
    private int textureID = -1;

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
        if (this.imageFuture == null) {
            queueTextureLoad(executor);
        }

        if (this.textureID == -1) {
            if (!this.imageFuture.isDone()) {
                return false;
            }

            ImageBuffer image = FutureUtil.getOrElse(this.imageFuture, null);
            if (image != null) {
                this.textureID = ((ExTextureManager) textureManager).getTexture(image);
            } else {
                this.textureID = -2;
            }
        }

        int id = this.textureID;
        if (id < 0) {
            id = textureManager.loadTexture("/gui/unknown_pack.png");
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        return true;
    }

    public void releaseTexture(Textures textureManager) {
        if (this.textureID >= 0) {
            textureManager.releaseTexture(this.textureID);
            this.textureID = -1;
        }
    }

    private void queueTextureLoad(ExecutorService executor) {
        this.imageFuture = executor.submit(() -> {
            try {
                if (thumbnailFile.exists()) {
                    return ImageLoader.load(thumbnailFile, 4);
                }
            } catch (IOException ex) {
                ACMod.LOGGER.warn("Failed to read map thumbnail \"{}\".", thumbnailFile.getPath(), ex);
            }
            return null;
        });
    }
}
