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
        int var1 = 0;
        this.tileEntities.clear();

        for (int var2 = this.minX; var2 <= this.maxX; ++var2) {
            for (int var3 = this.minZ; var3 <= this.maxZ; ++var3) {
                for (int var4 = this.minY; var4 <= this.maxY; ++var4) {
                    int var5 = this.world.getBlockId(var2, var4, var3);
                    int var6 = this.world.getBlockMeta(var2, var4, var3);
                    this.blockIDs[var1] = (byte) ExChunk.translate128(var5);
                    this.metadatas[var1] = (byte) var6;
                    BlockEntity var7 = this.world.getBlockEntity(var2, var4, var3);
                    if (var7 != null) {
                        CompoundTag var8 = new CompoundTag();
                        var7.writeNBT(var8);
                        this.tileEntities.add(var8);
                    }

                    ++var1;
                }
            }
        }

        this.world.getChunk(this.x, this.z).method_885();
    }

    public void loadCurrentArea() {
        if (this.blockIDs != null) {
            int var1 = 0;

            for (int var2 = this.minX; var2 <= this.maxX; ++var2) {
                for (int var3 = this.minZ; var3 <= this.maxZ; ++var3) {
                    for (int var4 = this.minY; var4 <= this.maxY; ++var4) {
                        int var5 = this.world.getBlockId(var2, var4, var3);
                        ((ExWorld) this.world).cancelBlockUpdate(var2, var4, var3, var5);
                        int var6 = ExChunk.translate256(this.blockIDs[var1]);
                        byte var7 = this.metadatas[var1];
                        this.world.placeBlockWithMetaData(var2, var4, var3, var6, var7);
                        this.world.removeBlockEntity(var2, var4, var3);
                        ++var1;
                    }
                }
            }

            for (CompoundTag var9 : this.tileEntities) {
                BlockEntity var10 = ofNBT(var9);
                this.world.setBlockEntity(var10.x, var10.y, var10.z, var10);
            }

        }
    }

    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        if (var1.containsKey("blockIDs")) {
            this.blockIDs = var1.getByteArray("blockIDs");
        }

        if (var1.containsKey("metadatas")) {
            this.metadatas = var1.getByteArray("metadatas");
        }

        if (var1.containsKey("numTiles")) {
            this.tileEntities.clear();
            int var2 = var1.getInt("numTiles");

            for (int var3 = 0; var3 < var2; ++var3) {
                this.tileEntities.add(var1.getCompoundTag(String.format("tile%d", var3)));
            }
        }

        if (!var1.containsKey("acVersion") && ((ExWorldProperties) Minecraft.instance.world.properties).isOriginallyFromAC()) {
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
            int var2 = 0;

            for (CompoundTag var4 : this.tileEntities) {
                var1.put(String.format("tile%d", var2), (AbstractTag) var4);
                ++var2;
            }

            var1.put("numTiles", var2);
        }

        var1.put("acVersion", (int) 0);
    }
}
