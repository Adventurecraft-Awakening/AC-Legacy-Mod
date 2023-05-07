package dev.adventurecraft.awakening.mixin.client.render.entity.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.client.render.entity.block.PistonRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PistonRenderer.class)
public abstract class MixinPistonRenderer {

    @ModifyConstant(
        method = "render(Lnet/minecraft/entity/block/PistonBlockEntity;DDDF)V",
        constant = @Constant(stringValue = "/terrain.png"))
    private String useTerrainTexture(String constant, @Local Block block) {
        int texture = ((ExBlock) block).getTextureNum();
        String var11;
        if (texture == 0) {
            var11 = constant;
        } else {
            var11 = String.format("/terrain%d.png", texture);
        }
        return var11;
    }
}
