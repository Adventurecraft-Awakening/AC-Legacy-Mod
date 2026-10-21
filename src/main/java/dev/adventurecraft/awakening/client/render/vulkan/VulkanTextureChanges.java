package dev.adventurecraft.awakening.client.render.vulkan;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;

/** Short-lived UV regions whose changed texture pixels cannot reuse temporal color history. */
final class VulkanTextureChanges {
    static final int GRID_SIZE = 16;
    static final int WORDS = GRID_SIZE * GRID_SIZE / Integer.SIZE;
    static final int BYTES = WORDS * Integer.BYTES;
    private final int width;
    private final int height;
    private final int mipLevels;
    private final long[] lastChangedSubmission;

    VulkanTextureChanges(int width, int height, int mipLevels) {
        if (width < 1 || height < 1 || mipLevels < 1
            || mipLevels > Integer.SIZE - Integer.numberOfLeadingZeros(Math.max(width, height)))
            throw new IllegalArgumentException("Texture change tracking requires valid dimensions and mip levels");
        this.width = width;
        this.height = height;
        this.mipLevels = mipLevels;
        lastChangedSubmission = new long[GRID_SIZE * GRID_SIZE];
        // Initial uploads have no prior image to reject; allocation changes reset history elsewhere.
        Arrays.fill(lastChangedSubmission, Long.MIN_VALUE);
    }

    private VulkanTextureChanges(VulkanTextureChanges source) {
        width = source.width;
        height = source.height;
        mipLevels = source.mipLevels;
        lastChangedSubmission = source.lastChangedSubmission.clone();
    }

    /** A new image snapshot must not change the metadata observed by already queued old-image draws. */
    VulkanTextureChanges copy() { return new VulkanTextureChanges(this); }

    void mark(int level, int x, int y, int regionWidth, int regionHeight, long submission) {
        requireSubmission(submission);
        if (level < 0 || level >= mipLevels) throw new IllegalArgumentException("Changed mip level exceeds its allocation");
        int mipWidth = Math.max(1, width >> level);
        int mipHeight = Math.max(1, height >> level);
        if (x < 0 || y < 0 || regionWidth < 1 || regionHeight < 1
            || (long) x + regionWidth > mipWidth || (long) y + regionHeight > mipHeight)
            throw new IllegalArgumentException("Changed region exceeds its mip allocation");
        int firstX = (int) ((long) x * GRID_SIZE / mipWidth);
        int firstY = (int) ((long) y * GRID_SIZE / mipHeight);
        int lastX = (int) ((((long) x + regionWidth) * GRID_SIZE - 1) / mipWidth);
        int lastY = (int) ((((long) y + regionHeight) * GRID_SIZE - 1) / mipHeight);
        for (int cellY = firstY; cellY <= lastY; cellY++) {
            for (int cellX = firstX; cellX <= lastX; cellX++) {
                int cell = cellY * GRID_SIZE + cellX;
                lastChangedSubmission[cell] = Math.max(lastChangedSubmission[cell], submission);
            }
        }
    }

    /** Writes eight uint words without changing the buffer's position or limit. */
    void writeReactiveTiles(ByteBuffer target, int offset, long submission) {
        requireTarget(target, offset, submission);
        for (int word = 0; word < WORDS; word++) target.putInt(offset + word * Integer.BYTES, 0);
        appendReactiveTiles(target, offset, submission);
    }

    /** Adds companion-map changes to the same albedo UV mask. */
    void appendReactiveTiles(ByteBuffer target, int offset, long submission) {
        requireTarget(target, offset, submission);
        long preceding = submission - 1;
        for (int cell = 0; cell < lastChangedSubmission.length; cell++) {
            long changed = lastChangedSubmission[cell];
            // The following submission also rejects history, then a static or reverted animation can settle.
            if (changed < preceding || changed > submission) continue;
            int wordOffset = offset + cell / Integer.SIZE * Integer.BYTES;
            target.putInt(wordOffset, target.getInt(wordOffset) | 1 << (cell % Integer.SIZE));
        }
    }

    private static void requireTarget(ByteBuffer target, int offset, long submission) {
        Objects.requireNonNull(target, "target");
        requireSubmission(submission);
        if (target.isReadOnly()) throw new ReadOnlyBufferException();
        if (offset < 0 || (long) offset + BYTES > target.limit())
            throw new IndexOutOfBoundsException("Texture change mask exceeds its destination buffer");
    }

    private static void requireSubmission(long submission) {
        if (submission < 0) throw new IllegalArgumentException("Texture change submission must be nonnegative");
    }
}
