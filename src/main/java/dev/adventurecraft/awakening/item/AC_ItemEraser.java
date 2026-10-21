package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.world.BlockPos;
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
        if (!EditorSession.forLevel(world).selection.bothSet) {
            return false;
        }

        BlockPos min = EditorSession.forLevel(world).selection.one.min(EditorSession.forLevel(world).selection.two);
        BlockPos max = EditorSession.forLevel(world).selection.one.max(EditorSession.forLevel(world).selection.two);

        BlockPos delta = max.sub(min);
        int width = delta.x() + 1;
        int height = delta.y() + 1;
        int depth = delta.z() + 1;
        int blockCount = width * height * depth;
        Minecraft.instance.gui.addMessage(String.format("Erasing Area (%d blocks)", blockCount));

        // TODO: bulk fill
        for (int bX = min.x(); bX <= max.x(); bX++) {
            for (int bY = min.y(); bY <= max.y(); bY++) {
                for (int bZ = min.z(); bZ <= max.z(); bZ++) {
                    world.setTile(bX, bY, bZ, 0);
                }
            }
        }
        return false;
    }
}
