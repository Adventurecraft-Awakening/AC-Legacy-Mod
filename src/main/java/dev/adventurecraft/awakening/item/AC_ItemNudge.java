package dev.adventurecraft.awakening.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AC_ItemNudge extends Item implements AC_ILeftClickItem {

    public AC_ItemNudge(int id) {
        super(id);
    }

    @Override
    public ItemInstance use(ItemInstance stack, Level world, Player player) {
        if (!AC_ItemCursor.bothSet) {
            return stack;
        }

        LivingEntity viewEntity = Minecraft.instance.cameraEntity;
        Vec3 viewRot = viewEntity.getLookAngle();
        int width = AC_ItemCursor.maxX - AC_ItemCursor.minX + 1;
        int height = AC_ItemCursor.maxY - AC_ItemCursor.minY + 1;
        int depth = AC_ItemCursor.maxZ - AC_ItemCursor.minZ + 1;
        int[] blockArray = new int[width * height * depth];
        int[] metaArray = new int[width * height * depth];

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = world.getTile(x + AC_ItemCursor.minX, y + AC_ItemCursor.minY, z + AC_ItemCursor.minZ);
                    int meta = world.getData(x + AC_ItemCursor.minX, y + AC_ItemCursor.minY, z + AC_ItemCursor.minZ);
                    int i = depth * (height * x + y) + z;
                    blockArray[i] = id;
                    metaArray[i] = meta;
                    world.setTileNoUpdate(x + AC_ItemCursor.minX, y + AC_ItemCursor.minY, z + AC_ItemCursor.minZ, 0);
                }
            }
        }

        double viewX = Math.abs(viewRot.x);
        double viewY = Math.abs(viewRot.y);
        double viewZ = Math.abs(viewRot.z);
        int cX = AC_ItemCursor.minX;
        int cY = AC_ItemCursor.minY;
        int cZ = AC_ItemCursor.minZ;
        if (viewX > viewY && viewX > viewZ) {
            if (viewRot.x < 0.0D) {
                ++cX;
                ++AC_ItemCursor.minX;
                ++AC_ItemCursor.maxX;
                ++AC_ItemCursor.oneX;
                ++AC_ItemCursor.twoX;
            } else {
                --cX;
                --AC_ItemCursor.minX;
                --AC_ItemCursor.maxX;
                --AC_ItemCursor.oneX;
                --AC_ItemCursor.twoX;
            }
        } else if (viewY > viewZ) {
            if (viewRot.y < 0.0D) {
                ++cY;
                ++AC_ItemCursor.minY;
                ++AC_ItemCursor.maxY;
                ++AC_ItemCursor.oneY;
                ++AC_ItemCursor.twoY;
            } else {
                --cY;
                --AC_ItemCursor.minY;
                --AC_ItemCursor.maxY;
                --AC_ItemCursor.oneY;
                --AC_ItemCursor.twoY;
            }
        } else if (viewRot.z < 0.0D) {
            ++cZ;
            ++AC_ItemCursor.minZ;
            ++AC_ItemCursor.maxZ;
            ++AC_ItemCursor.oneZ;
            ++AC_ItemCursor.twoZ;
        } else {
            --cZ;
            --AC_ItemCursor.minZ;
            --AC_ItemCursor.maxZ;
            --AC_ItemCursor.oneZ;
            --AC_ItemCursor.twoZ;
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

        LivingEntity viewEntity = Minecraft.instance.cameraEntity;
        Vec3 viewRot = viewEntity.getLookAngle();
        int width = AC_ItemCursor.maxX - AC_ItemCursor.minX + 1;
        int height = AC_ItemCursor.maxY - AC_ItemCursor.minY + 1;
        int depth = AC_ItemCursor.maxZ - AC_ItemCursor.minZ + 1;
        int[] blockArray = new int[width * height * depth];
        int[] metaArray = new int[width * height * depth];

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int id = world.getTile(x + AC_ItemCursor.minX, y + AC_ItemCursor.minY, z + AC_ItemCursor.minZ);
                    int meta = world.getData(x + AC_ItemCursor.minX, y + AC_ItemCursor.minY, z + AC_ItemCursor.minZ);
                    int i = depth * (height * x + y) + z;
                    blockArray[i] = id;
                    metaArray[i] = meta;
                    world.setTileNoUpdate(x + AC_ItemCursor.minX, y + AC_ItemCursor.minY, z + AC_ItemCursor.minZ, 0);
                }
            }
        }

        double viewX = Math.abs(viewRot.x);
        double viewY = Math.abs(viewRot.y);
        double viewZ = Math.abs(viewRot.z);
        int minX = AC_ItemCursor.minX;
        int minY = AC_ItemCursor.minY;
        int minZ = AC_ItemCursor.minZ;
        if (viewX > viewY && viewX > viewZ) {
            if (viewRot.x > 0.0D) {
                ++minX;
                ++AC_ItemCursor.minX;
                ++AC_ItemCursor.maxX;
                ++AC_ItemCursor.oneX;
                ++AC_ItemCursor.twoX;
            } else {
                --minX;
                --AC_ItemCursor.minX;
                --AC_ItemCursor.maxX;
                --AC_ItemCursor.oneX;
                --AC_ItemCursor.twoX;
            }
        } else if (viewY > viewZ) {
            if (viewRot.y > 0.0D) {
                ++minY;
                ++AC_ItemCursor.minY;
                ++AC_ItemCursor.maxY;
                ++AC_ItemCursor.oneY;
                ++AC_ItemCursor.twoY;
            } else {
                --minY;
                --AC_ItemCursor.minY;
                --AC_ItemCursor.maxY;
                --AC_ItemCursor.oneY;
                --AC_ItemCursor.twoY;
            }
        } else if (viewRot.z > 0.0D) {
            ++minZ;
            ++AC_ItemCursor.minZ;
            ++AC_ItemCursor.maxZ;
            ++AC_ItemCursor.oneZ;
            ++AC_ItemCursor.twoZ;
        } else {
            --minZ;
            --AC_ItemCursor.minZ;
            --AC_ItemCursor.maxZ;
            --AC_ItemCursor.oneZ;
            --AC_ItemCursor.twoZ;
        }

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
}
