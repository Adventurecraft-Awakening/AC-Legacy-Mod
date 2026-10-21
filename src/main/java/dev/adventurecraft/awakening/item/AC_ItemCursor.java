package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.math.IntVec3;
import dev.adventurecraft.awakening.editor.EditorSelection;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AC_ItemCursor extends Item implements AC_ILeftClickItem {

    protected AC_ItemCursor(int id) {
        super(id);
    }

    @Override
    public boolean onItemUseLeftClick(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        return this.useOn(stack, player, world, x, y, z, side);
    }

    @Override
    public boolean useOn(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        EditorSelection selection = EditorSession.forLevel(world).selection;
        int positionIndex;
        if (selection.firstPosition) {
            selection.one.set(new BlockPos(x, y, z));
            positionIndex = 0;
        }
        else {
            selection.two.set(new BlockPos(x, y, z));
            selection.bothSet = true;
            positionIndex = 1;
        }
        String message = String.format("Setting Cursor Position %d (%d, %d, %d)", positionIndex + 1, x, y, z);

        selection.min.set(selection.one.min(selection.two));
        selection.max.set(selection.one.max(selection.two));
        selection.firstPosition = !selection.firstPosition;

        if (selection.bothSet) {
            BlockPos delta = selection.max.sub(selection.min);
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
        return EditorSession.current().selection.one;
    }

    public static BlockPos setOne(@NotNull IntVec3 one) {
        return EditorSession.current().selection.one.set(one);
    }

    public static BlockPos two() {
        return EditorSession.current().selection.two;
    }

    public static BlockPos setTwo(@NotNull IntVec3 two) {
        return EditorSession.current().selection.two.set(two);
    }

    public static BlockPos min() {
        return EditorSession.current().selection.min;
    }

    public static BlockPos setMin(@NotNull IntVec3 min) {
        return EditorSession.current().selection.min.set(min);
    }

    public static BlockPos max() {
        return EditorSession.current().selection.max;
    }

    public static BlockPos setMax(@NotNull IntVec3 max) {
        return EditorSession.current().selection.max.set(max);
    }
}
