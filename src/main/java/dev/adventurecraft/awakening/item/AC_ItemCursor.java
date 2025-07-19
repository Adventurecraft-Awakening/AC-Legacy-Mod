package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AC_ItemCursor extends Item implements AC_ILeftClickItem {

    public static boolean bothSet = false;
    public static boolean firstPosition = true;

    private static Coord one = Coord.zero;
    private static Coord two = Coord.zero;
    private static Coord min = Coord.zero;
    private static Coord max = Coord.zero;

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
            setOne(new Coord(x, y, z));
            positionIndex = 0;
        }
        else {
            setTwo(new Coord(x, y, z));
            bothSet = true;
            positionIndex = 1;
        }
        String message = String.format("Setting Cursor Position %d (%d, %d, %d)", positionIndex + 1, x, y, z);

        setMin(one().min(two()));
        setMax(one().max(two()));
        firstPosition = !firstPosition;

        if (bothSet) {
            Coord delta = max().sub(min());
            int width = delta.x + 1;
            int height = delta.y + 1;
            int depth = delta.z + 1;
            int blockCount = width * height * depth;

            message += String.format("\nCursor Volume [%d, %d, %d]: %d blocks", width, height, depth, blockCount);
        }

        Minecraft.instance.gui.addMessage(message);
        return false;
    }


    public static Coord one() {
        return one;
    }

    public static Coord setOne(@NotNull Coord one) {
        AC_ItemCursor.one = one;
        return one;
    }

    public static Coord two() {
        return two;
    }

    public static Coord setTwo(@NotNull Coord two) {
        AC_ItemCursor.two = two;
        return two;
    }

    public static Coord min() {
        return min;
    }

    public static Coord setMin(@NotNull Coord min) {
        AC_ItemCursor.min = min;
        return min;
    }

    public static Coord max() {
        return max;
    }

    public static Coord setMax(@NotNull Coord max) {
        AC_ItemCursor.max = max;
        return max;
    }
}
