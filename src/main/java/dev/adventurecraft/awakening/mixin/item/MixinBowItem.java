package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_EntityArrowBomb;
import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class MixinBowItem extends Item {

    public MixinBowItem(int i) {
        super(i);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void tryUseBombArrow(ItemStack item, World world, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack heldItem = player.inventory.getHeldItem();
        ItemStack offhandItem = ((ExPlayerInventory) player.inventory).getOffhandItemStack();
        if (heldItem != null && heldItem.itemId == AC_Items.bombArow.id ||
            offhandItem != null && offhandItem.itemId == AC_Items.bombArow.id) {

            if (player.inventory.removeItem(AC_Items.bombArow.id)) {
                world.playSound(player, "random.bow", 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F));
                if (!world.isClient) {
                    world.spawnEntity(new AC_EntityArrowBomb(world, player));
                }
            }
            cir.setReturnValue(item);
        }
    }
}
