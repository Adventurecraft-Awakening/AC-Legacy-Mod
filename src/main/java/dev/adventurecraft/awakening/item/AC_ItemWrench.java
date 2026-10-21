package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class AC_ItemWrench extends Item {

    protected AC_ItemWrench(int var1) {
        super(var1);
    }

    public @Override boolean useOn(ItemInstance item, Player player, Level level, int x, int y, int z, int face) {
        if (!EditorSession.forLevel(level).selection.bothSet) {
            return false;
        }

        int tileId = level.getTile(x, y, z);
        int tileMeta = level.getData(x, y, z);
        Minecraft.instance.gui.addMessage(String.format("Swapping blocks with BlockID %d", tileId));

        BlockPos min = EditorSession.forLevel(level).selection.one.min(EditorSession.forLevel(level).selection.two);
        BlockPos max = EditorSession.forLevel(level).selection.one.max(EditorSession.forLevel(level).selection.two);

        // TODO: get in bulk, filter, and set in bulk?
        for (int bX = min.x(); bX <= max.x(); bX++) {
            for (int bY = min.y(); bY <= max.y(); bY++) {
                for (int bZ = min.z(); bZ <= max.z(); bZ++) {
                    int foundId = level.getTile(bX, bY, bZ);
                    switch (foundId) {
                        case 0, 23, 25, 46, 55, 61, 62, 64, 69, 70, 71, 72, 75, 76, 77, 93, 94,
                             101, 102, 103, 104, 107, 108, 109, 110, 111, 115, 117, 118 -> { }
                        default -> level.setTileAndData(bX, bY, bZ, tileId, tileMeta);
                    }
                }
            }
        }
        return false;
    }

    public @Override float getDestroySpeed(ItemInstance item, Tile tile) {
        return 32.0F;
    }

    public @Override boolean canDestroySpecial(Tile tile) {
        return true;
    }

    public @Override boolean isMirroredArt() {
        return true;
    }
}
