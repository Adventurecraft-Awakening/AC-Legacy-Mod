package dev.adventurecraft.awakening.dom;

import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.RandomAccess;

public final class RepeatNode extends AbstractObjectList<Node> implements ListNode, RandomAccess {

    private final Node content;
    private final int size;

    public RepeatNode(Node content, int size) {
        this.content = content;
        this.size = size;
    }

    public Node content() {
        return this.content;
    }

    public @Override int size() {
        return this.size;
    }

    public @Override Node get(int i) {
        Objects.checkIndex(i, this.size);
        return this.content;
    }

    public @Override @NotNull String toString() {
        return "repeat*" + this.size + "{" + this.content + "}";
    }

    public @Override boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof RepeatNode other) {
            return Objects.equals(this.content, other.content) && this.size == other.size;
        }
        return false;
    }

    public @Override int hashCode() {
        return Objects.hash(this.content, this.size);
    }
}
