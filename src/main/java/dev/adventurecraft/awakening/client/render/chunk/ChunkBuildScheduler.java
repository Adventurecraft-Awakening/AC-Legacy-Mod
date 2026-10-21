package dev.adventurecraft.awakening.client.render.chunk;

import dev.adventurecraft.awakening.extension.ExClass_66;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Owns terrain work from dirty admission to upload. Queue decisions, snapshots and
 * uploads belong to the render thread; workers only own their captured builder.
 */
public final class ChunkBuildScheduler {
    private static final long STARVATION_NANOS = 2_000_000_000L;
    private static final int MAX_PENDING_BUILDS = 32;
    private static final long MAX_COMPLETED_BYTES = 64L << 20;

    private final Executor executor;
    private final ChunkWorkBudget budget = new ChunkWorkBudget(2_000_000L, 2_000_000L, 4L << 20);
    private PriorityQueue<QueuedSection> queued = new PriorityQueue<>();
    private final LinkedHashMap<Chunk, QueuedSection> queuedBySection = new LinkedHashMap<>();
    private final IdentityHashMap<Chunk, Build> inFlight = new IdentityHashMap<>();
    private final ConcurrentLinkedQueue<ChunkBuilder> builders = new ConcurrentLinkedQueue<>();
    private final AtomicInteger pendingBuilds = new AtomicInteger();

    // Reset and worker publication share this lock, so a retired world's last
    // worker can release its builder even if no more world frames are rendered.
    private final Object publicationLock = new Object();
    private final ArrayDeque<Build> completed = new ArrayDeque<>();
    private long generation;
    private long completedBytes;

    private Level level;
    private long sequence;
    private boolean priorityInitialized;
    private double priorityX, priorityY, priorityZ;
    private long frame = Long.MIN_VALUE;
    private int started, uploaded, discarded, dependencyDeferrals;
    private long schedulingNanos, dependencyNanos, snapshotNanos;
    private long workerQueueNanos, buildNanos, uploadNanos, uploadedBytes;

    public ChunkBuildScheduler(Executor executor) {
        this.executor = java.util.Objects.requireNonNull(executor);
    }

    /** Cancel current ownership without waiting for CPU work or losing its resources. */
    public void reset(Level level) {
        synchronized (this.publicationLock) {
            ++this.generation;
            for (Build build : this.inFlight.values()) build.builder.cancel();
            this.inFlight.clear();
            while (!this.completed.isEmpty()) this.release(this.completed.removeFirst());
            this.completedBytes = 0;
        }
        this.queued.clear();
        this.queuedBySection.clear();
        this.priorityInitialized = false;
        this.level = level;
    }

    /** One bounded pass, even if a legacy caller enters more than once per frame. */
    public void update(Level level, java.util.List<Chunk> dirty, Mob camera,
                       long frame, long deadline, int snapshotMultiplier) {
        if (this.level != level) this.reset(level);
        long now = System.nanoTime();
        if (this.frame != frame) {
            this.frame = frame;
            this.started = this.uploaded = this.discarded = this.dependencyDeferrals = 0;
            this.schedulingNanos = this.dependencyNanos = this.snapshotNanos = 0;
            this.workerQueueNanos = this.buildNanos = this.uploadNanos = this.uploadedBytes = 0;
        }
        this.budget.beginFrame(frame, now, deadline, snapshotMultiplier);
        if (level == null) return;

        this.prioritize(camera);
        for (Chunk section : dirty) {
            if (section.level != level || !section.dirty) continue;
            Build build = this.inFlight.get(section);
            if (build != null) build.builder.cancel();
            else this.enqueue(section, now);
        }
        dirty.clear();
        for (Build build : this.inFlight.values()) {
            if (build.section.dirty || build.section.level != level || !build.builder.region.isCurrent()) {
                build.builder.cancel();
            }
        }

        long scheduling = System.nanoTime() - now;
        this.schedulingNanos += scheduling;
        this.uploadCompleted();

        var deferred = new ArrayList<QueuedSection>();
        boolean first = true;
        try {
            while (!this.queued.isEmpty() && this.budget.canSnapshot(System.nanoTime()) &&
                   this.pendingBuilds.get() < MAX_PENDING_BUILDS && this.completedBytes() < MAX_COMPLETED_BYTES) {
                QueuedSection queued = this.takeNext(first, now);
                first = false;
                Chunk section = queued.section;
                if (section.level != level || !section.dirty) {
                    this.queuedBySection.remove(section);
                    continue;
                }

                long start = System.nanoTime();
                boolean ready = ChunkDependencies.prepare(level, section);
                long elapsed = System.nanoTime() - start;
                this.dependencyNanos += elapsed;
                this.budget.recordSnapshot(elapsed);
                if (!ready) {
                    ++this.dependencyDeferrals;
                    deferred.add(queued);
                    continue;
                }
                if (!this.budget.canSnapshot(System.nanoTime())) {
                    deferred.add(queued);
                    break;
                }

                start = System.nanoTime();
                ChunkBuilder builder = this.builders.poll();
                if (builder == null) builder = new ChunkBuilder();
                try {
                    // Capture can call live block hooks. A reentrant dirty signal must
                    // survive the copy instead of being cleared after it.
                    section.dirty = false;
                    ((ExClass_66) section).ac$readWorldData(builder);
                    if (section.dirty) builder.cancel();
                } catch (RuntimeException | Error error) {
                    builder.release();
                    this.builders.add(builder);
                    section.setDirty();
                    deferred.add(queued);
                    throw error;
                } finally {
                    elapsed = System.nanoTime() - start;
                    this.snapshotNanos += elapsed;
                    this.budget.recordSnapshot(elapsed);
                }

                this.queuedBySection.remove(section);
                var build = new Build(section, builder, this.generation, queued.queuedAt);
                this.inFlight.put(section, build);
                this.pendingBuilds.incrementAndGet();
                try {
                    this.executor.execute(() -> this.build(build));
                    ++this.started;
                } catch (RuntimeException | Error error) {
                    this.inFlight.remove(section);
                    this.release(build);
                    section.setDirty();
                    this.enqueue(section, queued.queuedAt);
                    throw error;
                }
            }
        } finally {
            this.queued.addAll(deferred);
        }
        this.uploadCompleted();
    }

    private void enqueue(Chunk section, long queuedAt) {
        if (section.level != this.level || !section.dirty || this.queuedBySection.containsKey(section)) return;
        var queued = new QueuedSection(section, queuedAt, this.sequence++);
        this.updatePriority(queued);
        this.queued.add(queued);
        this.queuedBySection.put(section, queued);
    }

    private void prioritize(Mob camera) {
        double dx = camera.x - this.priorityX;
        double dy = camera.y - this.priorityY;
        double dz = camera.z - this.priorityZ;
        if (this.priorityInitialized && dx * dx + dy * dy + dz * dz < 16 * 16) return;
        this.priorityInitialized = true;
        this.priorityX = camera.x;
        this.priorityY = camera.y;
        this.priorityZ = camera.z;
        var entries = new ArrayList<>(this.queued);
        for (QueuedSection entry : entries) this.updatePriority(entry);
        // The collection constructor heapifies in O(n). Ordinary dirty admissions
        // use O(log n) insertion instead of sorting every section every frame.
        this.queued = new PriorityQueue<>(entries);
    }

    private void updatePriority(QueuedSection queued) {
        Chunk section = queued.section;
        double dx = this.priorityX - section.xm;
        double dy = this.priorityY - section.ym;
        double dz = this.priorityZ - section.zm;
        queued.priority = dx * dx + dy * dy + dz * dz + (section.visible ? 0 : 32 * 32);
    }

    private QueuedSection takeNext(boolean allowOldest, long now) {
        if (allowOldest) {
            QueuedSection oldest = this.queuedBySection.firstEntry().getValue();
            if (now - oldest.queuedAt >= STARVATION_NANOS && this.queued.remove(oldest)) return oldest;
        }
        return this.queued.remove();
    }

    private void build(Build build) {
        long start = System.nanoTime();
        build.workerQueueNanos = start - build.submittedAt;
        try {
            if (!build.builder.isCancelled()) ((ExClass_66) build.section).ac$generateMesh(build.builder);
        } catch (Throwable failure) {
            build.failure = failure;
        } finally {
            build.buildNanos = System.nanoTime() - start;
            build.bytes = build.builder.sizeInBytes();
            synchronized (this.publicationLock) {
                if (build.generation == this.generation) {
                    this.completed.addLast(build);
                    this.completedBytes += build.bytes;
                    return;
                }
            }
            // release() only owns CPU snapshot/vertex blocks, never graphics objects.
            this.release(build);
        }
    }

    private void uploadCompleted() {
        while (true) {
            Build build;
            synchronized (this.publicationLock) { build = this.completed.peekFirst(); }
            if (build == null) return;
            boolean current = build.section.level == this.level &&
                ((ExClass_66) build.section).ac$isMeshCurrent(build.builder);
            if (current && build.failure == null && !this.budget.canUpload(System.nanoTime(), build.bytes)) return;
            synchronized (this.publicationLock) {
                this.completed.removeFirst();
                this.completedBytes -= build.bytes;
            }
            this.inFlight.remove(build.section, build);
            this.workerQueueNanos += build.workerQueueNanos;
            this.buildNanos += build.buildNanos;
            try {
                if (!current) {
                    ++this.discarded;
                    if (build.section.level == this.level) build.section.setDirty();
                } else if (build.failure != null) {
                    build.section.setDirty();
                    throw new IllegalStateException("Asynchronous terrain mesh build failed", build.failure);
                } else {
                    long start = System.nanoTime();
                    try {
                        ((ExClass_66) build.section).ac$submitMesh(build.builder);
                        ++this.uploaded;
                        this.uploadedBytes += build.bytes;
                    } catch (RuntimeException | Error error) {
                        build.section.setDirty();
                        throw error;
                    } finally {
                        long elapsed = System.nanoTime() - start;
                        this.uploadNanos += elapsed;
                        this.budget.recordUpload(elapsed, build.bytes);
                    }
                }
            } finally {
                this.release(build);
                this.enqueue(build.section, build.queuedAt);
            }
        }
    }

    private void release(Build build) {
        try { build.builder.release(); }
        finally {
            this.builders.add(build.builder);
            this.pendingBuilds.decrementAndGet();
        }
    }

    private long completedBytes() {
        synchronized (this.publicationLock) { return this.completedBytes; }
    }

    public ChunkBuildStatistics statistics() {
        long oldest = 0;
        long now = System.nanoTime();
        for (QueuedSection queued : this.queued) oldest = Math.max(oldest, now - queued.queuedAt);
        return new ChunkBuildStatistics(this.frame, this.queued.size(), this.pendingBuilds.get(), this.completedBytes(),
            this.started, this.uploaded, this.discarded, this.dependencyDeferrals, this.schedulingNanos,
            this.dependencyNanos, this.snapshotNanos, this.workerQueueNanos, this.buildNanos,
            this.uploadNanos, this.uploadedBytes, oldest);
    }

    private static final class QueuedSection implements Comparable<QueuedSection> {
        final Chunk section;
        final long queuedAt;
        final long sequence;
        double priority;

        @Override
        public int compareTo(QueuedSection other) {
            int distance = Double.compare(this.priority, other.priority);
            return distance != 0 ? distance : Long.compare(this.sequence, other.sequence);
        }

        QueuedSection(Chunk section, long queuedAt, long sequence) {
            this.section = section;
            this.queuedAt = queuedAt;
            this.sequence = sequence;
        }
    }

    private static final class Build {
        final Chunk section;
        final ChunkBuilder builder;
        final long generation;
        final long queuedAt;
        final long submittedAt = System.nanoTime();
        Throwable failure;
        long workerQueueNanos, buildNanos, bytes;

        Build(Chunk section, ChunkBuilder builder, long generation, long queuedAt) {
            this.section = section;
            this.builder = builder;
            this.generation = generation;
            this.queuedAt = queuedAt;
        }
    }
}
