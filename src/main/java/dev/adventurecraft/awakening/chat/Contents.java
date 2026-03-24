package dev.adventurecraft.awakening.chat;

import java.util.Optional;

public interface Contents {

    default <T> Optional<T> visit(StyledText.Consumer<T> consumer) {
        return Optional.empty();
    }

    default <T> Optional<T> visit(StyledText.StyledConsumer<T> consumer, Style style) {
        return Optional.empty();
    }
}
