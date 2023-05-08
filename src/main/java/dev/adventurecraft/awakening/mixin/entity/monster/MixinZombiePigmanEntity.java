package dev.adventurecraft.awakening.mixin.entity.monster;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombiePigmanEntity.class)
public abstract class MixinZombiePigmanEntity extends MixinZombieEntity {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(World var1, CallbackInfo ci) {
        this.heldItem = new ItemStack(Item.GOLD_SWORD, 1);
    }

    @Environment(value = EnvType.CLIENT)
    @Overwrite
    public ItemStack getMonsterHeldItem() {
        return this.heldItem;
    }
}
