package dev.adventurecraft.awakening.chat;

import dev.adventurecraft.awakening.primitives.Unit;

import java.util.Optional;

public interface StyledText {

    Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);

    StyledText EMPTY = new StyledText() {
        public @Override <T> Optional<T> visit(Consumer<T> consumer) {
            return Optional.empty();
        }

        public @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style style) {
            return Optional.empty();
        }
    };

    <T> Optional<T> visit(Consumer<T> consumer);

    <T> Optional<T> visit(StyledConsumer<T> consumer, Style style);

    default String getString() {
        var builder = new StringBuilder();
        this.visit(content -> {
            builder.append(content);
            return Optional.empty();
        });
        return builder.toString();
    }

    static StyledText of(String value) {
        return new StyledText() {
            public @Override <T> Optional<T> visit(Consumer<T> consumer) {
                return consumer.accept(value);
            }

            public @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style style) {
                return consumer.accept(value, style);
            }
        };
    }

    static StyledText of(String value, Style style) {
        return new StyledText() {
            public @Override <T> Optional<T> visit(Consumer<T> consumer) {
                return consumer.accept(value);
            }

            public @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style visitStyle) {
                return consumer.accept(value, style.applyTo(visitStyle));
            }
        };
    }

    interface Consumer<T> {
        Optional<T> accept(String content);
    }

    interface StyledConsumer<T> {
        Optional<T> accept(String content, Style style);
    }
}
