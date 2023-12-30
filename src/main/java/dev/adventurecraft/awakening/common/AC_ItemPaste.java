package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AC_ItemPaste extends Item {

    public AC_ItemPaste(int id) {
        super(id);
    }

    @Override
    public ItemStack use(ItemStack item, World world, PlayerEntity player) {
        if (!AC_ItemCursor.bothSet) {
            return item;
        }

        int minX = AC_ItemCursor.minX;
        int minY = AC_ItemCursor.minY;
        int minZ = AC_ItemCursor.minZ;
        int maxX = AC_ItemCursor.maxX;
        int maxY = AC_ItemCursor.maxY;
        int maxZ = AC_ItemCursor.maxZ;
        LivingEntity entity = Minecraft.instance.viewEntity;
        Vec3d rot = entity.getRotation();
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        int[] idArray = new int[width * height * depth];
        int[] metaArray = new int[width * height * depth];

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = world.getBlockId(x + minX, y + minY, z + minZ);
                    int meta = world.getBlockMeta(x + minX, y + minY, z + minZ);
                    int i = depth * (height * x + y) + z;
                    idArray[i] = id;
                    metaArray[i] = meta;
                }
            }
        }

        int baseX = (int) (entity.x + (double) AC_DebugMode.reachDistance * rot.x);
        int baseY = (int) (entity.y + (double) AC_DebugMode.reachDistance * rot.y);
        int baseZ = (int) (entity.z + (double) AC_DebugMode.reachDistance * rot.z);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int i = depth * (height * x + y) + z;
                    int id = idArray[i];
                    int meta = metaArray[i];
                    world.setBlockWithMetadata(baseX + x, baseY + y, baseZ + z, id, meta);
                }
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = idArray[depth * (height * x + y) + z];
                    world.notifyOfNeighborChange(baseX + x, baseY + y, baseZ + z, id);
                }
            }
        }

        return item;
    }
}
