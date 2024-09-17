package dev.adventurecraft.awakening.common;

import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import net.minecraft.client.renderer.Textures;

public class AC_MapInfo {

    public String name;
    public String description1;
    public String description2;
    private BufferedImage mapThumbnail;
    private int textureID = -1;

    public AC_MapInfo(String name, String description1, String description2, BufferedImage thumbnail) {
        this.name = name;
        this.description1 = description1;
        this.description2 = description2;
        if (this.description1 == null) {
            this.description1 = "";
        }

        if (this.description2 == null) {
            this.description2 = "";
        }

        this.mapThumbnail = thumbnail;
    }

    public void bindTexture(Textures textureManager) {
        if (this.mapThumbnail != null && this.textureID < 0) {
            this.textureID = textureManager.getTexture(this.mapThumbnail);
        }

        if (this.mapThumbnail != null) {
            textureManager.bind(this.textureID);
        } else {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureManager.loadTexture("/gui/unknown_pack.png"));
        }
    }
}
