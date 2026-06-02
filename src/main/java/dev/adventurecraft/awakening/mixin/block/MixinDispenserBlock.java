package dev.adventurecraft.awakening.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.entity.AC_EntityArrowBomb;
import dev.adventurecraft.awakening.item.AC_Items;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.DispenserTile;
import net.minecraft.world.level.tile.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(DispenserTile.class)
public abstract class MixinDispenserBlock extends MixinBlock {

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
        if (!AC_DebugMode.active) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "dispenseFrom",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/ItemInstance;)Lnet/minecraft/world/entity/item/ItemEntity;",
            ordinal = 0
        ),
        cancellable = true
    )
    private void ac$dispenseBombArrow(
        Level level,
        int x,
        int y,
        int z,
        Random random,
        CallbackInfo ci,
        @Local ItemInstance item,
        @Local(ordinal = 4) int vx,
        @Local(ordinal = 5) int vz,
        @Local(ordinal = 0) double dx,
        @Local(ordinal = 1) double dy,
        @Local(ordinal = 2) double dz
    ) {
        if (item.id == AC_Items.bombArow.id) {
            var arrow = new AC_EntityArrowBomb(level, dx, dy, dz);
            arrow.shoot(vx, 0.1F, vz, 1.1F, 6.0F);
            level.addEntity(arrow);
            level.levelEvent(LevelEvent.SOUND_DISPENSER_PROJECTILE_LAUNCH, x, y, z, 0);
            level.levelEvent(LevelEvent.PARTICLES_SHOOT_SMOKE, x, y, z, vx + 1 + (vz + 1) * 3);
            ci.cancel();
        }
    }

    public @Override void ac$onRemove(Level level, int x, int y, int z, boolean dropItems) {
        if (dropItems) {
            super.onRemove(level, x, y, z);
        }
        else {
            level.removeTileEntity(x, y, z);
        }
    }
}
