package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.world.BlockPos;
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
        if (!EditorSession.forLevel(world).selection.bothSet) {
            return false;
        }

        int id = world.getTile(bX, bY, bZ);
        int meta = world.getData(bX, bY, bZ);
        Minecraft.instance.gui.addMessage(String.format("Swapping Area With BlockID %d", id));

        BlockPos min = EditorSession.forLevel(world).selection.one.min(EditorSession.forLevel(world).selection.two);
        BlockPos max = EditorSession.forLevel(world).selection.one.max(EditorSession.forLevel(world).selection.two);

        // TODO: block fill
        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
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
