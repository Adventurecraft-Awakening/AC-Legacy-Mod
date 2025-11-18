package dev.adventurecraft.awakening.world.region;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.world.BlockRegion;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;

public final class BlockEntityLayer extends BlockMetaLayer {

    // Use map instead of array assuming that tile entities are very uncommon.
    private final Int2ObjectMap<CompoundTag> tileEntities;

    public BlockEntityLayer(int width, int height, int depth) {
        super(width, height, depth);
        this.tileEntities = new Int2ObjectOpenHashMap<>();
    }

    public Int2ObjectMap<CompoundTag> getTileEntities() {
        return this.tileEntities;
    }

    public CompoundTag putTileEntity(Coord min, Coord max, CompoundTag tag) {
        int x = tag.getInt("x") - min.x;
        int y = tag.getInt("y") - min.y;
        int z = tag.getInt("z") - min.z;
        int h = max.y - min.y + 1;
        int d = max.z - min.z + 1;
        return this.tileEntities.put(BlockRegion.makeIndex(x, y, z, h, d), tag);
    }

    @Override
    public boolean readBlock(Level level, int index, int x, int y, int z) {
        if (super.readBlock(level, index, x, y, z) && Tile.isEntityTile[this.getBlock(index)]) {
            this.saveTileEntity(level, index, x, y, z);
            return true;
        }
        return false;
    }

    private void saveTileEntity(Level level, int index, int x, int y, int z) {
        TileEntity entity = ((ExWorld) level).ac$tryGetTileEntity(x, y, z, TileEntity.class);
        if (entity == null) {
            logMissingTileEntity(level, index, x, y, z);
            return;
        }

        var tag = new CompoundTag();
        entity.save(tag);
        this.tileEntities.put(index, tag);
    }

    @Override
    public boolean clearBlock(Level level, int index, int x, int y, int z) {
        int tileId = this.getBlock(index);
        if (Tile.isEntityTile[tileId]) {
            // This clears containers without dropping them as items.
            Tile.tiles[tileId].onPlace(level, x, y, z);
        }
        return super.clearBlock(level, index, x, y, z);
    }

    @Override
    public boolean writeBlock(Level level, int index, int x, int y, int z) {
        if (!super.writeBlock(level, index, x, y, z)) {
            return false;
        }

        CompoundTag tag = this.tileEntities.get(index);
        if (tag == null) {
            return false;
        }

        var entity = ((ExWorld) level).ac$tryGetTileEntity(x, y, z, TileEntity.class);
        if (entity == null) {
            logMissingTileEntity(level, index, x, y, z);
            return false;
        }

        entity.load(tag);
        entity.level = level;
        entity.x = x;
        entity.y = y;
        entity.z = z;
        return true;
    }

    private static void logMissingTileEntity(Level level, int index, int x, int y, int z) {
        ACMod.LOGGER.error("Missing tile entity during block region copy.");
    }
}
