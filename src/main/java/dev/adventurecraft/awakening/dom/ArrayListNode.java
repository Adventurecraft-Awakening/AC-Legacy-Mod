package dev.adventurecraft.awakening.dom;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayListNode extends ArrayList<Node> implements ListNode {

    public ArrayListNode() {
    }

    public ArrayListNode(@NotNull Collection<? extends Node> c) {
        super(c);
    }

    public @Override @NotNull String toString() {
        return "list" + super.toString();
    }
}
