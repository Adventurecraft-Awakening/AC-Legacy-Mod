package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// TODO: share code with AC_ItemPaste
// TODO: share code between methods

public class AC_ItemNudge extends Item implements AC_ILeftClickItem {

    public AC_ItemNudge(int id) {
        super(id);
    }

    @Override
    public ItemInstance use(ItemInstance stack, Level world, Player player) {
        if (!AC_ItemCursor.bothSet) {
            return stack;
        }

        Mob viewEntity = Minecraft.instance.cameraEntity;
        Vec3 viewRot = viewEntity.getLookAngle();
        Coord min = AC_ItemCursor.min();
        Coord delta = AC_ItemCursor.max().sub(min);
        int width = delta.x + 1;
        int height = delta.y + 1;
        int depth = delta.z + 1;
        int[] blockArray = new int[width * height * depth];
        int[] metaArray = new int[width * height * depth];

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = world.getTile(x + min.x, y + min.y, z + min.z);
                    int meta = world.getData(x + min.x, y + min.y, z + min.z);
                    int i = depth * (height * x + y) + z;
                    blockArray[i] = id;
                    metaArray[i] = meta;
                    world.setTileNoUpdate(x + min.x, y + min.y, z + min.z, 0);
                }
            }
        }

        Coord dir = getUnitDirection(viewRot);
        shiftCursor(dir);

        int cX = dir.x + min.x;
        int cY = dir.y + min.y;
        int cZ = dir.z + min.z;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int i = depth * (height * x + y) + z;
                    int id = blockArray[i];
                    int meta = metaArray[i];
                    world.setTileAndDataNoUpdate(cX + x, cY + y, cZ + z, id, meta);
                }
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int i = depth * (height * x + y) + z;
                    int id = blockArray[i];
                    int meta = metaArray[i];
                    world.setTileAndDataNoUpdate(cX + x, cY + y, cZ + z, id, meta);
                }
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = blockArray[depth * (height * x + y) + z];
                    world.tileUpdated(cX + x, cY + y, cZ + z, id);
                }
            }
        }

        return stack;
    }

    @Override
    public void onItemLeftClick(ItemInstance stack, Level world, Player player) {
        if (!AC_ItemCursor.bothSet) {
            return;
        }

        Mob viewEntity = Minecraft.instance.cameraEntity;
        Vec3 viewRot = viewEntity.getLookAngle();
        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        Coord delta = max.sub(min);
        int width = delta.x + 1;
        int height = delta.y + 1;
        int depth = delta.z + 1;
        int[] blockArray = new int[width * height * depth];
        int[] metaArray = new int[width * height * depth];

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = world.getTile(x + min.x, y + min.y, z + min.z);
                    int meta = world.getData(x + min.x, y + min.y, z + min.z);
                    int i = depth * (height * x + y) + z;
                    blockArray[i] = id;
                    metaArray[i] = meta;
                    world.setTileNoUpdate(x + min.x, y + min.y, z + min.z, 0);
                }
            }
        }

        Coord dir = getUnitDirection(viewRot);
        shiftCursor(dir);

        int minX = dir.x + min.x;
        int minY = dir.y + min.y;
        int minZ = dir.z + min.z;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int i = depth * (height * x + y) + z;
                    int id = blockArray[i];
                    int meta = metaArray[i];
                    world.setTileAndDataNoUpdate(minX + x, minY + y, minZ + z, id, meta);
                }
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = blockArray[depth * (height * x + y) + z];
                    world.tileUpdated(minX + x, minY + y, minZ + z, id);
                }
            }
        }
    }

    private static Coord getUnitDirection(Vec3 vec) {
        double viewX = Math.abs(vec.x);
        double viewY = Math.abs(vec.y);
        double viewZ = Math.abs(vec.z);
        int x = 0;
        int y = 0;
        int z = 0;

        if (viewX > viewY && viewX > viewZ) {
            x = vec.x > 0.0D ? 1 : -1;
        }
        else if (viewY > viewZ) {
            y = vec.y > 0.0D ? 1 : -1;
        }
        else {
            z = vec.z > 0.0D ? 1 : -1;
        }
        return new Coord(x, y, z);
    }

    private static void shiftCursor(Coord amount) {
        AC_ItemCursor.setMin(AC_ItemCursor.min().add(amount));
        AC_ItemCursor.setMax(AC_ItemCursor.max().add(amount));
        AC_ItemCursor.setOne(AC_ItemCursor.one().add(amount));
        AC_ItemCursor.setTwo(AC_ItemCursor.two().add(amount));
    }
}
