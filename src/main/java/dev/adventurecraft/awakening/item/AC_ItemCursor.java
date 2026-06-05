package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.math.IntVec3;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AC_ItemCursor extends Item implements AC_ILeftClickItem {

    public static boolean bothSet = false;
    public static boolean firstPosition = true;

    private static BlockPos.Mut one = BlockPos.mutZero();
    private static BlockPos.Mut two = BlockPos.mutZero();
    private static BlockPos.Mut min = BlockPos.mutZero();
    private static BlockPos.Mut max = BlockPos.mutZero();

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
            setOne(new BlockPos(x, y, z));
            positionIndex = 0;
        }
        else {
            setTwo(new BlockPos(x, y, z));
            bothSet = true;
            positionIndex = 1;
        }
        String message = String.format("Setting Cursor Position %d (%d, %d, %d)", positionIndex + 1, x, y, z);

        setMin(one().min(two()));
        setMax(one().max(two()));
        firstPosition = !firstPosition;

        if (bothSet) {
            BlockPos delta = max().sub(min());
            int width = delta.x() + 1;
            int height = delta.y() + 1;
            int depth = delta.z() + 1;
            int blockCount = width * height * depth;

            message += String.format("\nCursor Volume [%d, %d, %d]: %d blocks", width, height, depth, blockCount);
        }

        Minecraft.instance.gui.addMessage(message);
        return false;
    }


    public static BlockPos one() {
        return one;
    }

    public static BlockPos setOne(@NotNull IntVec3 one) {
        return AC_ItemCursor.one.set(one);
    }

    public static BlockPos two() {
        return two;
    }

    public static BlockPos setTwo(@NotNull IntVec3 two) {
        return AC_ItemCursor.two.set(two);
    }

    public static BlockPos min() {
        return min;
    }

    public static BlockPos setMin(@NotNull IntVec3 min) {
        return AC_ItemCursor.min.set(min);
    }

    public static BlockPos max() {
        return max;
    }

    public static BlockPos setMax(@NotNull IntVec3 max) {
        return AC_ItemCursor.max.set(max);
    }
}
