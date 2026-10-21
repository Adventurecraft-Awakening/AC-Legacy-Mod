package dev.adventurecraft.awakening.client.render.lighting;

import java.util.Arrays;

/** Reusable, exact render-section footprint for a small moving light. */
public final class TorchLightSections {

    private int[] coordinates = new int[3 * 64];
    private int size;

    public void clear() {
        this.size = 0;
    }

    public int size() {
        return this.size;
    }

    /** Match LevelRenderer.tileChanged's one-block mesh sampling border. */
    public void addPaddedBlock(int x, int y, int z) {
        int x0 = Math.floorDiv(x - 1, 16);
        int y0 = Math.floorDiv(y - 1, 16);
        int z0 = Math.floorDiv(z - 1, 16);
        int x1 = Math.floorDiv(x + 1, 16);
        int y1 = Math.floorDiv(y + 1, 16);
        int z1 = Math.floorDiv(z + 1, 16);
        for (int sx = x0; sx <= x1; sx++) {
            for (int sy = y0; sy <= y1; sy++) {
                for (int sz = z0; sz <= z1; sz++) this.add(sx, sy, sz);
            }
        }
    }

    public void addAll(TorchLightSections other) {
        for (int i = 0; i < other.size * 3; i += 3) {
            this.add(other.coordinates[i], other.coordinates[i + 1], other.coordinates[i + 2]);
        }
    }

    private void add(int x, int y, int z) {
        for (int i = 0; i < this.size * 3; i += 3) {
            if (this.coordinates[i] == x && this.coordinates[i + 1] == y && this.coordinates[i + 2] == z) return;
        }
        int offset = this.size++ * 3;
        if (offset + 3 > this.coordinates.length) {
            this.coordinates = Arrays.copyOf(this.coordinates, this.coordinates.length * 2);
        }
        this.coordinates[offset] = x;
        this.coordinates[offset + 1] = y;
        this.coordinates[offset + 2] = z;
    }

    public void forEach(SectionConsumer consumer) {
        for (int i = 0; i < this.size * 3; i += 3) {
            consumer.accept(this.coordinates[i], this.coordinates[i + 1], this.coordinates[i + 2]);
        }
    }

    @FunctionalInterface
    public interface SectionConsumer {
        void accept(int x, int y, int z);
    }
}
