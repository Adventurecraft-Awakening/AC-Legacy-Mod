package dev.adventurecraft.awakening.client.render.chunk;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.world.level.tile.Tile;

/**
 * Build-owned copies of the mutable tile tables used by terrain lighting.
 * Capture on the world thread; meshing workers only query these copies.
 */
public final class CapturedTileProperties {

    private static final int TILE_COUNT = 256;

    private final boolean[] neighborLit = new boolean[TILE_COUNT];
    private final boolean[] translucent = new boolean[TILE_COUNT];
    private final int[] lightEmission = new int[TILE_COUNT];

    public void capture() {
        System.arraycopy(ExBlock.neighborLit, 0, this.neighborLit, 0, TILE_COUNT);
        System.arraycopy(Tile.translucent, 0, this.translucent, 0, TILE_COUNT);
        System.arraycopy(Tile.lightEmission, 0, this.lightEmission, 0, TILE_COUNT);
    }

    public boolean neighborLit(int tileId) {
        return this.neighborLit[tileId];
    }

    public boolean isTranslucent(int tileId) {
        return this.translucent[tileId];
    }

    public int lightEmission(int tileId) {
        return this.lightEmission[tileId];
    }
}
