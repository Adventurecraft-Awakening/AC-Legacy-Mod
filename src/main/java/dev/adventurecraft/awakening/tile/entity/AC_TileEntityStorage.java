package dev.adventurecraft.awakening.tile.entity;

import java.util.ArrayList;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityStorage extends AC_TileEntityMinMax {

    byte[] blockIDs = null;
    byte[] metadatas = null;
    ArrayList<CompoundTag> tileEntities = new ArrayList<>();

    public void setArea() {
        this.setMin(AC_ItemCursor.min());
        this.setMax(AC_ItemCursor.max());
        Coord delta = this.max().sub(this.min());
        int width = delta.x + 1;
        int height = delta.y + 1;
        int depth = delta.z + 1;
        int volume = width * height * depth;
        this.blockIDs = new byte[volume];
        this.metadatas = new byte[volume];
        this.saveCurrentArea();
    }

    public void saveCurrentArea() {
        if (this.blockIDs == null) {
            return;
        }

        int blockIndex = 0;
        this.tileEntities.clear();

        Coord min = this.min();
        Coord max = this.max();
        for (int x = min.x; x <= max.x; ++x) {
            for (int z = min.z; z <= max.z; ++z) {
                for (int y = min.y; y <= max.y; ++y) {
                    int id = this.level.getTile(x, y, z);
                    int meta = this.level.getData(x, y, z);
                    this.blockIDs[blockIndex] = (byte) ExChunk.translate128(id);
                    this.metadatas[blockIndex] = (byte) meta;
                    TileEntity tileEntity = this.level.getTileEntity(x, y, z);
                    if (tileEntity != null) {
                        var tag = new CompoundTag();
                        tileEntity.save(tag);
                        this.tileEntities.add(tag);
                    }
                    ++blockIndex;
                }
            }
        }

        this.setChanged();
    }

    public void loadCurrentArea() {
        if (this.blockIDs == null) {
            return;
        }

        int blockIndex = 0;

        Coord min = this.min();
        Coord max = this.max();
        for (int x = min.x; x <= max.x; ++x) {
            for (int z = min.z; z <= max.z; ++z) {
                for (int y = min.y; y <= max.y; ++y) {
                    int id = this.level.getTile(x, y, z);
                    ((ExWorld) this.level).cancelBlockUpdate(x, y, z, id);
                    int id256 = ExChunk.translate256(this.blockIDs[blockIndex]);
                    byte meta = this.metadatas[blockIndex];
                    this.level.setTileAndData(x, y, z, id256, meta);
                    this.level.removeTileEntity(x, y, z);
                    ++blockIndex;
                }
            }
        }

        for (CompoundTag tag : this.tileEntities) {
            TileEntity tileEntity = loadStatic(tag);
            this.level.setTileEntity(tileEntity.x, tileEntity.y, tileEntity.z, tileEntity);
        }
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        var exTag = (ExCompoundTag) tag;

        exTag.findByteArray("blockIDs").ifPresent(a -> this.blockIDs = a);
        exTag.findByteArray("metadatas").ifPresent(a -> this.metadatas = a);

        var numTiles = exTag.findInt("numTiles");
        if (numTiles.isPresent()) {
            this.tileEntities.clear();

            int tileCount = numTiles.get();
            for (int i = 0; i < tileCount; ++i) {
                this.tileEntities.add(tag.getCompoundTag(String.format("tile%d", i)));
            }
        }

        if (!tag.hasKey("acVersion") && ((ExWorldProperties) Minecraft.instance.level.levelData).isOriginallyFromAC()) {
            AC_Blocks.convertACVersion(this.blockIDs);
        }
    }

    public void save(CompoundTag tag) {
        super.save(tag);

        if (this.blockIDs != null) {
            tag.putByteArray("blockIDs", this.blockIDs);
        }

        if (this.metadatas != null) {
            tag.putByteArray("metadatas", this.metadatas);
        }

        if (!this.tileEntities.isEmpty()) {
            int tileIndex = 0;

            for (CompoundTag tileTag : this.tileEntities) {
                tag.putCompoundTag(String.format("tile%d", tileIndex), tileTag);
                ++tileIndex;
            }

            tag.putInt("numTiles", tileIndex);
        }

        tag.putInt("acVersion", 0);
    }
}
