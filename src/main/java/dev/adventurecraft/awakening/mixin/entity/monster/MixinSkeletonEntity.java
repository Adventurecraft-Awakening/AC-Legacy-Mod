package dev.adventurecraft.awakening.mixin.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonEntity.class)
public abstract class MixinSkeletonEntity extends MixinMonsterEntity {

    @Shadow
    protected abstract int getMobDrops();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(World var1, CallbackInfo ci) {
        this.heldItem = new ItemStack(Item.BOW, 1);
    }

    @ModifyExpressionValue(method = "updateDespawnCounter", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/World;isDaylight()Z"))
    private boolean conditionalMobBurn(boolean value) {
        return value && ((ExWorldProperties) this.world.properties).getMobsBurn();
    }

    @Inject(
        method = "tryAttack",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/entity/projectile/ArrowEntity;<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)V"))
    private void setArrowStrength(Entity var1, float var2, CallbackInfo ci, @Local ArrowEntity arrowEntity) {
        ((ExArrowEntity) arrowEntity).setAttackStrength(this.attackDamage);
    }

    @Overwrite
    protected void getDrops() {
        if (this.getMobDrops() != 0) {
            int dropAmount = this.rand.nextInt(3) + 1;

            for (int i = 0; i < dropAmount; ++i) {
                ItemEntity itemEntity = this.dropItem(this.getMobDrops(), 1);
                if (this.getMobDrops() != Item.ARROW.id) {
                    itemEntity.stack.count = 3;
                }
            }
        }

        int boneAmount = this.rand.nextInt(3);
        for (int i = 0; i < boneAmount; ++i) {
            this.dropItem(Item.BONE.id, 1);
        }
    }

    @Environment(value = EnvType.CLIENT)
    @Overwrite
    public ItemStack getMonsterHeldItem() {
        return this.heldItem;
    }
}
