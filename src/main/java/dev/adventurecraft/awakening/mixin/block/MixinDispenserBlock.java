package dev.adventurecraft.awakening.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.entity.AC_EntityArrowBomb;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.item.AC_Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.DispenserTile;

@Mixin(DispenserTile.class)
public abstract class MixinDispenserBlock {

    @Inject(
        method = "use",
        at = @At("HEAD"),
        cancellable = true
    )
    private void disableUsageInPlayMode(
        Level level,
        int x,
        int y,
        int z,
        Player player,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!((ExPlayerEntity) player).isDebugMode()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "dispenseFrom",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/ItemInstance;id:I",
            shift = At.Shift.BEFORE,
            ordinal = 0
        ),
        cancellable = true
    )
    private void dispenseAcItem(
        Level level,
        int x,
        int y,
        int z,
        Random random,
        CallbackInfo ci,
        @Local ItemInstance item,
        @Local(ordinal = 4) int dX,
        @Local(ordinal = 5) int dZ,
        @Local(ordinal = 0) double eX,
        @Local(ordinal = 1) double eY,
        @Local(ordinal = 2) double eZ
    ) {
        if (item.id == AC_Items.bombArow.id) {
            var arrow = new AC_EntityArrowBomb(level, eX, eY, eZ);
            arrow.shoot(dX, 0.1F, dZ, 1.1F, 6.0F);
            level.addEntity(arrow);
            level.levelEvent(1002, x, y, z, 0);
            ci.cancel();
        }
    }
}
