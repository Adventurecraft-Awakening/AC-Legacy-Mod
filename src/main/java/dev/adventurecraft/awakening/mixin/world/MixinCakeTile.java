package dev.adventurecraft.awakening.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.tile.CakeTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CakeTile.class)
public abstract class MixinCakeTile {

    @ModifyConstant(
        method = "eat",
        constant = @Constant(intValue = 20)
    )
    private int respectMaxHealthWhenEating(int constant, @Local(argsOnly = true) Player player) {
        return ((ExMob) player).getMaxHealth();
    }
}
