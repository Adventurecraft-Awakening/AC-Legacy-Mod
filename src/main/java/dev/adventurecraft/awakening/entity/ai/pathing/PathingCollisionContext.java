package dev.adventurecraft.awakening.entity.ai.pathing;

import dev.adventurecraft.awakening.extension.world.level.ExRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

import javax.annotation.Nullable;

/** Bridges path query views to the owning world required by Beta's collision-shape API. */
public final class PathingCollisionContext {

    private PathingCollisionContext() {
    }

    public static @Nullable Level levelFor(LevelSource source) {
        if (source instanceof Level level) {
            return level;
        }
        if (source instanceof ExRegion region) {
            return region.ac$getLevel();
        }
        return null;
    }

    public static boolean blocksMotion(LevelSource source, Material material, Tile tile, int x, int y, int z) {
        if (!tile.mayPick()) {
            return false;
        }
        Level level = levelFor(source);
        // An unknown view cannot supply the Level required by dynamic shapes. Its own material
        // remains the fallback; an unrelated active client world is never queried.
        return level != null ? tile.getAABB(level, x, y, z) != null : material.blocksMotion();
    }
}
