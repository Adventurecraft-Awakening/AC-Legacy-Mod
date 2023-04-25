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

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityMessage var5 = (AC_TileEntityMessage) var1.getBlockEntity(var2, var3, var4);
        if (!var5.message.equals("")) {
            Minecraft.instance.overlay.addChatMessage(var5.message);
        }

        if (!var5.sound.equals("")) {
            var1.playSound((double) var2 + 0.5D, (double) var3 + 0.5D, (double) var4 + 0.5D, var5.sound, 1.0F, 1.0F);
        }

    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active) {
            AC_TileEntityMessage var6 = (AC_TileEntityMessage) var1.getBlockEntity(var2, var3, var4);
            AC_GuiMessage.showUI(var1, var6);
            return true;
        } else {
            return false;
        }
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
