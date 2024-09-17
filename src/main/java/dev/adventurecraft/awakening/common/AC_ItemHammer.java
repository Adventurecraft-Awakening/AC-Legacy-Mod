package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class AC_ItemHammer extends Item {

    protected AC_ItemHammer(int id) {
        super(id);
    }

    @Override
    public boolean useOn(ItemInstance item, Player player, Level world, int bX, int bY, int bZ, int side) {
        if (!AC_ItemCursor.bothSet) {
            return false;
        }

        int id = world.getTile(bX, bY, bZ);
        int meta = world.getData(bX, bY, bZ);
        Minecraft.instance.gui.addMessage(String.format("Swapping Area With BlockID %d", id));
        int minX = Math.min(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
        int maxX = Math.max(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
        int minY = Math.min(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
        int maxY = Math.max(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
        int minZ = Math.min(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
        int maxZ = Math.max(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    world.setTileAndData(x, y, z, id, meta);
                }
            }
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemInstance item, Tile block) {
        return 32.0F;
    }

    @Override
    public boolean canDestroySpecial(Tile block) {
        return true;
    }

    @Override
    public boolean isMirroredArt() {
        return true;
    }
}
