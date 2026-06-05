package dev.adventurecraft.awakening.commands;

import com.mojang.brigadier.Message;
import dev.adventurecraft.awakening.dom.Node;
import dev.adventurecraft.awakening.dom.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record MessageNode(Node node) implements Node, Message {

    public @Override <T> Optional<T> visit(NodeConsumer<T> consumer) {
        return this.node.visit(consumer);
    }

    public @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style visitStyle) {
        return this.node.visit(consumer, visitStyle);
    }

    public @Override String getString() {
        return Node.super.getString();
    }

    public @Override @NotNull String toString() {
        return "message{" + this.node + "}";
    }
}
