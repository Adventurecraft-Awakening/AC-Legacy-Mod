package dev.adventurecraft.awakening.world.trigger;

import dev.adventurecraft.awakening.common.AC_TriggerArea;

import java.util.Arrays;
import java.util.Comparator;

/**
 * The coverage decision made before adding an area. The immutable area snapshot remains valid
 * even when activation scripts change the registry while this plan is being traversed.
 */
public final class TriggerActivationPlan {

    private final AC_TriggerArea area;
    private final AC_TriggerArea[] coverage;
    private final boolean empty;

    TriggerActivationPlan(AC_TriggerArea area, AC_TriggerArea[] coverage) {
        this.area = area;
        this.coverage = coverage;
        boolean fullyCovered = TriggerAreaTraversal.isEmpty(area);
        for (var covered : coverage) {
            if (covered.isPointInside(area.min) && covered.isPointInside(area.max)) {
                fullyCovered = true;
                break;
            }
        }
        this.empty = fullyCovered;
        if (!fullyCovered) {
            Arrays.sort(this.coverage, Comparator.comparingInt(value -> value.min.z()));
        }
    }

    /** Emits uncovered coordinates in the original X, Y, Z order without per-block objects. */
    public void forEach(TriggerAreaTraversal.BlockConsumer consumer) {
        if (this.empty) {
            return;
        }
        for (long x = this.area.min.x(); x <= this.area.max.x(); ++x) {
            for (long y = this.area.min.y(); y <= this.area.max.y(); ++y) {
                long nextZ = this.area.min.z();
                for (AC_TriggerArea covered : this.coverage) {
                    if (x < covered.min.x() || x > covered.max.x() ||
                        y < covered.min.y() || y > covered.max.y() || covered.max.z() < nextZ) {
                        continue;
                    }
                    long coveredMin = Math.max((long) covered.min.z(), this.area.min.z());
                    long uncoveredEnd = Math.min(coveredMin - 1, this.area.max.z());
                    for (long z = nextZ; z <= uncoveredEnd; ++z) {
                        consumer.accept((int) x, (int) y, (int) z);
                    }
                    nextZ = Math.max(nextZ, (long) covered.max.z() + 1);
                    if (nextZ > this.area.max.z()) {
                        break;
                    }
                }
                for (long z = nextZ; z <= this.area.max.z(); ++z) {
                    consumer.accept((int) x, (int) y, (int) z);
                }
            }
        }
    }
}
