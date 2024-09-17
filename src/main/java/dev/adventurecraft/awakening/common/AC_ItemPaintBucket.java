package dev.adventurecraft.awakening.common;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class AC_ItemPaintBucket extends Item {

    protected AC_ItemPaintBucket(int id) {
        super(id);
    }

    public boolean useOn(ItemInstance stack, Player player, Level world, int x, int y, int z, int meta) {
        // TODO: use on singular block?
        if (!AC_ItemCursor.bothSet) {
            return false;
        }

        int minX = Math.min(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
        int maxX = Math.max(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
        int minY = Math.min(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
        int maxY = Math.max(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
        int minZ = Math.min(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
        int maxZ = Math.max(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
        int amount = player.isSneaking() ? -1 : 1;

        for (int bX = minX; bX <= maxX; ++bX) {
            for (int bY = minY; bY <= maxY; ++bY) {
                for (int bZ = minZ; bZ <= maxZ; ++bZ) {
                    Tile block = Tile.tiles[world.getTile(bX, bY, bZ)];
                    if (block instanceof AC_IBlockColor colorBlock) {
                        colorBlock.incrementColor(world, bX, bY, bZ, amount);
                        world.sendTileUpdated(bX, bY, bZ);
                    }
                }
            }
        }
        return false;
    }
}
