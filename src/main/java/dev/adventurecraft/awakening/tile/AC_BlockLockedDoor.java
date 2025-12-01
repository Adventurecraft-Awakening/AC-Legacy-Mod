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

    protected AC_BlockLockedDoor(int id, int tex, int keyItem) {
        super(id, Material.METAL);
        this.tex = tex;
        this.doorKeyToUse = keyItem;

        this.setBlockUpdate();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public HitResult clip(Level level, int x, int y, int z, Vec3 vec1, Vec3 vec2) {
        boolean unlocked = (level.getData(x, y, z) & 1) != 0;
        return !AC_DebugMode.active && unlocked ? null : super.clip(level, x, y, z, vec1, vec2);
    }

    @Override
    public AABB getAABB(Level level, int x, int y, int z) {
        boolean unlocked = (level.getData(x, y, z) & 1) == 0;
        return !AC_DebugMode.active && unlocked ? super.getAABB(level, x, y, z) : null;
    }

    @Override
    public int getRenderShape(LevelSource view, int x, int y, int z) {
        if (AC_DebugMode.active || view.getData(x, y, z) == 0) {
            return this.getRenderShape();
        }
        return BlockShapes.NONE;
    }

    public @Override boolean canBeTriggered() {
        return false;
    }

    public @Override void onPlace(Level level, int x, int y, int z) {
        this.updateSegmentData(level, x, y, z);
    }

    public @Override void neighborChanged(Level level, int x, int y, int z, int tile) {
        this.updateSegmentData(level, x, y, z);
    }

    private void updateSegmentData(Level level, int x, int y, int z) {
        int c = 1;
        while (level.getTile(x, y + c, z) == this.id) {
            ++c;
        }

        int b = 1;
        while (level.getTile(x, y - b, z) == this.id) {
            ++c;
            ++b;
        }

        if (c <= 2) {
            return;
        }
        int segment = 1;
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
                ++segment;
            }
        }

        int data = level.getData(x, y, z);
        int newData = (segment << 1) | (data & 1);
        if (newData != data) {
            level.setData(x, y, z, newData);
        }
    }

    @Override
    public int getTexture(LevelSource level, int x, int y, int z, int side) {
        if (side == 0 || side == 1) {
            return this.tex;
        }

        int segment = level.getData(x, y, z) >>> 1;
        if (segment != 0) {
            return this.tex + segment - 1;
        }

        // Part from second or third row.
        int id = 16 + (level.getTile(x, y - 1, z) != this.id ? 16 : 0);
        int neighbor = switch (side) {
            case 2 -> level.getTile(x + 1, y, z);
            case 3 -> level.getTile(x - 1, y, z);
            case 4 -> level.getTile(x, y, z - 1);
            case 5 -> level.getTile(x, y, z + 1);
            default -> 0;
        };
        return this.tex + id + (neighbor == this.id ? 1 : 0);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void attack(Level level, int x, int y, int z, Player player) {
        if (!player.inventory.removeResource(this.doorKeyToUse)) {
            return;
        }

        float pitch = level.random.nextFloat() * 0.1F + 0.9F;
        level.playSound(x + 0.5D, y + 0.5D, z + 0.5D, "random.door_open", 1.0F, pitch);

        // TODO: update in bulk
        for (int a = 0; level.getTile(x, y + a, z) == this.id; ++a) {
            for (int b = 0; this.unlockOnAttack(level, x + b, y + a, z); b++) {
            }

            for (int b = 1; this.unlockOnAttack(level, x - b, y + a, z); ++b) {
            }

            for (int b = 1; this.unlockOnAttack(level, x, y + a, z + b); ++b) {
            }

            for (int b = 1; this.unlockOnAttack(level, x, y + a, z - b); ++b) {
            }
        }

        // TODO: update in bulk (same as above)
        for (int a = -1; level.getTile(x, y + a, z) == this.id; --a) {
            for (int b = 0; this.unlockOnAttack(level, x + b, y + a, z); ++b) {
            }

            for (int b = 1; this.unlockOnAttack(level, x - b, y + a, z); ++b) {
            }

            for (int b = 1; this.unlockOnAttack(level, x, y + a, z + b); ++b) {
            }

            for (int b = 1; this.unlockOnAttack(level, x, y + a, z - b); ++b) {
            }
        }
    }

    private boolean unlockOnAttack(Level level, int x, int y, int z) {
        if (level.getTile(x, y, z) != this.id) {
            return false;
        }
        int data = level.getData(x, y, z);
        level.setData(x, y, z, data | 1);
        return true;
    }

    @Override
    public void reset(Level level, int x, int y, int z, boolean forDeath) {
        if (!forDeath) {
            int data = level.getData(x, y, z);
            level.setData(x, y, z, data & ~1);
        }
    }

    @Override
    public int alwaysUseClick(Level level, int x, int y, int z) {
        return 0;
    }
}
