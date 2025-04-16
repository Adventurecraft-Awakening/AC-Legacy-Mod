package dev.adventurecraft.awakening.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AC_ItemCursor extends Item implements AC_ILeftClickItem {

    public static boolean bothSet = false;
    public static boolean firstPosition = true;
    public static int oneX;
    public static int oneY;
    public static int oneZ;
    public static int twoX;
    public static int twoY;
    public static int twoZ;
    public static int minX;
    public static int minY;
    public static int minZ;
    public static int maxX;
    public static int maxY;
    public static int maxZ;

    protected AC_ItemCursor(int id) {
        super(id);
    }

    @Override
    public boolean onItemUseLeftClick(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        return this.useOn(stack, player, world, x, y, z, side);
    }

    @Override
    public boolean useOn(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        int positionIndex;
        if (firstPosition) {
            oneX = x;
            oneY = y;
            oneZ = z;
            positionIndex = 0;
        } else {
            twoX = x;
            twoY = y;
            twoZ = z;
            bothSet = true;
            positionIndex = 1;
        }
        String message = String.format("Setting Cursor Position %d (%d, %d, %d)", positionIndex + 1, x, y, z);

        minX = Math.min(oneX, twoX);
        minY = Math.min(oneY, twoY);
        minZ = Math.min(oneZ, twoZ);
        maxX = Math.max(oneX, twoX);
        maxY = Math.max(oneY, twoY);
        maxZ = Math.max(oneZ, twoZ);
        firstPosition = !firstPosition;

        if (bothSet) {
            int width = maxX - minX + 1;
            int height = maxY - minY + 1;
            int depth = maxZ - minZ + 1;
            int blockCount = width * height * depth;

            message += String.format("\nCursor Volume [%d, %d, %d]: %d blocks", width, height, depth, blockCount);
        }

        Minecraft.instance.gui.addMessage(message);
        return false;
    }
}
