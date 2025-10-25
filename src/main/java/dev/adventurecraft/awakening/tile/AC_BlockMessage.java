package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiMessage;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockMessage extends TileEntityTile implements AC_ITriggerDebugBlock {

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
    public void onTriggerActivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityMessage entityMessage)) {
            return;
        }
        if (!entityMessage.message.isEmpty()) {
            Minecraft.instance.gui.addMessage(entityMessage.message);
        }

        if (!entityMessage.sound.isEmpty()) {
            world.playSound(x + 0.5D, y + 0.5D, z + 0.5D, entityMessage.sound, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if (world.getTileEntity(x, y, z) instanceof AC_TileEntityMessage entityMessage) {
            AC_GuiMessage.showUI(entityMessage);
            return true;
        }
        return false;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}
