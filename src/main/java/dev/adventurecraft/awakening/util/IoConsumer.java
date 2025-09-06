package dev.adventurecraft.awakening.util;

import java.io.IOException;

@FunctionalInterface
public interface IoConsumer<T> {
    void accept(T value)
        throws IOException;
}

