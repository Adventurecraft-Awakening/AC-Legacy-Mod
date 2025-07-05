package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// TODO: share code with AC_ItemNudge

public class AC_ItemPaste extends Item {

    public AC_ItemPaste(int id) {
        super(id);
    }

    @Override
    public ItemInstance use(ItemInstance item, Level world, Player player) {
        if (!AC_ItemCursor.bothSet) {
            return item;
        }

        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        Mob entity = Minecraft.instance.cameraEntity;
        Vec3 rot = entity.getLookAngle();
        Coord delta = max.sub(min);
        int width = delta.x + 1;
        int height = delta.y + 1;
        int depth = delta.z + 1;
        int[] idArray = new int[width * height * depth];
        int[] metaArray = new int[width * height * depth];

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = world.getTile(x + min.x, y + min.y, z + min.z);
                    int meta = world.getData(x + min.x, y + min.y, z + min.z);
                    int i = depth * (height * x + y) + z;
                    idArray[i] = id;
                    metaArray[i] = meta;
                }
            }
        }

        int baseX = (int) (entity.x + AC_DebugMode.reachDistance * rot.x);
        int baseY = (int) (entity.y + AC_DebugMode.reachDistance * rot.y);
        int baseZ = (int) (entity.z + AC_DebugMode.reachDistance * rot.z);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int i = depth * (height * x + y) + z;
                    int id = idArray[i];
                    int meta = metaArray[i];
                    world.setTileAndDataNoUpdate(baseX + x, baseY + y, baseZ + z, id, meta);
                }
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = idArray[depth * (height * x + y) + z];
                    world.tileUpdated(baseX + x, baseY + y, baseZ + z, id);
                }
            }
        }

        return item;
    }
}
