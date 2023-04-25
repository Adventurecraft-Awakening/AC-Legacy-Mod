package dev.adventurecraft.awakening.mixin.client.render;

import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureBinder.class)
public abstract class MixinTextureBinder {

    @Shadow
    public int renderMode;

    @Overwrite
    public void bindTexture(TextureManager var1) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, var1.getTextureId(this.getTexture()));
    }

    public String getTexture() {
        if (this.renderMode == 0) return "/terrain.png";
        //if (this.renderMode == 1) return "/gui/items.png";
        return "/gui/items.png";
    }
}
