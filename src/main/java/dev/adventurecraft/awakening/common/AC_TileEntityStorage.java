package dev.adventurecraft.awakening.common;

import java.util.ArrayList;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.AbstractTag;
import net.minecraft.util.io.CompoundTag;

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
        int var1 = this.maxX - this.minX + 1;
        int var2 = this.maxY - this.minY + 1;
        int var3 = this.maxZ - this.minZ + 1;
        int var4 = var1 * var2 * var3;
        this.blockIDs = new byte[var4];
        this.metadatas = new byte[var4];
        this.saveCurrentArea();
    }

    public void saveCurrentArea() {
        int blockIndex = 0;
        this.tileEntities.clear();

        for (int x = this.minX; x <= this.maxX; ++x) {
            for (int z = this.minZ; z <= this.maxZ; ++z) {
                for (int y = this.minY; y <= this.maxY; ++y) {
                    int id = this.world.getBlockId(x, y, z);
                    int meta = this.world.getBlockMeta(x, y, z);
                    this.blockIDs[blockIndex] = (byte) ExChunk.translate128(id);
                    this.metadatas[blockIndex] = (byte) meta;
                    BlockEntity tileEntity = this.world.getBlockEntity(x, y, z);
                    if (tileEntity != null) {
                        CompoundTag tag = new CompoundTag();
                        tileEntity.writeNBT(tag);
                        this.tileEntities.add(tag);
                    }

                    ++blockIndex;
                }
            }
        }

        this.world.getChunk(this.x, this.z).method_885();
    }

    public void loadCurrentArea() {
        if (this.blockIDs != null) {
            int blockIndex = 0;

            for (int x = this.minX; x <= this.maxX; ++x) {
                for (int z = this.minZ; z <= this.maxZ; ++z) {
                    for (int y = this.minY; y <= this.maxY; ++y) {
                        int id = this.world.getBlockId(x, y, z);
                        ((ExWorld) this.world).cancelBlockUpdate(x, y, z, id);
                        int id256 = ExChunk.translate256(this.blockIDs[blockIndex]);
                        byte meta = this.metadatas[blockIndex];
                        this.world.placeBlockWithMetaData(x, y, z, id256, meta);
                        this.world.removeBlockEntity(x, y, z);
                        ++blockIndex;
                    }
                }
            }

            for (CompoundTag tag : this.tileEntities) {
                BlockEntity tileEntity = ofNBT(tag);
                this.world.setBlockEntity(tileEntity.x, tileEntity.y, tileEntity.z, tileEntity);
            }
        }
    }

    public void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        if (tag.containsKey("blockIDs")) {
            this.blockIDs = tag.getByteArray("blockIDs");
        }

        if (tag.containsKey("metadatas")) {
            this.metadatas = tag.getByteArray("metadatas");
        }

        if (tag.containsKey("numTiles")) {
            this.tileEntities.clear();
            int tileCount = tag.getInt("numTiles");

            for (int i = 0; i < tileCount; ++i) {
                this.tileEntities.add(tag.getCompoundTag(String.format("tile%d", i)));
            }
        }

        if (!tag.containsKey("acVersion") && ((ExWorldProperties) Minecraft.instance.world.properties).isOriginallyFromAC()) {
            AC_Blocks.convertACVersion(this.blockIDs);
        }
    }

    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        if (this.blockIDs != null) {
            var1.put("blockIDs", this.blockIDs);
        }

        if (this.metadatas != null) {
            var1.put("metadatas", this.metadatas);
        }

        if (!this.tileEntities.isEmpty()) {
            int tileIndex = 0;

            for (CompoundTag tag : this.tileEntities) {
                var1.put(String.format("tile%d", tileIndex), (AbstractTag) tag);
                ++tileIndex;
            }

            var1.put("numTiles", tileIndex);
        }

        var1.put("acVersion", 0);
    }
}
