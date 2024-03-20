package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import net.minecraft.client.render.TextureBinder;

public class AC_TextureAnimated extends TextureBinder {

    public String texName;

    public AC_TextureAnimated(String texName, int x, int y, int width, int height) {
        super(0);
        this.texName = texName;
        ((AC_TextureBinder) this).setAtlasRect(x, y, width, height);
    }
}
