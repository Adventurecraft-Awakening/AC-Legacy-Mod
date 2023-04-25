package dev.adventurecraft.awakening.common;

import java.awt.image.BufferedImage;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class AC_MapInfo {
    public String name;
    public String description1;
    public String description2;
    private BufferedImage mapThumbnail;
    private int textureID = -1;

    public AC_MapInfo(String var1, String var2, String var3, BufferedImage var4) {
        this.name = var1;
        this.description1 = var2;
        this.description2 = var3;
        if (this.description1 == null) {
            this.description1 = "";
        }

        if (this.description2 == null) {
            this.description2 = "";
        }

        this.mapThumbnail = var4;
    }

    public void bindTexture(Minecraft var1) {
        if (this.mapThumbnail != null && this.textureID < 0) {
            this.textureID = var1.textureManager.getTextureId(this.mapThumbnail);
        }

        if (this.mapThumbnail != null) {
            var1.textureManager.bindTexture(this.textureID);
        } else {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, var1.textureManager.getTextureId("/gui/unknown_pack.png"));
        }
    }
}
