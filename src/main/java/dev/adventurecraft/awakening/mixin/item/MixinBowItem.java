package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_EntityArrowBomb;
import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
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
    private void tryUseBombArrow(ItemInstance item, Level world, Player player, CallbackInfoReturnable<ItemInstance> cir) {
        ItemInstance heldItem = player.inventory.getSelected();
        ItemInstance offhandItem = ((ExPlayerInventory) player.inventory).getOffhandItemStack();
        if (heldItem != null && heldItem.id == AC_Items.bombArow.id ||
            offhandItem != null && offhandItem.id == AC_Items.bombArow.id) {

            if (player.inventory.removeResource(AC_Items.bombArow.id)) {
                world.playSound(player, "random.bow", 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
                if (!world.isClientSide) {
                    world.addEntity(new AC_EntityArrowBomb(world, player));
                }
            }
            cir.setReturnValue(item);
        }
    }
}
