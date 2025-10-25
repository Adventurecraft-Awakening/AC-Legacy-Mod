package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiStoreDebug;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityStore;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_BlockStore extends TileEntityTile implements AC_ITriggerBlock {

    protected AC_BlockStore(int var1, int var2) {
        super(var1, var2, Material.GLASS);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityStore();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public int getRenderLayer() {
        return 1;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityStore entityStore)) {
            return false;
        }
        if (AC_DebugMode.active) {
            AC_GuiStoreDebug.showUI(entityStore);
            return true;
        }

        if (entityStore.buySupplyLeft == 0) {
            return false;
        }

        if (entityStore.sellItemID != 0 &&
            !((ExPlayerInventory) player.inventory).consumeItemAmount(
                entityStore.sellItemID,
                entityStore.sellItemDamage,
                entityStore.sellItemAmount
            )) {
            Minecraft.instance.gui.addMessage("Don't have enough to trade.");
            return true;
        }

        if (entityStore.buyItemID != 0) {
            player.inventory.add(new ItemInstance(
                entityStore.buyItemID,
                entityStore.buyItemAmount,
                entityStore.buyItemDamage
            ));
        }

        --entityStore.buySupplyLeft;
        if (entityStore.tradeTrigger != null) {
            ((ExWorld) world).getTriggerManager().addArea(x, y, z, entityStore.tradeTrigger);
            ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
        }
        return true;
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        if (world.getTileEntity(x, y, z) instanceof AC_TileEntityStore entityStore) {
            entityStore.buySupplyLeft = entityStore.buySupply;
        }
    }
}
