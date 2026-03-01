package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.tile.AC_IBlockColor;
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

        int amount = player.isSneaking() ? -1 : 1;

        Coord min = AC_ItemCursor.one().min(AC_ItemCursor.two());
        Coord max = AC_ItemCursor.one().max(AC_ItemCursor.two());

        for (int bX = min.x; bX <= max.x; bX++) {
            for (int bY = min.y; bY <= max.y; bY++) {
                for (int bZ = min.z; bZ <= max.z; bZ++) {
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
