package dev.adventurecraft.awakening.common;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockTeleport extends BlockWithEntity {
    protected AC_BlockTeleport(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTeleport();
    }

    public boolean isFullOpaque() {
        return false;
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityTeleport var5 = (AC_TileEntityTeleport) var1.getBlockEntity(var2, var3, var4);

        int var6 = var5.y;
        while (var6 < 128 && var1.getMaterial(var5.x, var6, var5.z) != Material.AIR) {
            ++var6;
        }

        for (PlayerEntity var9 : (List<PlayerEntity>) var1.players) {
            var9.setPosition((double) var5.x + 0.5D, (double) var6, (double) var5.z + 0.5D);
        }

    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityTeleport var6 = (AC_TileEntityTeleport) var1.getBlockEntity(var2, var3, var4);
            var6.x = AC_ItemCursor.minX;
            var6.y = AC_ItemCursor.minY;
            var6.z = AC_ItemCursor.minZ;
            Minecraft.instance.overlay.addChatMessage(String.format("Setting Teleport (%d, %d, %d)", var6.x, var6.y, var6.z));
            return true;
        } else {
            return false;
        }
    }
}
