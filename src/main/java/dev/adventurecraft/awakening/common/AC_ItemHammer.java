package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemHammer extends Item {

    protected AC_ItemHammer(int id) {
        super(id);
    }

    @Override
    public boolean useOnBlock(ItemStack item, PlayerEntity player, World world, int bX, int bY, int bZ, int side) {
        if (!AC_ItemCursor.bothSet) {
            return false;
        }

        int id = world.getBlockId(bX, bY, bZ);
        int meta = world.getBlockMeta(bX, bY, bZ);
        Minecraft.instance.overlay.addChatMessage(String.format("Swapping Area With BlockID %d", id));
        int minX = Math.min(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
        int maxX = Math.max(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
        int minY = Math.min(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
        int maxY = Math.max(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
        int minZ = Math.min(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
        int maxZ = Math.max(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    world.placeBlockWithMetaData(x, y, z, id, meta);
                }
            }
        }
        return false;
    }

    @Override
    public float getStrengthOnBlock(ItemStack item, Block block) {
        return 32.0F;
    }

    @Override
    public boolean isEffectiveOn(Block block) {
        return true;
    }

    @Override
    public boolean shouldSpinWhenRendering() {
        return true;
    }
}
