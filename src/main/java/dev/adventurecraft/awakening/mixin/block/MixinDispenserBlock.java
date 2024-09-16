package dev.adventurecraft.awakening.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_EntityArrowBomb;
import dev.adventurecraft.awakening.common.AC_Items;
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
        Level var1, int var2, int var3, int var4, Random var5, CallbackInfo ci,
        @Local ItemInstance var10,
        @Local(ordinal = 4) int var7,
        @Local(ordinal = 5) int var8,
        @Local(ordinal = 0) double var11,
        @Local(ordinal = 1) double var13,
        @Local(ordinal = 2) double var15) {
        if (var10.id == AC_Items.bombArow.id) {
            AC_EntityArrowBomb var22 = new AC_EntityArrowBomb(var1, var11, var13, var15);
            var22.shoot(var7, 0.1F, var8, 1.1F, 6.0F);
            var1.addEntity(var22);
            var1.levelEvent(1002, var2, var3, var4, 0);
            ci.cancel();
        }
    }
}
