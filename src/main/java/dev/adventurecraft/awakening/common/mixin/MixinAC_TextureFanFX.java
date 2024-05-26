package dev.adventurecraft.awakening.common.mixin;

import dev.adventurecraft.awakening.common.AC_TextureFanFX;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.mixin.client.render.MixinTextureBinder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.awt.image.BufferedImage;

@Mixin(AC_TextureFanFX.class)
public abstract class MixinAC_TextureFanFX extends MixinTextureBinder {

    @Override
    public void loadImage(String name, World world) {
        if (name == null) {
            name = "/misc/fan.png";
        }

        BufferedImage image = null;
        if (world instanceof ExWorld exWorld) {
            image = exWorld.loadMapTexture(name);
        }

        try {
            if (image == null) {
                image = ((ExTextureManager) Minecraft.instance.textureManager).getTextureImage(name);
            }
        } catch (Exception ex) {
            Minecraft.instance.overlay.addChatMessage(String.format("Unable to load texture '%s': %s", name, ex));
        }

        super.loadImage(name, image);
    }

    @Override
    public void onTick(Vec2 size) {
    }
}
