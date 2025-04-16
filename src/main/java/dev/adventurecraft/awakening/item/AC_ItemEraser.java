package dev.adventurecraft.awakening.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AC_ItemEraser extends Item {

    protected AC_ItemEraser(int id) {
        super(id);
    }

    @Override
    public boolean useOn(ItemInstance item, Player player, Level world, int x, int y, int z, int side) {
        if (!AC_ItemCursor.bothSet) {
            return false;
        }

        int minX = Math.min(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
        int maxX = Math.max(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
        int minY = Math.min(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
        int maxY = Math.max(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
        int minZ = Math.min(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
        int maxZ = Math.max(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        int blockCount = width * height * depth;
        Minecraft.instance.gui.addMessage(String.format("Erasing Area (%d blocks)", blockCount));

        for (int bX = minX; bX <= maxX; ++bX) {
            for (int bY = minY; bY <= maxY; ++bY) {
                for (int bZ = minZ; bZ <= maxZ; ++bZ) {
                    world.setTile(bX, bY, bZ, 0);
                }
            }
        }
        return false;
    }
}
