package dev.adventurecraft.awakening.collections.doubles;

import it.unimi.dsi.fastutil.doubles.*;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.RandomAccess;

public final class DoubleListX {

    public static DoubleList of() {
        return List0.INSTANCE;
    }

    public static DoubleList of(double e0) {
        return new List1(e0);
    }

    public static DoubleList of(double e0, double e1) {
        return new List2(e0, e1);
    }

    public static DoubleList of(double e0, double e1, double e2) {
        return new List3(e0, e1, e2);
    }

    public static DoubleList of(double e0, double e1, double e2, double e3) {
        return new List4(e0, e1, e2, e3);
    }

    public static DoubleList of(double... values) {
        return switch (values.length) {
            case 0 -> of();
            case 1 -> of(values[0]);
            case 2 -> of(values[0], values[1]);
            case 3 -> of(values[0], values[1], values[2]);
            case 4 -> of(values[0], values[1], values[2], values[3]);
            default -> new ListArray(values.clone());
        };
    }

    private static abstract class ListN extends AbstractDoubleList implements Cloneable, RandomAccess {

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        public @Override ListN clone() {
            return this;
        }

        //region UnsupportedOperation

        public @Override void addElements(int index, double[] a, int offset, int length) {
            throw new UnsupportedOperationException();
        }

        public @Override void removeElements(int from, int to) {
            throw new UnsupportedOperationException();
        }

        public @Override void setElements(int index, double[] a, int offset, int length) {
            throw new UnsupportedOperationException();
        }

        public @Override void sort(DoubleComparator comparator) {
            throw new UnsupportedOperationException();
        }

        public @Override void unstableSort(DoubleComparator comparator) {
            throw new UnsupportedOperationException();
        }

        public @Override void sort(Comparator<? super Double> comparator) {
            throw new UnsupportedOperationException();
        }

        public @Override void unstableSort(Comparator<? super Double> comparator) {
            throw new UnsupportedOperationException();
        }

        //endregion

        public boolean equals(DoubleList other) {
            int s = other.size();
            if (s != this.size()) {
                return false;
            }
            for (int i = 0; i < s; i++) {
                if (this.getDouble(i) != other.getDouble(i)) {
                    return false;
                }
            }
            return true;
        }

        public @Override boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof DoubleList other) {
                return this.equals(other);
            }
            return super.equals(o);
        }

        public @Override int hashCode() {
            int h = 1;
            int s = this.size();
            for (int i = 0; i < s; i++) {
                h = 31 * h + it.unimi.dsi.fastutil.HashCommon.double2int(this.getDouble(i));
            }
            return h;
        }

        static IndexOutOfBoundsException makeIoob(int index) {
            throw new IndexOutOfBoundsException(index);
        }
    }

    private static final class List0 extends ListN {

        private static final List0 INSTANCE = new List0();

        private List0() {
        }

        public @Override double getDouble(int index) {
            throw makeIoob(index);
        }

        public @Override int size() {
            return 0;
        }

        public @Override double[] toDoubleArray() {
            return DoubleArrays.EMPTY_ARRAY;
        }

        public @Override @NotNull DoubleListIterator listIterator() {
            return DoubleIterators.EMPTY_ITERATOR;
        }

        public @Override @NotNull DoubleSpliterator spliterator() {
            return DoubleSpliterators.EMPTY_SPLITERATOR;
        }

        public @Override @NotNull DoubleListIterator listIterator(int i) {
            if (i != 0) {
                throw new IndexOutOfBoundsException();
            }
            return this.listIterator();
        }

        public @Override @NotNull DoubleList subList(int from, int to) {
            this.ensureIndex(from);
            this.ensureIndex(to);
            if (from > to) {
                throw new IndexOutOfBoundsException(
                    "Start index (" + from + ") is greater than end index (" + to + ")");
            }
            return this;
        }

        //region Sort

        // Lists of size 0 are always sorted
        public @Override void sort(DoubleComparator comparator) {
        }

        public @Override void unstableSort(DoubleComparator comparator) {
        }

        @Deprecated
        public @Override void sort(Comparator<? super Double> comparator) {
        }

        @Deprecated
        public @Override void unstableSort(Comparator<? super Double> comparator) {
        }

        //endregion
    }

    private static final class List1 extends ListN {
        private final double e0;

        List1(double e0) {
            this.e0 = e0;
        }

        public @Override double getDouble(int index) {
            if (index == 0) {
                return this.e0;
            }
            throw makeIoob(index);
        }

        public @Override int size() {
            return 1;
        }

        public @Override double[] toDoubleArray() {
            return new double[] {this.e0};
        }

        public @Override @NotNull DoubleListIterator listIterator() {
            return DoubleIterators.singleton(this.e0);
        }

        public @Override @NotNull DoubleSpliterator spliterator() {
            return DoubleSpliterators.singleton(this.e0);
        }

        public @Override @NotNull DoubleListIterator listIterator(int index) {
            if (index > 1 || index < 0) {
                throw new IndexOutOfBoundsException();
            }
            DoubleListIterator l = this.listIterator();
            if (index == 1) {
                l.nextDouble();
            }
            return l;
        }

        public @Override @NotNull DoubleList subList(int from, int to) {
            this.ensureIndex(from);
            this.ensureIndex(to);
            if (from > to) {
                throw new IndexOutOfBoundsException(
                    "Start index (" + from + ") is greater than end index (" + to + ")");
            }
            if (from != 0 || to != 1) {
                return of();
            }
            return this;
        }

        //region Sort

        // Lists of size 1 are always sorted
        public @Override void sort(DoubleComparator comparator) {
        }

        public @Override void unstableSort(DoubleComparator comparator) {
        }

        @Deprecated
        public @Override void sort(Comparator<? super Double> comparator) {
        }

        @Deprecated
        public @Override void unstableSort(Comparator<? super Double> comparator) {
        }

        //endregion
    }

    private static final class List2 extends ListN {
        private final double e0, e1;

        List2(double e0, double e1) {
            this.e0 = e0;
            this.e1 = e1;
        }

        public @Override double getDouble(int index) {
            return switch (index) {
                case 0 -> this.e0;
                case 1 -> this.e1;
                default -> throw makeIoob(index);
            };
        }

        public @Override int size() {
            return 2;
        }
    }

    private static final class List3 extends ListN {
        private final double e0, e1, e2;

        List3(double e0, double e1, double e2) {
            this.e0 = e0;
            this.e1 = e1;
            this.e2 = e2;
        }

        public @Override double getDouble(int index) {
            return switch (index) {
                case 0 -> this.e0;
                case 1 -> this.e1;
                case 2 -> this.e2;
                default -> throw makeIoob(index);
            };
        }

        public @Override int size() {
            return 3;
        }
    }

    private static final class List4 extends ListN {
        private final double e0, e1, e2, e3;

        List4(double e0, double e1, double e2, double e3) {
            this.e0 = e0;
            this.e1 = e1;
            this.e2 = e2;
            this.e3 = e3;
        }

        public @Override double getDouble(int index) {
            return switch (index) {
                case 0 -> this.e0;
                case 1 -> this.e1;
                case 2 -> this.e2;
                case 3 -> this.e3;
                default -> throw makeIoob(index);
            };
        }

        public @Override int size() {
            return 4;
        }
    }

    private static final class ListArray extends ListN {
        private final double[] a;

        ListArray(double[] a) {
            this.a = a;
        }

        public @Override double getDouble(int index) {
            return this.a[index];
        }

        public @Override int size() {
            return this.a.length;
        }

        public @Override void getElements(int from, double[] a, int offset, int length) {
            DoubleArrays.ensureOffsetLength(a, offset, length);
            System.arraycopy(this.a, from, a, offset, length);
        }
    }
}
