package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockMessage extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockMessage(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityMessage();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
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
        var entity = (AC_TileEntityMessage) world.getBlockEntity(x, y, z);
        if (!entity.message.equals("")) {
            Minecraft.instance.overlay.addChatMessage(entity.message);
        }

        if (!entity.sound.equals("")) {
            world.playSound(x + 0.5D, y + 0.5D, z + 0.5D, entity.sound, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityMessage) world.getBlockEntity(x, y, z);
            AC_GuiMessage.showUI(world, entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
