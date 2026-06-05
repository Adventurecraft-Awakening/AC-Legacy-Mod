package dev.adventurecraft.awakening.dom;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record StyleNode(Node node, Style style) implements Node, StyleSource {

    public @Override <T> Optional<T> visit(NodeConsumer<T> consumer) {
        return this.node.visit(consumer);
    }

    public @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style visitStyle) {
        return this.node.visit(consumer, this.style.applyTo(visitStyle));
    }

    public @Override @NotNull String toString() {
        return "style{" + this.node + "}";
    }
}
