package dev.adventurecraft.awakening.common;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockHealDamage extends BlockWithEntity {
    protected AC_BlockHealDamage(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityHealDamage();
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
        AC_TileEntityHealDamage var5 = (AC_TileEntityHealDamage) var1.getBlockEntity(var2, var3, var4);

        for (PlayerEntity var8 : (List<PlayerEntity>) var1.players) {
            if (var5.healDamage > 0) {
                var8.addHealth(var5.healDamage);
            } else {
                var8.applyDamage(-var5.healDamage);
            }
        }

    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityHealDamage var6 = (AC_TileEntityHealDamage) var1.getBlockEntity(var2, var3, var4);
            AC_GuiHealDamage.showUI(var1, var6);
            return true;
        } else {
            return false;
        }
    }
}
