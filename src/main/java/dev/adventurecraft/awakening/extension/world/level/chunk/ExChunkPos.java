package dev.adventurecraft.awakening.extension.world.level.chunk;

import dev.adventurecraft.awakening.util.PosUtil;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.world.level.chunk.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ExChunkPos {

    int x();

    int z();

    default BlockPos blockAt(int x, int y, int z) {
        return new BlockPos.Mut(this.blockX(x), y, this.blockZ(z));
    }

    default int blockX(int offset) {
        return PosUtil.sectionToBlockCoord(this.x()) + offset;
    }

    default int blockZ(int offset) {
        return PosUtil.sectionToBlockCoord(this.z()) + offset;
    }

    default int centerBlockX() {
        return this.blockX(8);
    }

    default int centerBlockZ() {
        return this.blockZ(8);
    }

    default int minBlockX() {
        return PosUtil.sectionToBlockCoord(this.x());
    }

    default int minBlockZ() {
        return PosUtil.sectionToBlockCoord(this.z());
    }

    default BlockPos minBlockAt(int y) {
        return new BlockPos.Mut(this.minBlockX(), y, this.minBlockZ());
    }

    default int maxBlockX() {
        return this.blockX(15);
    }

    default int maxBlockZ() {
        return this.blockZ(15);
    }

    default BlockPos maxBlockAt(int y) {
        return new BlockPos.Mut(this.maxBlockX(), y, this.maxBlockZ());
    }

    static ChunkPos ofBlock(int x, int z) {
        return new ChunkPos(PosUtil.blockToSectionCoord(x), PosUtil.blockToSectionCoord(z));
    }

    static ChunkPos of(BlockPos pos) {
        return ofBlock(pos.x(), pos.z());
    }

    static Stream<ChunkPos> betweenClosed(ChunkPos from, ChunkPos to) {
        int xSize = Math.abs(from.x - to.x) + 1;
        int zSize = Math.abs(from.z - to.z) + 1;
        return StreamSupport.stream(
            new Spliterators.AbstractSpliterator<>((long) xSize * zSize, Spliterator.SIZED) {

                private final int xDiff = from.x < to.x ? 1 : -1;
                private final int zDiff = from.z < to.z ? 1 : -1;
                private @Nullable ChunkPos pos;

                public boolean tryAdvance(Consumer<? super ChunkPos> action) {
                    if (this.pos == null) {
                        this.pos = from;
                    }
                    else {
                        int x = this.pos.x;
                        int z = this.pos.z;
                        if (x == to.x) {
                            if (z == to.z) {
                                return false;
                            }
                            this.pos = new ChunkPos(from.x, z + this.zDiff);
                        }
                        else {
                            this.pos = new ChunkPos(x + this.xDiff, z);
                        }
                    }
                    action.accept(this.pos);
                    return true;
                }
            }, false
        );
    }

    static Stream<ChunkPos> betweenClosed(ChunkPos center, int radius) {
        return betweenClosed(
            new ChunkPos(center.x - radius, center.z - radius),
            new ChunkPos(center.x + radius, center.z + radius)
        );
    }
}
