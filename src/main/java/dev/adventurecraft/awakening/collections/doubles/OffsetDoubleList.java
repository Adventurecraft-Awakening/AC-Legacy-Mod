package dev.adventurecraft.awakening.collections.doubles;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class OffsetDoubleList extends AbstractDoubleList {

    private final DoubleList inner;
    private final double offset;

    public OffsetDoubleList(DoubleList inner, double offset) {
        this.inner = inner;
        this.offset = offset;
    }

    public @Override double getDouble(int index) {
        return this.inner.getDouble(index) + this.offset;
    }

    public @Override int size() {
        return this.inner.size();
    }
}

