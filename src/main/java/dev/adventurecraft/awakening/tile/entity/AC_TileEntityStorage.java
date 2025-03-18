package dev.adventurecraft.awakening.tile.entity;

import java.util.ArrayList;

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
        this.minX = AC_ItemCursor.minX;
        this.minY = AC_ItemCursor.minY;
        this.minZ = AC_ItemCursor.minZ;
        this.maxX = AC_ItemCursor.maxX;
        this.maxY = AC_ItemCursor.maxY;
        this.maxZ = AC_ItemCursor.maxZ;
        int width = this.maxX - this.minX + 1;
        int height = this.maxY - this.minY + 1;
        int depth = this.maxZ - this.minZ + 1;
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

        for (int x = this.minX; x <= this.maxX; ++x) {
            for (int z = this.minZ; z <= this.maxZ; ++z) {
                for (int y = this.minY; y <= this.maxY; ++y) {
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

        this.level.getChunkAt(this.x, this.z).markUnsaved();
    }

    public void loadCurrentArea() {
        if (this.blockIDs == null) {
            return;
        }

        int blockIndex = 0;

        for (int x = this.minX; x <= this.maxX; ++x) {
            for (int z = this.minZ; z <= this.maxZ; ++z) {
                for (int y = this.minY; y <= this.maxY; ++y) {
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
        if (tag.hasKey("blockIDs")) {
            this.blockIDs = tag.getByteArray("blockIDs");
        }

        if (tag.hasKey("metadatas")) {
            this.metadatas = tag.getByteArray("metadatas");
        }

        if (tag.hasKey("numTiles")) {
            this.tileEntities.clear();
            int tileCount = tag.getInt("numTiles");

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
