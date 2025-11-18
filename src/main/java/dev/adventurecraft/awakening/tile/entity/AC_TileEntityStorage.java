package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.world.BlockRegion;
import dev.adventurecraft.awakening.world.region.BlockEntityLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.TickNextTickData;

public class AC_TileEntityStorage extends AC_TileEntityMinMax {

    BlockRegion blockRegion;

    public void setArea(Coord min, Coord max) {
        this.setMin(min);
        this.setMax(max);

        this.blockRegion = BlockRegion.fromCoords(this.min(), this.max(), true);
        this.saveCurrentArea();
    }

    public void saveCurrentArea() {
        if (this.blockRegion == null) {
            return;
        }
        this.blockRegion.readBlocks(this.level, this.min(), this.max());
        this.setChanged();
    }

    public void loadCurrentArea() {
        if (this.blockRegion == null) {
            return;
        }
        var entry = new TickNextTickData(0, 0, 0, 0);
        this.blockRegion.forEachBlock(
            this.level, this.min(), this.max(), (level, index, x, y, z) -> {
                entry.x = x;
                entry.y = y;
                entry.z = z;
                entry.priority = level.getTile(x, y, z);
                return ((ExWorld) level).cancelBlockUpdate(entry);
            }
        );
        this.blockRegion.writeBlocks(this.level, this.min(), this.max());
        this.blockRegion.updateBlocks(this.level, this.min(), this.max());
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        var exTag = (ExCompoundTag) tag;

        var blockIds = exTag.findByteArray("blockIDs").orElse(null);
        var metadata = exTag.findByteArray("metadatas").orElse(null);
        if (blockIds == null || metadata == null) {
            return;
        }
        if (!tag.hasKey("acVersion") && ((ExWorldProperties) this.level.levelData).isOriginallyFromAC()) {
            AC_Blocks.convertACVersion(blockIds);
        }

        this.blockRegion = BlockRegion.fromCoords(this.min(), this.max(), true);
        var layer = (BlockEntityLayer) this.blockRegion.getLayer();
        layer.getBlockBuffer().put(blockIds);
        layer.getMetaBuffer().put(metadata);

        var numTiles = exTag.findInt("numTiles");
        if (numTiles.isPresent()) {
            int tileCount = numTiles.get();
            for (int i = 0; i < tileCount; ++i) {
                layer.putTileEntity(this.min(), this.max(), tag.getCompoundTag(String.format("tile%d", i)));
            }
        }
    }

    public void save(CompoundTag tag) {
        super.save(tag);

        if (this.blockRegion != null) {
            this.saveLayer(tag, (BlockEntityLayer) this.blockRegion.getLayer());
        }
    }

    private void saveLayer(CompoundTag tag, BlockEntityLayer layer) {
        tag.putByteArray("blockIDs", layer.getBlockBuffer().array());
        tag.putByteArray("metadatas", layer.getMetaBuffer().array());

        var tileEntities = layer.getTileEntities();
        if (!tileEntities.isEmpty()) {
            int tileIndex = 0;
            for (CompoundTag tileTag : tileEntities.values()) {
                tag.putCompoundTag(String.format("tile%d", tileIndex), tileTag);
                tileIndex++;
            }
            tag.putInt("numTiles", tileIndex);
        }

        tag.putInt("acVersion", 0);
    }
}
