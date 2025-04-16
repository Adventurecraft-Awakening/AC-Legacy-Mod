package dev.adventurecraft.awakening.common.mixin;

import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.mixin.client.render.MixinTextureBinder;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AC_TextureAnimated.class)
public abstract class MixinAC_TextureAnimated extends MixinTextureBinder {

    @Override
    public String getTexture() {
        return ((AC_TextureAnimated) (Object) this).texName;
    }

    @Override
    public void loadImage(String name, ImageBuffer image) {
        this.hasImages = false;
        this.curFrame = 0;

        if (image == null) {
            Minecraft.instance.gui.addMessage(String.format("Unable to load texture '%s'", name));
        } else if (this.width != image.getWidth()) {
            Minecraft.instance.gui.addMessage(String.format(
                "Animated texture width of %d didn't match the specified width of %d", image.getWidth(), this.width));
        } else if (0 != image.getHeight() % this.height) {
            Minecraft.instance.gui.addMessage(String.format("Animated texture height of %d isn't a multiple of the specified height of %d", image.getHeight(), this.height));
        } else {
            this.numFrames = image.getHeight() / this.height;
            this.imageData = this.allocImageData(image.getWidth(), image.getHeight());
            image.copyTo(this.imageData, ImageFormat.RGBA_U8);
            this.hasImages = true;
        }
    }
}
