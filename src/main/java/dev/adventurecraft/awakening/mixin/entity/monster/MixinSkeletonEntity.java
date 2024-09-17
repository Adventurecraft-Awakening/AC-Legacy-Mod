package dev.adventurecraft.awakening.mixin.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Skeleton.class)
public abstract class MixinSkeletonEntity extends MixinMonsterEntity {

    @Shadow
    protected abstract int getDeathLoot();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        this.setHeldItem(new ItemInstance(Item.BOW, 1));
    }

    @ModifyExpressionValue(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    private boolean conditionalMobBurn(boolean value) {
        return value && ((ExWorldProperties) this.level.levelData).getMobsBurn();
    }

    @Inject(
        method = "checkHurtTarget",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addEntity(Lnet/minecraft/world/entity/Entity;)Z",
            shift = At.Shift.BEFORE))
    private void setArrowStrength(Entity var1, float var2, CallbackInfo ci, @Local Arrow arrowEntity) {
        ((ExArrowEntity) arrowEntity).setAttackStrength(this.damage);
    }

    @Overwrite
    public void dropDeathLoot() {
        if (this.getDeathLoot() != 0) {
            int dropAmount = this.random.nextInt(3) + 1;

            for (int i = 0; i < dropAmount; ++i) {
                ItemEntity itemEntity = this.spawnAtLocation(this.getDeathLoot(), 1);
                if (this.getDeathLoot() != Item.ARROW.id) {
                    itemEntity.item.count = 3;
                }
            }
        }

        int boneAmount = this.random.nextInt(3);
        for (int i = 0; i < boneAmount; ++i) {
            this.spawnAtLocation(Item.BONE.id, 1);
        }
    }

    @Environment(value = EnvType.CLIENT)
    @Overwrite
    public ItemInstance getCarriedItem() {
        return this.getSelectedItem();
    }
}
