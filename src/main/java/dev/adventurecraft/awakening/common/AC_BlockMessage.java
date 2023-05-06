package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockMessage extends BlockWithEntity {
    protected AC_BlockMessage(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityMessage();
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

    public void onTriggerActivated(World world, int x, int y, int z) {
        AC_TileEntityMessage tileEntity = (AC_TileEntityMessage) world.getBlockEntity(x, y, z);
        if (!tileEntity.message.equals("")) {
            Minecraft.instance.overlay.addChatMessage(tileEntity.message);
        }

        if (!tileEntity.sound.equals("")) {
            world.playSound(x + 0.5D, y + 0.5D, z + 0.5D, tileEntity.sound, 1.0F, 1.0F);
        }
    }

    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active) {
            AC_TileEntityMessage tileEntity = (AC_TileEntityMessage) world.getBlockEntity(x, y, z);
            AC_GuiMessage.showUI(world, tileEntity);
            return true;
        } else {
            return false;
        }
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
