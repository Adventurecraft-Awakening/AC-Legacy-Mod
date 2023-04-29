package dev.adventurecraft.awakening.mixin.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.GlassBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GlassBlock.class)
public abstract class MixinGlassBlock {

    @Environment(EnvType.CLIENT)
    @Overwrite
    public int getRenderPass() {
        return 1;
    }
}
