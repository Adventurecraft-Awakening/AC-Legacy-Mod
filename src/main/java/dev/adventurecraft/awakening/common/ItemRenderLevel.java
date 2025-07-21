package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.level.tile.ExTileEntityTile;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;

public class ItemRenderLevel implements LevelSource {

    private final BiomeSource biomeSource;

    public int tile;
    public int data;
    private TileEntity tileEntity;

    public ItemRenderLevel() {
        this.biomeSource = new FixedBiomeSource(Biome.PLAINS, 0.5F, 0.5F);
    }

    private boolean inRange(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0;
    }

    @Override
    public int getTile(int x, int y, int z) {
        if (this.inRange(x, y, z)) {
            return this.tile;
        }
        return 0;
    }

    public void setTile(int x, int y, int z, int id) {
        if (this.inRange(x, y, z)) {
            this.tile = id;

            if (Tile.tiles[id] instanceof ExTileEntityTile entityTile) {
                this.tileEntity = entityTile.ac$newTileEntity();
            }
        }
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        if (this.inRange(x, y, z)) {
            return this.tileEntity;
        }
        return null;
    }

    @Override
    public float getBrightness(int x, int y, int z, int max) {
        return 1.0F;
    }

    @Override
    public float getBrightness(int x, int y, int z) {
        return 1.0F;
    }

    @Override
    public int getData(int x, int y, int z) {
        if (this.inRange(x, y, z)) {
            return this.data;
        }
        return 0;
    }

    public void setData(int x, int y, int z, int data) {
        if (this.inRange(x, y, z)) {
            this.data = data;
        }
    }

    @Override
    public Material getMaterial(int x, int y, int z) {
        int id = this.getTile(x, y, z);
        return id == 0 ? Material.AIR : Tile.tiles[id].material;
    }

    @Override
    public boolean isSolidTile(int x, int y, int z) {
        Tile tile = Tile.tiles[this.getTile(x, y, z)];
        return tile != null && tile.isSolidRender();
    }

    @Override
    public boolean isSolidBlockingTile(int x, int y, int z) {
        Tile tile = Tile.tiles[this.getTile(x, y, z)];
        if (tile == null) {
            return false;
        }
        return tile.material.isSolidBlocking() && tile.isCubeShaped();
    }

    @Override
    public BiomeSource getBiomeSource() {
        return this.biomeSource;
    }
}
