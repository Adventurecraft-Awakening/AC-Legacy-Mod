package dev.adventurecraft.awakening.dom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface TextNode extends Node {

    TextNode EMPTY = new TextNode() {
        public @Override String text() {
            return "";
        }

        public @Override String toString() {
            return "empty";
        }
    };

    String text();

    static TextNode of(String value) {
        return value.isEmpty() ? EMPTY : new Text(value);
    }

    static TextNode of(String value, Style style) {
        return value.isEmpty() ? EMPTY : new StyledText(value, style);
    }

    static @Nullable TextNode ofOrNull(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return new Text(value);
    }

    record Text(String text) implements TextNode {

        public @Override <T> Optional<T> visit(NodeConsumer<T> consumer) {
            return consumer.accept(this.text);
        }

        public @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style style) {
            return consumer.accept(this.text, style);
        }

        public @Override @NotNull String toString() {
            return "text{" + this.text + "}";
        }
    }

    record StyledText(String text, Style style) implements TextNode, StyleSource {

        public @Override <T> Optional<T> visit(NodeConsumer<T> consumer) {
            return consumer.accept(this.text);
        }

        public @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style visitStyle) {
            return consumer.accept(this.text, this.style.applyTo(visitStyle));
        }

        public @Override @NotNull String toString() {
            return "text{" + this.text + "}";
        }
    }
}
