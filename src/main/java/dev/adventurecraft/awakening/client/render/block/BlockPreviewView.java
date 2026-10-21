package dev.adventurecraft.awakening.client.render.block;

import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;

/** A single preview block overlays read-only world inputs without modifying the live level. */
public record BlockPreviewView(LevelSource source, int x, int y, int z, int tileId, int metadata) implements LevelSource {
    public BlockPreviewView { java.util.Objects.requireNonNull(source); }

    private boolean contains(int x, int y, int z) { return this.x == x && this.y == y && this.z == z; }
    @Override public int getTile(int x, int y, int z) { return contains(x, y, z) ? tileId : source.getTile(x, y, z); }
    @Override public int getData(int x, int y, int z) { return contains(x, y, z) ? metadata : source.getData(x, y, z); }
    @Override public TileEntity getTileEntity(int x, int y, int z) { return source.getTileEntity(x, y, z); }
    @Override public float getBrightness(int x, int y, int z, int minimum) { return source.getBrightness(x, y, z, minimum); }
    @Override public float getBrightness(int x, int y, int z) { return source.getBrightness(x, y, z); }
    @Override public BiomeSource getBiomeSource() { return source.getBiomeSource(); }
    @Override public Material getMaterial(int x, int y, int z) {
        if (!contains(x, y, z)) return source.getMaterial(x, y, z);
        Tile tile = Tile.tiles[tileId];
        return tile == null ? Material.AIR : tile.material;
    }
    @Override public boolean isSolidTile(int x, int y, int z) {
        if (!contains(x, y, z)) return source.isSolidTile(x, y, z);
        Tile tile = Tile.tiles[tileId];
        return tile != null && tile.isSolidRender();
    }
    @Override public boolean isSolidBlockingTile(int x, int y, int z) {
        if (!contains(x, y, z)) return source.isSolidBlockingTile(x, y, z);
        Tile tile = Tile.tiles[tileId];
        return tile != null && tile.material.isSolidBlocking() && tile.isCubeShaped();
    }
}
