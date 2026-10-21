package dev.adventurecraft.awakening.client.render.chunk;

/** Soft CPU budgets: a copy or upload already in progress is never interrupted. */
public final class ChunkWorkBudget {
    private final long snapshotNanos;
    private final long uploadNanos;
    private final long uploadBytes;
    private boolean initialized;
    private long frameId;
    private long snapshotRemaining;
    private long uploadRemaining;
    private long bytesRemaining;
    private long deadline;
    private boolean hasDeadline;
    private boolean uploaded;

    public ChunkWorkBudget(long snapshotNanos, long uploadNanos, long uploadBytes) {
        if (snapshotNanos <= 0 || uploadNanos <= 0 || uploadBytes <= 0) {
            throw new IllegalArgumentException("Terrain budgets must be positive");
        }
        this.snapshotNanos = snapshotNanos;
        this.uploadNanos = uploadNanos;
        this.uploadBytes = uploadBytes;
    }

    /** Repeated entry in the same render frame can tighten, but never replenish, a budget. */
    public void beginFrame(long frameId, long now, long deadline, int snapshotMultiplier) {
        if (!this.initialized || this.frameId != frameId) {
            this.initialized = true;
            this.frameId = frameId;
            int multiplier = Math.clamp(snapshotMultiplier, 1, 3);
            this.snapshotRemaining = this.snapshotNanos > Long.MAX_VALUE / multiplier
                ? Long.MAX_VALUE : this.snapshotNanos * multiplier;
            this.uploadRemaining = this.uploadNanos;
            this.bytesRemaining = this.uploadBytes;
            this.hasDeadline = false;
            this.uploaded = false;
        }
        // Match the caller's monotonic deadline convention; zero means no outer deadline.
        long remaining = deadline - now;
        // A deadline already missed on entry must not starve terrain forever at
        // low frame rates. The internal soft budgets still bound that frame.
        if (deadline != 0 && remaining > 0 && remaining <= 1_000_000_000L &&
            (!this.hasDeadline || deadline - this.deadline < 0)) {
            this.deadline = deadline;
            this.hasDeadline = true;
        }
    }

    public boolean canSnapshot(long now) {
        return this.initialized && this.snapshotRemaining > 0 && this.beforeDeadline(now);
    }

    public void recordSnapshot(long elapsedNanos) {
        this.snapshotRemaining = subtract(this.snapshotRemaining, elapsedNanos);
    }

    public boolean canUpload(long now, long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("Negative mesh size");
        return this.initialized && this.uploadRemaining > 0 && this.bytesRemaining > 0 &&
            this.beforeDeadline(now) && (!this.uploaded || bytes <= this.bytesRemaining);
    }

    public void recordUpload(long elapsedNanos, long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("Negative mesh size");
        this.uploadRemaining = subtract(this.uploadRemaining, elapsedNanos);
        this.bytesRemaining = subtract(this.bytesRemaining, bytes);
        this.uploaded = true;
    }

    private boolean beforeDeadline(long now) {
        return !this.hasDeadline || now - this.deadline < 0;
    }

    private static long subtract(long remaining, long amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative work cost");
        return amount >= remaining ? 0 : remaining - amount;
    }
}
