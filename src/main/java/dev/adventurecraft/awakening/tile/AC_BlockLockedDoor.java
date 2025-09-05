package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.client.renderer.BlockShapes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AC_BlockLockedDoor extends Tile implements AC_ITriggerDebugBlock {

    int doorKeyToUse;

    protected AC_BlockLockedDoor(int var1, int var2, int var3) {
        super(var1, Material.METAL);
        this.tex = var2;
        this.doorKeyToUse = var3;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public HitResult clip(Level level, int x, int y, int z, Vec3 var5, Vec3 var6) {
        int data = level.getData(x, y, z);
        return data == 1 ? null : super.clip(level, x, y, z, var5, var6);
    }

    @Override
    public AABB getAABB(Level level, int x, int y, int z) {
        int data = level.getData(x, y, z);
        return data != 1 ? super.getAABB(level, x, y, z) : null;
    }

    @Override
    public int getRenderShape(LevelSource view, int x, int y, int z) {
        if (AC_DebugMode.isActive() || view.getData(x, y, z) == 0) {
            return this.getRenderShape();
        }
        return BlockShapes.NONE;
    }

    public @Override boolean canBeTriggered() {
        return false;
    }

    @Override
    public int getTexture(LevelSource level, int x, int y, int z, int side) {
        if (side == 0 || side == 1) {
            return this.tex;
        }

        int c = 1;
        while (level.getTile(x, y + c, z) == this.id) {
            ++c;
        }

        int b;
        for (b = 1; level.getTile(x, y - b, z) == this.id; ++b) {
            ++c;
        }

        int id = this.tex;
        if (c > 2) {
            if (c / 2 == b - 1) {
                int a = 1;

                b = 1;
                while (level.getTile(x + a, y, z) == this.id) {
                    ++a;
                }

                while (level.getTile(x - b, y, z) == this.id) {
                    ++a;
                    ++b;
                }

                if (a == 1) {
                    while (level.getTile(x, y, z + a) == this.id) {
                        ++a;
                    }

                    while (level.getTile(x, y, z - b) == this.id) {
                        ++a;
                        ++b;
                    }
                }

                if (a / 2 == b - 1) {
                    ++id;
                }
            }
            return id;
        }

        id += 16;
        if (level.getTile(x, y - 1, z) != this.id) {
            id += 16;
        }

        if (side == 2) {
            if (level.getTile(x + 1, y, z) == this.id) {
                ++id;
            }
        }
        else if (side == 3) {
            if (level.getTile(x - 1, y, z) == this.id) {
                ++id;
            }
        }
        else if (side == 4) {
            if (level.getTile(x, y, z - 1) == this.id) {
                ++id;
            }
        }
        else if (side == 5 && level.getTile(x, y, z + 1) == this.id) {
            ++id;
        }
        return id;
    }

    @Override
    public void attack(Level level, int x, int y, int z, Player player) {
        if (!player.inventory.removeResource(this.doorKeyToUse)) {
            return;
        }

        float pitch = level.random.nextFloat() * 0.1F + 0.9F;
        level.playSound(x + 0.5D, y + 0.5D, z + 0.5D, "random.door_open", 1.0F, pitch);

        int a;
        int b;
        for (a = 0; level.getTile(x, y + a, z) == this.id; ++a) {
            for (b = 0; level.getTile(x + b, y + a, z) == this.id; ++b) {
                level.setData(x + b, y + a, z, 1);
                level.sendTileUpdated(x + b, y + a, z);
            }

            for (b = 1; level.getTile(x - b, y + a, z) == this.id; ++b) {
                level.setData(x - b, y + a, z, 1);
                level.sendTileUpdated(x - b, y + a, z);
            }

            for (b = 1; level.getTile(x, y + a, z + b) == this.id; ++b) {
                level.setData(x, y + a, z + b, 1);
                level.sendTileUpdated(x, y + a, z + b);
            }

            for (b = 1; level.getTile(x, y + a, z - b) == this.id; ++b) {
                level.setData(x, y + a, z - b, 1);
                level.sendTileUpdated(x, y + a, z - b);
            }
        }

        for (a = -1; level.getTile(x, y + a, z) == this.id; --a) {
            for (b = 0; level.getTile(x + b, y + a, z) == this.id; ++b) {
                level.setData(x + b, y + a, z, 1);
                level.sendTileUpdated(x + b, y + a, z);
            }

            for (b = 1; level.getTile(x - b, y + a, z) == this.id; ++b) {
                level.setData(x - b, y + a, z, 1);
                level.sendTileUpdated(x - b, y + a, z);
            }

            for (b = 1; level.getTile(x, y + a, z + b) == this.id; ++b) {
                level.setData(x, y + a, z + b, 1);
                level.sendTileUpdated(x, y + a, z + b);
            }

            for (b = 1; level.getTile(x, y + a, z - b) == this.id; ++b) {
                level.setData(x, y + a, z - b, 1);
                level.sendTileUpdated(x, y + a, z - b);
            }
        }
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        if (!forDeath) {
            world.setData(x, y, z, 0);
        }
    }

    @Override
    public int alwaysUseClick(Level world, int x, int y, int z) {
        return 0;
    }
}
