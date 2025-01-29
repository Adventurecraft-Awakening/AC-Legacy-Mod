package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import net.minecraft.client.renderer.ptexture.DynamicTexture;

public class AC_TextureAnimated extends DynamicTexture {

    public String texName;
    public int x;
    public int y;

    public AC_TextureAnimated(String texName, int x, int y, int width, int height) {
        super(0);
        this.texName = texName;
        this.x = x;
        this.y = y;
        ((AC_TextureBinder) this).setWidth(width);
        ((AC_TextureBinder) this).setHeight(height);
    }
}
