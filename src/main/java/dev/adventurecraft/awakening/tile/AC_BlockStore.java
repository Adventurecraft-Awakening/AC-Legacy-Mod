package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityStore;
import dev.adventurecraft.awakening.common.gui.AC_GuiStoreDebug;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorld;
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
        var entity = (AC_TileEntityStore) world.getTileEntity(x, y, z);
        if (((ExPlayerEntity) player).isDebugMode()) {
            AC_GuiStoreDebug.showUI(entity);
            return true;
        }

        if (entity.buySupplyLeft == 0) {
            return false;
        }

        if (entity.sellItemID != 0 &&
            !((ExPlayerInventory) player.inventory).consumeItemAmount(entity.sellItemID, entity.sellItemDamage, entity.sellItemAmount)) {
            Minecraft.instance.gui.addMessage("Don't have enough to trade.");
            return true;
        }

        if (entity.buyItemID != 0) {
            player.inventory.add(new ItemInstance(entity.buyItemID, entity.buyItemAmount, entity.buyItemDamage));
        }

        --entity.buySupplyLeft;
        if (entity.tradeTrigger != null) {
            ((ExWorld) world).getTriggerManager().addArea(x, y, z, entity.tradeTrigger);
            ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
        }
        return true;
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        var entity = (AC_TileEntityStore) world.getTileEntity(x, y, z);
        entity.buySupplyLeft = entity.buySupply;
    }
}
