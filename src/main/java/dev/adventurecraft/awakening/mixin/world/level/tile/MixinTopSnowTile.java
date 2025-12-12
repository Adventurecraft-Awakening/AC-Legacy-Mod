package dev.adventurecraft.awakening.mixin.world.level.tile;

import dev.adventurecraft.awakening.extension.world.level.tile.ExTopSnowTile;
import dev.adventurecraft.awakening.mixin.block.MixinBlock;
import dev.adventurecraft.awakening.tile.AC_BlockShapes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.stats.Stats;
import net.minecraft.util.Facing;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.TopSnowTile;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TopSnowTile.class)
public abstract class MixinTopSnowTile extends MixinBlock implements ExTopSnowTile {

    @Environment(value = EnvType.CLIENT)
    @Override
    public int getRenderShape() {
        return AC_BlockShapes.SNOW_LAYER;
    }

    @Override
    public float ac$getHeight(LevelSource level, int x, int y, int z) {
        int n = level.getData(x, y, z) & 7;
        return (float) (2 * (1 + n)) / 16.0f;
    }

    @Override
    public void updateShape(LevelSource source, int x, int y, int z) {
        float h = this.ac$getHeight(source, x, y, z);
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, h, 1.0f);
    }

    @Environment(value = EnvType.CLIENT)
    @Override
    public boolean shouldRenderFace(LevelSource level, int x, int y, int z, int face) {
        if (face == Facing.DOWN) {
            return this.yy0 > 0.0 || !level.isSolidTile(x, y, z);
        }
        if (face == Facing.UP) {
            return this.yy1 < 1.0 || !level.isSolidTile(x, y, z);
        }

        int tile = level.getTile(x, y, z);
        if (tile == Tile.SNOW_LAYER.id) {
            float h = ((ExTopSnowTile) Tile.SNOW_LAYER).ac$getHeight(level, x, y, z);
            return h < this.yy1;
        }

        if (face == Facing.NORTH && this.zz0 > 0.0) {
            return true;
        }
        if (face == Facing.SOUTH && this.zz1 < 1.0) {
            return true;
        }
        if (face == Facing.WEST && this.xx0 > 0.0) {
            return true;
        }
        if (face == Facing.EAST && this.xx1 < 1.0) {
            return true;
        }
        return !level.isSolidTile(x, y, z);
    }

    @Override
    public AABB getAABB(Level level, int x, int y, int z) {
        int n = level.getData(x, y, z) & 7;
        if (n >= 3) {
            int h = (n + 1) / 4;
            return AABB.newTemp(x + this.xx0, y + this.yy0, z + this.zz0, x + this.xx1, y + (h * 0.5f), z + this.zz1);
        }
        return null;
    }

    @Override
    public void playerDestroy(Level level, Player player, int x, int y, int z, int meta) {
        level.setTile(x, y, z, 0);
        player.awardStat(Stats.STAT_MINE_BLOCK[this.id], 1);
    }
}
