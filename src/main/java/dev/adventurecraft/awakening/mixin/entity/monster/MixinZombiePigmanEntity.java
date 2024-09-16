package dev.adventurecraft.awakening.mixin.entity.monster;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.monster.PigZombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PigZombie.class)
public abstract class MixinZombiePigmanEntity extends MixinZombieEntity {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        this.setHeldItem(new ItemInstance(Item.GOLD_SWORD, 1));
    }

    @Environment(value = EnvType.CLIENT)
    @Overwrite
    public ItemInstance getMonsterHeldItem() {
        return this.getHeldItem();
    }
}
