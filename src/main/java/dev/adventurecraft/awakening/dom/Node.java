package dev.adventurecraft.awakening.dom;

import dev.adventurecraft.awakening.primitives.Unit;

import java.util.Optional;

public interface Node {

    Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);

    Node EMPTY = new Node() {
        public @Override String toString() {
            return "empty";
        }
    };

    default <T> Optional<T> visit(NodeConsumer<T> consumer) {
        return Optional.empty();
    }

    default <T> Optional<T> visit(StyledConsumer<T> consumer, Style style) {
        return Optional.empty();
    }

    default Node withStyle(Style style) {
        if (style == Style.EMPTY) {
            return this;
        }
        return new StyleNode(this, style);
    }

    default String getString() {
        var builder = new StringBuilder();
        this.visit(content -> {
            builder.append(content);
            return Optional.empty();
        });
        return builder.toString();
    }

    default String getString(int limit) {
        var builder = new StringBuilder();
        this.visit(content -> {
            int rem = limit - builder.length();
            if (rem <= 0) {
                return STOP_ITERATION;
            }
            if (content.length() <= rem) {
                builder.append(content);
            }
            else {
                builder.append(content, 0, rem);
            }
            return Optional.empty();
        });
        return builder.toString();
    }

    static TextNode text(String value) {
        return TextNode.of(value);
    }

    static TextNode text(char value) {
        return text(String.valueOf(value));
    }

    static TextNode text(String value, Style style) {
        return TextNode.of(value, style);
    }
    static TextNode text(char value, Style style) {
        return text(String.valueOf(value), style);
    }

    static Node repeat(Node content, int amount) {
        return switch (amount) {
            case 0 -> EMPTY;
            case 1 -> content;
            default -> new RepeatNode(content, amount);
        };
    }

    interface NodeConsumer<T> {
        Optional<T> accept(String content);
    }

    interface StyledConsumer<T> {
        Optional<T> accept(String content, Style style);
    }
}
