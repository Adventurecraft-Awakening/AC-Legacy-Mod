package dev.adventurecraft.awakening.chat;

import com.mojang.brigadier.Message;
import dev.adventurecraft.awakening.dom.Node;
import dev.adventurecraft.awakening.dom.NumberNode;
import dev.adventurecraft.awakening.dom.Style;
import dev.adventurecraft.awakening.dom.TextNode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Component extends Node, Message {

    Node contents();

    List<Component> siblings();

    Style style();

    default MutComponent copy() {
        List<Component> siblings = this.siblings();
        return new MutComponent(this.contents(), siblings.isEmpty() ? null : new ArrayList<>(siblings), this.style());
    }

    default @Override String getString() {
        return Node.super.getString();
    }

    default <T> Optional<T> visit(NodeConsumer<T> consumer) {
        Optional<T> o1 = this.contents().visit(consumer);
        if (o1.isPresent()) {
            return o1;
        }
        for (Component sibling : this.siblings()) {
            Optional<T> o2 = sibling.visit(consumer);
            if (o2.isPresent()) {
                return o2;
            }
        }
        return Optional.empty();
    }

    default <T> Optional<T> visit(StyledConsumer<T> consumer, @Nullable Style style) {
        Style appliedStyle = this.style().applyTo(style);
        Optional<T> o1 = this.contents().visit(consumer, appliedStyle);
        if (o1.isPresent()) {
            return o1;
        }
        for (Component sibling : this.siblings()) {
            Optional<T> o2 = sibling.visit(consumer, appliedStyle);
            if (o2.isPresent()) {
                return o2;
            }
        }
        return Optional.empty();
    }

    static MutComponent empty() {
        return of(TextNode.EMPTY);
    }

    static MutComponent of(Node content) {
        return new MutComponent(content, new ArrayList<>(), Style.EMPTY);
    }

    static MutComponent number(Number value) {
        return of(NumberNode.ofUnbox(value));
    }

    static MutComponent literal(String value) {
        return of(TextNode.of(value));
    }

    static MutComponent literal(char value) {
        return of(TextNode.of(String.valueOf(value)));
    }

    static MutComponent repeat(Node content, int amount) {
        return of(Node.repeat(content, amount));
    }
}
