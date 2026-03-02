package dev.adventurecraft.awakening.nbt;

import dev.adventurecraft.awakening.extension.nbt.ExTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.Tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;

public class LongArrayTag extends Tag implements ExTag {
    public long[] data;

    public LongArrayTag() {
    }

    public LongArrayTag(long[] data) {
        this.data = data;
    }

    @Override
    public LongArrayTag copy() {
        return new LongArrayTag(this.data.clone());
    }

    public @Override void accept(TagVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void write(DataOutput output) {
        try {
            long[] a = this.data;
            output.writeByte(Tags.TAG_LONG); // TODO: remove list header needed by compat
            output.writeInt(a.length);
            for (int i = 0; i < a.length; i++) {
                output.writeLong(a[i]);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    protected void read(DataInput input) {
        try {
            int n = input.readInt();
            long[] a = new long[n];
            for (int j = 0; j < a.length; j++) {
                a[j] = input.readLong();
            }
            this.data = a;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public byte getId() {
        // TODO: this is backwards compatible with old NBT; upgrade in future
        return Tags.TAG_LIST;
    }

    public String toString() {
        return "[" + this.data.length + " longs]";
    }
}
