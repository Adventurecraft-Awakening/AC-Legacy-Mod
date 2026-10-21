package dev.adventurecraft.awakening.world.trigger;

import dev.adventurecraft.awakening.common.AC_TriggerArea;

/** Inclusive trigger geometry, including the ends of the integer coordinate range. */
public final class TriggerAreaTraversal {

    private TriggerAreaTraversal() {
    }

    @FunctionalInterface
    public interface BlockConsumer {
        void accept(int x, int y, int z);
    }

    public static void forEach(AC_TriggerArea area, BlockConsumer consumer) {
        if (isEmpty(area)) {
            return;
        }
        for (long x = area.min.x(); x <= area.max.x(); ++x) {
            for (long y = area.min.y(); y <= area.max.y(); ++y) {
                for (long z = area.min.z(); z <= area.max.z(); ++z) {
                    consumer.accept((int) x, (int) y, (int) z);
                }
            }
        }
    }

    static boolean isEmpty(AC_TriggerArea area) {
        return area.min.x() > area.max.x() || area.min.y() > area.max.y() || area.min.z() > area.max.z();
    }

    static boolean intersects(AC_TriggerArea first, AC_TriggerArea second) {
        return !isEmpty(first) && !isEmpty(second) &&
            first.min.x() <= second.max.x() && first.max.x() >= second.min.x() &&
            first.min.y() <= second.max.y() && first.max.y() >= second.min.y() &&
            first.min.z() <= second.max.z() && first.max.z() >= second.min.z();
    }
}
