package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.Vec2;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureBinder.class)
public abstract class MixinTextureBinder implements AC_TextureBinder {

    @Shadow
    public byte[] grid;

    @Shadow
    public boolean render3d;

    @Shadow
    public int renderMode;

    @Shadow
    public void updateTexture() {
        throw new AssertionError();
    }

    @Overwrite
    public void bindTexture(TextureManager var1) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, var1.getTextureId(this.getTexture()));
    }

    @Override
    public void onTick(Vec2 size) {
        this.updateTexture();
    }

    @Override
    public String getTexture() {
        if (this.renderMode == 0) return "/terrain.png";
        //if (this.renderMode == 1) return "/gui/items.png";
        return "/gui/items.png";
    }
}
