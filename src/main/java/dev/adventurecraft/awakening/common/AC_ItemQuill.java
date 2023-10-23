package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemQuill extends Item {

    protected AC_ItemQuill(int id) {
        super(id);
    }

    public boolean useOnBlock(ItemStack item, PlayerEntity player, World world, int x, int y, int z, int side) {
        double newY = 128.0D;
        double offset = 0.001; // helps round upwards

        for (int bY = y; bY <= 128; ++bY) {
            if (world.getBlockId(x, bY, z) == 0) {
                newY = (double) bY + player.standingEyeHeight + offset;
                break;
            }
        }

        double newX = x + 0.5D;
        double newZ = z + 0.5D;
        Minecraft.instance.overlay.addChatMessage(String.format("Teleporting to (%.1f, %.1f %.1f)", newX, newY, newZ));
        player.setPosition(newX, newY, newZ);
        return false;
    }
}
