package dev.adventurecraft.awakening.client.render.chunk;

/** Last-frame costs and current queue pressure; all times are monotonic nanoseconds. */
public record ChunkBuildStatistics(
    long frame,
    int queuedSections,
    int pendingBuilds,
    long completedBytes,
    int started,
    int uploaded,
    int discarded,
    int dependencyDeferrals,
    long schedulingNanos,
    long dependencyNanos,
    long snapshotNanos,
    long workerQueueNanos,
    long buildNanos,
    long uploadNanos,
    long uploadedBytes,
    long oldestQueuedNanos
) {
    public static final ChunkBuildStatistics EMPTY = new ChunkBuildStatistics(
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
}
