package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_ITriggerBlock;
import dev.adventurecraft.awakening.common.AC_TileEntityMessage;
import dev.adventurecraft.awakening.common.gui.AC_GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockMessage extends TileEntityTile implements AC_ITriggerBlock {

    protected AC_BlockMessage(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityMessage();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public boolean shouldRender(LevelSource view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityMessage) world.getTileEntity(x, y, z);
        if (!entity.message.equals("")) {
            Minecraft.instance.gui.addMessage(entity.message);
        }

        if (!entity.sound.equals("")) {
            world.playSound(x + 0.5D, y + 0.5D, z + 0.5D, entity.sound, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityMessage) world.getTileEntity(x, y, z);
            AC_GuiMessage.showUI(world, entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}
