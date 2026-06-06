package dev.adventurecraft.awakening.collections.booleans;

import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractBooleanSet extends AbstractBooleanCollection implements BooleanSet {

    public abstract @Override @NotNull BooleanIterator iterator();

    public @Override boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Set<?> s)) {
            return false;
        }
        if (s.size() != this.size()) {
            return false;
        }
        if (s instanceof BooleanSet os) {
            return this.containsAll(os);
        }
        return this.containsAll(s);
    }

    public @Override int hashCode() {
        int h = 0;
        BooleanIterator i = iterator();
        while (i.hasNext()) {
            h += i.nextBoolean() ? 1231 : 1237;
        }
        return h;
    }
}
