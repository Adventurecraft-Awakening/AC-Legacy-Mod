package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_BlockStore extends BlockWithEntity {
    protected AC_BlockStore(int var1, int var2) {
        super(var1, var2, Material.GLASS);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityStore();
    }

    public boolean isFullOpaque() {
        return false;
    }

    public int getRenderPass() {
        return 1;
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        AC_TileEntityStore var6 = (AC_TileEntityStore) var1.getBlockEntity(var2, var3, var4);
        if (AC_DebugMode.active) {
            AC_GuiStoreDebug.showUI(var6);
            return true;
        } else if (var6.buySupplyLeft == 0) {
            return false;
        } else {
            if (var6.sellItemID != 0 && !((ExPlayerInventory) var5.inventory).consumeItemAmount(var6.sellItemID, var6.sellItemDamage, var6.sellItemAmount)) {
                Minecraft.instance.overlay.addChatMessage("Don\'t have enough to trade.");
            } else {
                if (var6.buyItemID != 0) {
                    var5.inventory.addStack(new ItemStack(var6.buyItemID, var6.buyItemAmount, var6.buyItemDamage));
                }

                --var6.buySupplyLeft;
                if (var6.tradeTrigger != null) {
                    ((ExWorld) var1).getTriggerManager().addArea(var2, var3, var4, var6.tradeTrigger);
                    ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);
                }
            }

            return true;
        }
    }

    public void reset(World var1, int var2, int var3, int var4, boolean var5) {
        AC_TileEntityStore var6 = (AC_TileEntityStore) var1.getBlockEntity(var2, var3, var4);
        var6.buySupplyLeft = var6.buySupply;
    }
}
