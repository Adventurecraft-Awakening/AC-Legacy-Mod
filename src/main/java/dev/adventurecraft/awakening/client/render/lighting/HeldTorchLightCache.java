package dev.adventurecraft.awakening.client.render.lighting;

/**
 * Legacy held-light sampling, separate from world notifications and terrain scheduling.
 * The cache is deliberately written in XYZ order: gameplay listeners can read it during updates.
 */
public final class HeldTorchLightCache {

    public static final int BRIGHTNESS = 15;
    public static final int DIAMETER = BRIGHTNESS * 2 + 1;
    public static final int SAMPLE_COUNT = DIAMETER * DIAMETER * DIAMETER;

    private final float[] values = new float[SAMPLE_COUNT];
    private final TorchLightSections contributingSections = new TorchLightSections();
    private final TorchLightSections dirtySections = new TorchLightSections();
    private int originX;
    private int originY;
    private int originZ;
    private int updateDepth;
    private Statistics statistics = new Statistics(0, 0, 0, 0, 0, 0);

    public float get(int x, int y, int z) {
        int bx = x - this.originX + BRIGHTNESS;
        int by = y - this.originY + BRIGHTNESS;
        int bz = z - this.originZ + BRIGHTNESS;
        if (bx < 0 || bx >= DIAMETER || by < 0 || by >= DIAMETER || bz < 0 || bz >= DIAMETER) return 0.0F;
        return this.values[(bx * DIAMETER + by) * DIAMETER + bz];
    }

    public Statistics statistics() {
        return this.statistics;
    }

    /** Reused until the next update; consume after the cache has finished publishing. */
    public TorchLightSections dirtySections() {
        return this.dirtySections;
    }

    public void update(float x, float y, float z, boolean wasActive, boolean active,
                       boolean nonnegativeOpacity, Samples source) {
        int previousLightReads = 0;
        int index = 0;
        int opacityReads = 0;
        int lightReads = 0;
        int notifications = 0;
        this.updateDepth++;
        try {
            if (this.updateDepth == 1) {
                this.dirtySections.clear();
                if (wasActive) {
                    this.dirtySections.addAll(this.contributingSections);
                    // Ambient light can darken while the torch stands still. An independent
                    // terrain rebuild may then start using previously submerged torch light.
                    // Recheck the old samples before relocating the cache, rather than always
                    // rebuilding its whole positive footprint in bright daylight.
                    for (int i = 0; i < this.values.length; i++) {
                        float value = this.values[i];
                        if (value <= 0.0F) continue;
                        int bx = i / (DIAMETER * DIAMETER) + this.originX - BRIGHTNESS;
                        int by = i / DIAMETER % DIAMETER + this.originY - BRIGHTNESS;
                        int bz = i % DIAMETER + this.originZ - BRIGHTNESS;
                        previousLightReads++;
                        if (value > source.lightLevel(bx, by, bz)) this.dirtySections.addPaddedBlock(bx, by, bz);
                    }
                }
                this.contributingSections.clear();
            } else {
                // Gameplay callbacks can reenter the legacy global cache update. Keep
                // every intermediate footprint until the outer renderer batch flushes.
                this.dirtySections.addAll(this.contributingSections);
            }

            // Keep Java's truncation toward zero, including fractional negative positions.
            this.originX = (int) x;
            this.originY = (int) y;
            this.originZ = (int) z;
            float baseX = (x - (float) this.originX) - 0.5F;
            float baseY = (y - (float) this.originY) - 0.5F;
            float baseZ = (z - (float) this.originZ) - 0.5F;
            for (int rx = -BRIGHTNESS; rx <= BRIGHTNESS; rx++) {
                int bx = rx + this.originX;
                float xLight = Math.abs(rx - baseX);
                for (int ry = -BRIGHTNESS; ry <= BRIGHTNESS; ry++) {
                    int by = ry + this.originY;
                    float xyLight = xLight + Math.abs(ry - baseY);
                    for (int rz = -BRIGHTNESS; rz <= BRIGHTNESS; rz++) {
                        int bz = rz + this.originZ;
                        float distance = xyLight + Math.abs(rz - baseZ);
                        float result = 0.0F;
                        boolean contributes = false;
                        // Opacity can only increase falloff in the normal table. Reset skipped
                        // cache cells in the same order, without fetching their world tiles.
                        // Custom negative opacity retains the original complete world scan.
                        if (!nonnegativeOpacity || distance <= BRIGHTNESS) {
                            int opacity = source.opacity(bx, by, bz);
                            opacityReads++;
                            if (opacity < BRIGHTNESS) {
                                float light = distance + opacity * 1.5F;
                                if (light <= BRIGHTNESS) {
                                    result = BRIGHTNESS - light;
                                    lightReads++;
                                    contributes = result > source.lightLevel(bx, by, bz);
                                    if (contributes) {
                                        // Preserve every non-render listener's original timing:
                                        // notify before publishing this individual cache cell.
                                        source.tileChanged(bx, by, bz);
                                        notifications++;
                                    }
                                }
                            }
                        }
                        this.values[index++] = result;
                        if (active && contributes) this.contributingSections.addPaddedBlock(bx, by, bz);
                    }
                }
            }
        } finally {
            this.dirtySections.addAll(this.contributingSections);
            this.statistics = new Statistics(index, opacityReads, lightReads, previousLightReads, notifications, this.dirtySections.size());
            this.updateDepth--;
        }
    }

    public interface Samples {
        int opacity(int x, int y, int z);

        int lightLevel(int x, int y, int z);

        void tileChanged(int x, int y, int z);
    }

    public record Statistics(int cacheSamples, int opacityReads, int lightReads, int previousLightReads, int tileNotifications,
                             int affectedRenderSections) { }
}
