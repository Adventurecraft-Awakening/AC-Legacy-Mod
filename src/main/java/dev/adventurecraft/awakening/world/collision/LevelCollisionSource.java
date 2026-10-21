package dev.adventurecraft.awakening.world.collision;

import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** Live world adapter; neither block shapes nor entity clip flags are cached. */
public final class LevelCollisionSource implements CollisionQueries.Source<Entity> {

    private final Level level;

    public LevelCollisionSource(Level level) {
        this.level = level;
    }

    public @Override boolean hasChunkAt(int x, int y, int z) {
        return this.level.hasChunkAt(x, y, z);
    }

    public @Override boolean addBlockCollisions(
        Entity entity, int x, int y, int z, AABB area, ArrayList<AABB> output
    ) {
        Tile tile = Tile.tiles[this.level.getTile(x, y, z)];
        return tile != null && addBlockCollisions(tile, this.level, entity, x, y, z, area, output);
    }

    public static boolean addBlockCollisions(
        Tile tile, Level level, Entity entity, int x, int y, int z, AABB area, ArrayList<AABB> output
    ) {
        if (((ExEntity) entity).getCollidesWithClipBlocks() || !isClippingBlock(tile.id)) {
            tile.addAABBs(level, x, y, z, area, output);
            return true;
        }
        return false;
    }

    public static boolean isClippingBlock(int id) {
        return id == AC_Blocks.clipBlock.id || ExLadderBlock.isLadderID(id);
    }

    @SuppressWarnings("unchecked")
    public @Override List<Entity> getEntities(Entity entity, AABB area) {
        return this.level.getEntities(entity, area);
    }

    public @Override AABB getCollideBox(Entity entity) {
        return entity.getCollideBox();
    }

    public @Override AABB getCollideAgainstBox(Entity entity, Entity other) {
        return entity.getCollideAgainstBox(other);
    }
}
