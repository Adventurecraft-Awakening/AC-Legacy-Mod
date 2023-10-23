package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemEraser extends Item {

    protected AC_ItemEraser(int id) {
        super(id);
    }

    @Override
    public boolean useOnBlock(ItemStack item, PlayerEntity player, World world, int x, int y, int z, int side) {
        if (AC_ItemCursor.bothSet) {
            Minecraft.instance.overlay.addChatMessage("Erasing Area");
            int minX = Math.min(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
            int maxX = Math.max(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
            int minY = Math.min(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
            int maxY = Math.max(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
            int minZ = Math.min(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
            int maxZ = Math.max(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);

            for (int bX = minX; bX <= maxX; ++bX) {
                for (int bY = minY; bY <= maxY; ++bY) {
                    for (int bZ = minZ; bZ <= maxZ; ++bZ) {
                        world.setBlock(bX, bY, bZ, 0);
                    }
                }
            }
        }
        return false;
    }
}
