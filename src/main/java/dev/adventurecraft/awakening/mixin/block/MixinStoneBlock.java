package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.common.AC_IBlockColor;
import net.minecraft.block.StoneBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StoneBlock.class)
public abstract class MixinStoneBlock implements AC_IBlockColor {
}
