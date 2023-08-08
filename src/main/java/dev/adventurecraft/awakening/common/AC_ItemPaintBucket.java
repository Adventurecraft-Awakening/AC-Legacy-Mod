package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemPaintBucket extends Item {

    protected AC_ItemPaintBucket(int var1) {
        super(var1);
    }

    public boolean useOnBlock(ItemStack item, PlayerEntity player, World world, int x, int y, int z, int meta) {
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
        int amount = player.method_1373() ? -1 : 1;

        for (int bX = minX; bX <= maxX; ++bX) {
            for (int bY = minY; bY <= maxY; ++bY) {
                for (int bZ = minZ; bZ <= maxZ; ++bZ) {
                    Block block = Block.BY_ID[world.getBlockId(bX, bY, bZ)];
                    if (block instanceof AC_IBlockColor colorBlock) {
                        colorBlock.incrementColor(world, bX, bY, bZ, amount);
                        world.notifyListeners(bX, bY, bZ);
                    }
                }
            }
        }
        return false;
    }
}
