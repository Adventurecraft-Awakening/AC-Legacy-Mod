package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTeleport;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class AC_BlockTeleport extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockTeleport(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityTeleport();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        return null;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var tileEntity = (AC_TileEntityTeleport) world.getTileEntity(x, y, z);
        if (!tileEntity.hasPosition) {
            return;
        }

        int targetY = tileEntity.y;
        while (targetY < 128 && world.getMaterial(tileEntity.x, targetY, tileEntity.z) != Material.AIR) {
            ++targetY;
        }

        for (Player player : (List<Player>) world.players) {
            player.setPos((double) tileEntity.x + 0.5D, targetY, (double) tileEntity.z + 0.5D);
        }
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            ItemInstance heldItem = player.getSelectedItem();
            if (heldItem != null && heldItem.id == AC_Items.cursor.id) {
                var entity = (AC_TileEntityTeleport) world.getTileEntity(x, y, z);
                Coord pos = AC_ItemCursor.min();
                entity.x = pos.x;
                entity.y = pos.y;
                entity.z = pos.z;
                entity.hasPosition = true;
                Minecraft.instance.gui.addMessage(String.format("Setting Teleport (%d, %d, %d)", entity.x, entity.y, entity.z));
                return true;
            }
        }
        return false;
    }
}
