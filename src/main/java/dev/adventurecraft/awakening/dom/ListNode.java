package dev.adventurecraft.awakening.dom;

import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.stream.Stream;

public interface ListNode extends Node, List<Node> {

    ListNode EMPTY = new List0();

    default ListNode append(@Nullable Node node) {
        if (node != null) {
            this.add(node);
        }
        return this;
    }

    default @Override <T> Optional<T> visit(NodeConsumer<T> consumer) {
        for (int i = 0; i < this.size(); i++) {
            Optional<T> o = this.get(i).visit(consumer);
            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    default @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style style) {
        for (int i = 0; i < this.size(); i++) {
            Optional<T> o = this.get(i).visit(consumer, style);
            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    static ListNode of() {
        return EMPTY;
    }

    static ListNode of(Node e0) {
        return new List1(e0);
    }

    static ListNode of(Node e0, Node e1) {
        return new List2(e0, e1);
    }

    static ListNode of(Node... items) {
        return switch (items.length) {
            case 0 -> of();
            case 1 -> of(items[0]);
            case 2 -> of(items[0], items[1]);
            default -> new ListN(items.clone());
        };
    }

    abstract class ImmutableList extends AbstractObjectList<Node> implements ListNode, RandomAccess {
        public @Override String toString() {
            return "list" + super.toString();
        }
    }

    final class List0 extends ImmutableList {
        private List0() {
        }

        public @Override Node get(int i) {
            throw new IndexOutOfBoundsException();
        }

        public @Override int size() {
            return 0;
        }

        public @Override @NotNull Stream<Node> stream() {
            return Stream.empty();
        }

        @SuppressWarnings("unchecked")
        public @Override @NotNull ObjectListIterator<Node> listIterator(int index) {
            return ObjectIterators.EMPTY_ITERATOR;
        }
    }

    final class List1 extends ImmutableList {
        private final Node e0;

        private List1(Node e0) {
            this.e0 = e0;
        }

        public @Override int size() {
            return 1;
        }

        public @Override Node get(int i) {
            if (i == 0) {
                return this.e0;
            }
            throw new IndexOutOfBoundsException();
        }

        public @Override @NotNull Stream<Node> stream() {
            return Stream.of(this.e0);
        }

        public @Override @NotNull Stream<Node> parallelStream() {
            return this.stream().parallel();
        }

        public @Override @NotNull ObjectListIterator<Node> listIterator(int index) {
            if (index == 0) {
                return ObjectIterators.singleton(this.e0);
            }
            throw new IndexOutOfBoundsException();
        }
    }

    final class List2 extends ImmutableList {
        private final Node e0;
        private final Node e1;

        private List2(Node e0, Node e1) {
            this.e0 = e0;
            this.e1 = e1;
        }

        public @Override int size() {
            return 2;
        }

        public @Override Node get(int i) {
            return switch (i) {
                case 0 -> this.e0;
                case 1 -> this.e1;
                default -> throw new IndexOutOfBoundsException();
            };
        }
    }

    final class ListN extends ImmutableList {
        private final Node[] array;

        private ListN(Node[] array) {
            this.array = array;
        }

        public @Override int size() {
            return this.array.length;
        }

        public @Override Node get(int i) {
            return this.array[i];
        }

        public @Override @NotNull ObjectListIterator<Node> listIterator() {
            return ObjectIterators.wrap(this.array);
        }

        public @Override @NotNull ObjectListIterator<Node> listIterator(int index) {
            return ObjectIterators.wrap(this.array, index, this.array.length - index);
        }

        public @Override @NotNull Stream<Node> stream() {
            return Arrays.stream(this.array);
        }

        public @Override @NotNull Stream<Node> parallelStream() {
            return this.stream().parallel();
        }
    }
}
