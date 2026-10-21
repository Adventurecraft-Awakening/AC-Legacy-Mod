package dev.adventurecraft.awakening.client.render.lighting;

/** Renderer-only notification coalescing, with explicit nesting and renderer replacement invalidation. */
public final class TorchLightUpdateBatch {

    private final TorchLightSections sections = new TorchLightSections();
    private int depth;
    private int lastDirtySections;
    private long epoch;

    public long begin() {
        this.depth++;
        return this.epoch;
    }

    public boolean tileChanged(int x, int y, int z) {
        if (this.depth == 0) return false;
        this.sections.addPaddedBlock(x, y, z);
        return true;
    }

    public boolean end(long epoch, TorchLightSections lightSections, TorchLightSections.SectionConsumer invalidate) {
        if (this.epoch != epoch || this.depth == 0) return false;
        this.sections.addAll(lightSections);
        if (--this.depth != 0) return false;
        this.lastDirtySections = this.sections.size();
        try {
            this.sections.forEach(invalidate);
        } finally {
            this.sections.clear();
        }
        return true;
    }

    public int lastDirtySections() {
        return this.lastDirtySections;
    }

    public void reset() {
        this.sections.clear();
        this.depth = 0;
        this.lastDirtySections = 0;
        this.epoch++;
    }
}
