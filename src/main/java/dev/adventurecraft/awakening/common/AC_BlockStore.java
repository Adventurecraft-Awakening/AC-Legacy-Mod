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

public class AC_BlockStore extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockStore(int var1, int var2) {
        super(var1, var2, Material.GLASS);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityStore();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public int getRenderPass() {
        return 1;
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        var entity = (AC_TileEntityStore) world.getBlockEntity(x, y, z);
        if (AC_DebugMode.active) {
            AC_GuiStoreDebug.showUI(entity);
            return true;
        }

        if (entity.buySupplyLeft == 0) {
            return false;
        }

        if (entity.sellItemID != 0 &&
            !((ExPlayerInventory) player.inventory).consumeItemAmount(entity.sellItemID, entity.sellItemDamage, entity.sellItemAmount)) {
            Minecraft.instance.overlay.addChatMessage("Don't have enough to trade.");
            return true;
        }

        if (entity.buyItemID != 0) {
            player.inventory.addStack(new ItemStack(entity.buyItemID, entity.buyItemAmount, entity.buyItemDamage));
        }

        --entity.buySupplyLeft;
        if (entity.tradeTrigger != null) {
            ((ExWorld) world).getTriggerManager().addArea(x, y, z, entity.tradeTrigger);
            ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
        }
        return true;
    }

    @Override
    public void reset(World world, int x, int y, int z, boolean forDeath) {
        var entity = (AC_TileEntityStore) world.getBlockEntity(x, y, z);
        entity.buySupplyLeft = entity.buySupply;
    }
}
