package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AC_ItemQuill extends Item {

    protected AC_ItemQuill(int id) {
        super(id);
    }

    @Override
    public boolean useOn(ItemInstance item, Player player, Level world, int x, int y, int z, int side) {
        double newY = 128.0D;
        double offset = 0.001; // helps round upwards

        for (int bY = y; bY <= 128; ++bY) {
            if (world.getTile(x, bY, z) == 0) {
                newY = (double) bY + player.heightOffset + offset;
                break;
            }
        }

        double newX = x + 0.5D;
        double newZ = z + 0.5D;
        Minecraft.instance.gui.addMessage(String.format("Teleporting to (%.1f, %.1f %.1f)", newX, newY, newZ));
        player.setPos(newX, newY, newZ);
        return false;
    }
}
