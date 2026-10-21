package dev.adventurecraft.awakening.world.trigger;

import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.world.BlockPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Map;

/**
 * A tiered X/Z membership index. Every source/id registration occupies at most four cells at
 * one power-of-two scale, so even world-sized areas use bounded space. Queries visit one cell
 * per occupied scale and retain the exact three-dimensional inclusive bounds check.
 */
public final class TriggerAreaIndex {

    private static final int MIN_SHIFT = 4;
    private static final int MAX_SHIFT = 32;

    private final Map<BlockPos, Int2ObjectOpenHashMap<Registration>> sources = new Object2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<ArrayList<Registration>>[] tiers;
    private long occupiedTiers;
    private int registrationCount;
    private int bucketCount;
    private int bucketReferenceCount;
    private long queryCount;
    private long candidateTestCount;
    private int lastQueryCandidateCount;

    @SuppressWarnings("unchecked")
    public TriggerAreaIndex() {
        this.tiers = new Long2ObjectOpenHashMap[MAX_SHIFT + 1];
        for (int shift = MIN_SHIFT; shift <= MAX_SHIFT; ++shift) {
            this.tiers[shift] = new Long2ObjectOpenHashMap<>();
        }
    }

    public void put(BlockPos source, int id, AC_TriggerArea area) {
        var registrations = this.sources.get(source);
        if (registrations == null) {
            registrations = new Int2ObjectOpenHashMap<>();
            this.sources.put(source.freeze(), registrations);
        }
        var registration = new Registration(area);
        var previous = registrations.put(id, registration);
        if (previous != null) {
            this.removeFromBuckets(previous);
        }
        else {
            ++this.registrationCount;
        }
        this.addToBuckets(registration);
    }

    public void remove(BlockPos source, int id) {
        var registrations = this.sources.get(source);
        if (registrations == null) {
            return;
        }
        var removed = registrations.remove(id);
        if (removed == null) {
            return;
        }
        this.removeFromBuckets(removed);
        --this.registrationCount;
        if (registrations.isEmpty()) {
            this.sources.remove(source);
        }
    }

    public void removeSource(BlockPos source) {
        var removed = this.sources.remove(source);
        if (removed == null) {
            return;
        }
        for (var registration : removed.values()) {
            this.removeFromBuckets(registration);
        }
        this.registrationCount -= removed.size();
    }

    public void clear() {
        this.sources.clear();
        for (int shift = MIN_SHIFT; shift <= MAX_SHIFT; ++shift) {
            this.tiers[shift].clear();
        }
        this.occupiedTiers = 0;
        this.registrationCount = 0;
        this.bucketCount = 0;
        this.bucketReferenceCount = 0;
        this.queryCount = 0;
        this.candidateTestCount = 0;
        this.lastQueryCandidateCount = 0;
    }

    public int count(int x, int y, int z) {
        return this.query(x, y, z, false);
    }

    public boolean contains(int x, int y, int z) {
        return this.query(x, y, z, true) != 0;
    }

    private int query(int x, int y, int z, boolean stopAtFirst) {
        ++this.queryCount;
        this.lastQueryCandidateCount = 0;
        int count = 0;
        long remainingTiers = this.occupiedTiers;
        while (remainingTiers != 0) {
            int shift = Long.numberOfTrailingZeros(remainingTiers);
            remainingTiers &= remainingTiers - 1;
            var bucket = this.tiers[shift].get(cellKey((long) x >> shift, (long) z >> shift));
            if (bucket == null) {
                continue;
            }
            for (int i = 0; i < bucket.size(); ++i) {
                ++this.lastQueryCandidateCount;
                ++this.candidateTestCount;
                if (bucket.get(i).area.isPointInside(x, y, z)) {
                    ++count;
                    if (stopAtFirst) {
                        return count;
                    }
                }
            }
        }
        return count;
    }

    /** Captures only intersecting immutable areas; no registry state is retained by the plan. */
    public TriggerActivationPlan planActivation(AC_TriggerArea area) {
        var coverage = new ArrayList<AC_TriggerArea>();
        if (!TriggerAreaTraversal.isEmpty(area)) {
            for (var registrations : this.sources.values()) {
                for (var registration : registrations.values()) {
                    if (TriggerAreaTraversal.intersects(area, registration.area)) {
                        coverage.add(registration.area);
                    }
                }
            }
        }
        return new TriggerActivationPlan(area, coverage.toArray(AC_TriggerArea[]::new));
    }

    /** Counters are updated without allocating on the query path. */
    public Statistics statistics() {
        return new Statistics(this.registrationCount, this.bucketCount, this.bucketReferenceCount,
            this.queryCount, this.candidateTestCount, this.lastQueryCandidateCount);
    }

    private void addToBuckets(Registration registration) {
        if (registration.shift < 0) {
            return;
        }
        var tier = this.tiers[registration.shift];
        for (long x = registration.minCellX; x <= registration.maxCellX; ++x) {
            for (long z = registration.minCellZ; z <= registration.maxCellZ; ++z) {
                long key = cellKey(x, z);
                var bucket = tier.get(key);
                if (bucket == null) {
                    bucket = new ArrayList<>();
                    tier.put(key, bucket);
                    ++this.bucketCount;
                }
                bucket.add(registration);
                ++this.bucketReferenceCount;
            }
        }
        this.occupiedTiers |= 1L << registration.shift;
    }

    private void removeFromBuckets(Registration registration) {
        if (registration.shift < 0) {
            return;
        }
        var tier = this.tiers[registration.shift];
        for (long x = registration.minCellX; x <= registration.maxCellX; ++x) {
            for (long z = registration.minCellZ; z <= registration.maxCellZ; ++z) {
                long key = cellKey(x, z);
                var bucket = tier.get(key);
                bucket.remove(registration);
                --this.bucketReferenceCount;
                if (bucket.isEmpty()) {
                    tier.remove(key);
                    --this.bucketCount;
                }
            }
        }
        if (tier.isEmpty()) {
            this.occupiedTiers &= ~(1L << registration.shift);
        }
    }

    private static long cellKey(long x, long z) {
        return (x << 32) | (z & 0xffff_ffffL);
    }

    public record Statistics(int registrationCount, int bucketCount, int bucketReferenceCount,
                             long queryCount, long candidateTestCount, int lastQueryCandidateCount) {
    }

    private static final class Registration {
        final AC_TriggerArea area;
        final int shift;
        final int minCellX;
        final int minCellZ;
        final int maxCellX;
        final int maxCellZ;

        Registration(AC_TriggerArea area) {
            this.area = area;
            if (TriggerAreaTraversal.isEmpty(area)) {
                this.shift = -1;
                this.minCellX = this.minCellZ = this.maxCellX = this.maxCellZ = 0;
                return;
            }
            long width = Math.max((long) area.max.x() - area.min.x() + 1,
                (long) area.max.z() - area.min.z() + 1);
            this.shift = Math.max(MIN_SHIFT, Long.SIZE - Long.numberOfLeadingZeros(width - 1));
            this.minCellX = (int) ((long) area.min.x() >> this.shift);
            this.minCellZ = (int) ((long) area.min.z() >> this.shift);
            this.maxCellX = (int) ((long) area.max.x() >> this.shift);
            this.maxCellZ = (int) ((long) area.max.z() >> this.shift);
        }
    }
}
