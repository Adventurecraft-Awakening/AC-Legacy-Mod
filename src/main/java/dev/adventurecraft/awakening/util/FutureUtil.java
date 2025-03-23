package dev.adventurecraft.awakening.util;

import java.util.concurrent.*;

public final class FutureUtil {

    public static <V> V getOrElse(Future<V> future, V other) {
        if (future.isDone()) {
            try {
                return future.get(0, TimeUnit.NANOSECONDS);
            } catch (InterruptedException | ExecutionException | CancellationException | TimeoutException ignored) {
            }
        }
        return other;
    }
}
