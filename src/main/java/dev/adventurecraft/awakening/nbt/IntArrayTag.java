package dev.adventurecraft.awakening.nbt;

import dev.adventurecraft.awakening.extension.nbt.ExTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.Tags;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;

public class IntArrayTag extends Tag implements ExTag {

    public int[] data;

    public IntArrayTag() {
    }

    public IntArrayTag(int[] data) {
        this.data = data;
    }

    public @Override void accept(TagVisitor visitor) {
        visitor.visit(this);
    }

    public @Override IntArrayTag copy() {
        return new IntArrayTag(this.data.clone());
    }

    protected @Override void write(DataOutput output) {
        try {
            int[] a = this.data;
            output.writeByte(Tags.TAG_INT); // TODO: remove list header needed by compat
            output.writeInt(a.length);
            for (int i = 0; i < a.length; i++) {
                output.writeInt(a[i]);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected @Override void read(DataInput input) {
        try {
            int n = input.readInt();
            int[] a = new int[n];
            for (int j = 0; j < a.length; j++) {
                a[j] = input.readInt();
            }
            this.data = a;
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public @Override byte getId() {
        // TODO: this is backwards compatible with old NBT; upgrade in future
        return Tags.TAG_LIST;
    }

    public @Override String toString() {
        return "[" + this.data.length + " ints]";
    }
}
