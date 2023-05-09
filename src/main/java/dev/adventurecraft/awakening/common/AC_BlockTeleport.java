package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;

public class AC_BlockTeleport extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockTeleport(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTeleport();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        var tileEntity = (AC_TileEntityTeleport) world.getBlockEntity(x, y, z);

        int targetY = tileEntity.y;
        while (targetY < 128 && world.getMaterial(tileEntity.x, targetY, tileEntity.z) != Material.AIR) {
            ++targetY;
        }

        for (PlayerEntity player : (List<PlayerEntity>) world.players) {
            player.setPosition((double) tileEntity.x + 0.5D, targetY, (double) tileEntity.z + 0.5D);
        }

    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && player.getHeldItem() != null && player.getHeldItem().itemId == AC_Items.cursor.id) {
            var entity = (AC_TileEntityTeleport) world.getBlockEntity(x, y, z);
            entity.x = AC_ItemCursor.minX;
            entity.y = AC_ItemCursor.minY;
            entity.z = AC_ItemCursor.minZ;
            Minecraft.instance.overlay.addChatMessage(String.format("Setting Teleport (%d, %d, %d)", entity.x, entity.y, entity.z));
            return true;
        } else {
            return false;
        }
    }
}
