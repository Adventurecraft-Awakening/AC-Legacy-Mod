package dev.adventurecraft.awakening.collections;

import dev.adventurecraft.awakening.collections.booleans.AbstractBooleanSet;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import org.jetbrains.annotations.NotNull;

public final class BoolPackSet extends AbstractBooleanSet {

    private static final byte S_NONE = 0;
    private static final byte S_FALSE = 1;
    private static final byte S_TRUE = 1 << 1;
    private static final byte S_BOTH = S_FALSE | S_TRUE;

    private byte state = S_NONE;

    public BoolPackSet() {
    }

    public BoolPackSet(boolean[] values) {
        for (boolean v : values) {
            this.add(v);
        }
    }

    public @Override boolean add(boolean k) {
        byte old = this.state;
        this.state |= (k ? S_TRUE : S_FALSE);
        return this.state != old;
    }

    public @Override boolean contains(boolean k) {
        return (this.state & (k ? S_TRUE : S_FALSE)) != 0;
    }

    @Override
    public boolean remove(boolean k) {
        byte old = this.state;
        this.state &= (byte) ~(k ? S_TRUE : S_FALSE);
        return this.state != old;
    }

    public @Override @NotNull BooleanIterator iterator() {
        return new Iter();
    }

    public @Override int size() {
        return ((this.state & S_BOTH) + 1) >>> 1;
    }

    final class Iter implements BooleanIterator {
        private byte bits = state;
        private byte mask;

        public @Override boolean hasNext() {
            return this.bits != 0;
        }

        public @Override boolean nextBoolean() {
            // 11, 10 => false, true, _
            // 01, 00 => false, _
            // 10, 00 => true, _
            // 00, 00 => _
            boolean val = this.bits == S_TRUE;
            this.mask = (byte) Integer.lowestOneBit(this.bits);
            this.bits &= (byte) ~this.mask;
            return val;
        }

        public @Override void remove() {
            // TODO: throw on mask=0?
            state &= (byte) ~this.mask;
        }
    }
}
