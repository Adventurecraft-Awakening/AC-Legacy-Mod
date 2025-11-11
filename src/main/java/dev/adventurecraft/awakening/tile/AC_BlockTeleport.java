package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTeleport;
import net.minecraft.client.Minecraft;
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
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTeleport.class);
        if (!entity.hasPosition) {
            return;
        }

        int targetY = entity.y;
        while (targetY < 128 && world.getMaterial(entity.x, targetY, entity.z) != Material.AIR) {
            ++targetY;
        }

        for (Player player : (List<Player>) world.players) {
            player.setPos((double) entity.x + 0.5D, targetY, (double) entity.z + 0.5D);
        }
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.showDebugGuiOnUse(player)) {
            return false;
        }
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTeleport.class);
        Coord pos = AC_ItemCursor.min();
        entity.x = pos.x;
        entity.y = pos.y;
        entity.z = pos.z;
        entity.hasPosition = true;
        Minecraft.instance.gui.addMessage(String.format(
            "Setting Teleport (%d, %d, %d)",
            entity.x,
            entity.y,
            entity.z
        ));
        return true;
    }
}
