package dev.adventurecraft.awakening.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.entity.AC_EntityArrowBomb;
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
public abstract class MixinDispenserBlock extends MixinBlock {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void disableUsageInPlayMode(Level i, int j, int k, int arg2, Player par5, CallbackInfoReturnable<Boolean> cir) {
        if (!AC_DebugMode.active) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "dispenseFrom",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/ItemInstance;id:I",
            shift = At.Shift.BEFORE,
            ordinal = 0),
        cancellable = true)
    private void dispenseAcItem(
        Level level, int x, int y, int z, Random var5, CallbackInfo ci,
        @Local ItemInstance var10,
        @Local(ordinal = 4) int var7,
        @Local(ordinal = 5) int var8,
        @Local(ordinal = 0) double var11,
        @Local(ordinal = 1) double var13,
        @Local(ordinal = 2) double var15) {
        if (var10.id == AC_Items.bombArow.id) {
            var var22 = new AC_EntityArrowBomb(level, var11, var13, var15);
            var22.shoot(var7, 0.1F, var8, 1.1F, 6.0F);
            level.addEntity(var22);
            level.levelEvent(1002, x, y, z, 0);
            ci.cancel();
        }
    }

    @Override
    public void ac$onRemove(Level level, int x, int y, int z, boolean dropItems) {
        if (dropItems) {
            super.onRemove(level, x, y, z);
        } else {
            level.removeTileEntity(x, y, z);
        }
    }
}
