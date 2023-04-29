package dev.adventurecraft.awakening.mixin.client.entity.particle;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.client.entity.particle.DiggingParticleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DiggingParticleEntity.class)
public abstract class MixinDiggingParticleEntity {

    @Shadow
    private Block block;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 0))
    private int useBlockMeta(int constant, @Local(ordinal = 0, argsOnly = true) int meta) {
        return meta;
    }

    @Overwrite
    public int method_2003() {
        int var1 = ((ExBlock) this.block).getTextureNum();
        return var1 == 2 ? 3 : (var1 == 3 ? 4 : 1);
    }
}
