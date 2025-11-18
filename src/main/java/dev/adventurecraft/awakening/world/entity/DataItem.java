package dev.adventurecraft.awakening.world.entity;

import dev.adventurecraft.awakening.util.HashCode;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ItemInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public abstract class DataItem implements Int2ObjectMap<DataItem>, Int2ObjectMap.Entry<DataItem> {

    private final byte id;
    private boolean dirty;

    public DataItem(int id) {
        this.id = (byte) id;
        this.dirty = true;
    }

    public abstract Object getData();

    public abstract void setData(Object value);

    public final boolean isDirty() {
        return this.dirty;
    }

    public final void setDirty(boolean value) {
        this.dirty = value;
    }

    @Override
    public final int getIntKey() {
        return this.id;
    }

    @Override
    public final DataItem getValue() {
        return this;
    }

    @Override
    public final DataItem setValue(DataItem dataItem) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int size() {
        return 1;
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public final boolean containsValue(Object o) {
        return o == this.getValue();
    }

    @Override
    public final void putAll(@NotNull Map<? extends Integer, ? extends DataItem> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void defaultReturnValue(DataItem rv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DataItem defaultReturnValue() {
        return null;
    }

    @Override
    public final ObjectSet<Int2ObjectMap.Entry<DataItem>> int2ObjectEntrySet() {
        return ObjectSet.of(this);
    }

    @Override
    public final @NotNull IntSet keySet() {
        return IntSet.of(this.getIntKey());
    }

    @Override
    public final @NotNull ObjectCollection<DataItem> values() {
        return ObjectSet.of(this.getValue());
    }

    @Override
    public final DataItem get(int key) {
        if (key == this.getIntKey()) {
            return this.getValue();
        }
        return this.defaultReturnValue();
    }

    @Override
    public final boolean containsKey(int key) {
        return key == this.getIntKey();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataItem item) {
            return this.id == item.id && this.getData() == item.getData();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(this.id | ((this.dirty ? 1 : 0) << 8), this.getData().hashCode());
    }

    @Override
    public String toString() {
        return "DataItem{" + "id=" + this.id + ", dirty=" + this.dirty + ", data=" + this.getData() + '}';
    }

    public static final class Byte extends DataItem {
        private byte data;

        public Byte(int id, byte data) {
            super(id);
            this.data = data;
        }

        @Override
        public java.lang.Byte getData() {
            return this.data;
        }

        public void setData(byte value) {
            this.setDirty(this.data != value);
            this.data = value;
        }

        @Override
        public void setData(Object value) {
            this.setData((byte) value);
        }
    }

    public static final class Short extends DataItem {
        private short data;

        public Short(int id, short data) {
            super(id);
            this.data = data;
        }

        @Override
        public java.lang.Short getData() {
            return this.data;
        }

        public void setData(short value) {
            this.setDirty(this.data != value);
            this.data = value;
        }

        @Override
        public void setData(Object value) {
            this.setData((short) value);
        }
    }

    public static final class Int extends DataItem {
        private int data;

        public Int(int id, int data) {
            super(id);
            this.data = data;
        }

        @Override
        public Integer getData() {
            return this.data;
        }

        public void setData(int value) {
            this.setDirty(this.data != value);
            this.data = value;
        }

        @Override
        public void setData(Object value) {
            this.setData((int) value);
        }
    }

    public static final class Float extends DataItem {
        private float data;

        public Float(int id, float data) {
            super(id);
            this.data = data;
        }

        @Override
        public java.lang.Float getData() {
            return this.data;
        }

        public void setData(float value) {
            this.setDirty(this.data != value);
            this.data = value;
        }

        @Override
        public void setData(Object value) {
            this.setData((float) value);
        }
    }

    public static abstract class Obj<T> extends DataItem {
        private T data;

        public Obj(int id, T data) {
            super(id);
            this.data = data;
        }

        @Override
        public final T getData() {
            return this.data;
        }

        public final void setData(Object value, Class<T> type) {
            T other = type.cast(value);
            this.setDirty(!Objects.equals(this.data, other));
            this.data = other;
        }
    }

    public static final class JString extends Obj<String> {
        public JString(int id, String data) {
            super(id, data);
        }

        @Override
        public void setData(Object value) {
            this.setData(value, String.class);
        }
    }

    public static final class McItemInstance extends Obj<ItemInstance> {
        public McItemInstance(int id, ItemInstance data) {
            super(id, data);
        }

        @Override
        public void setData(Object value) {
            this.setData(value, ItemInstance.class);
        }
    }

    public static final class McVec3i extends Obj<Vec3i> {
        public McVec3i(int id, Vec3i data) {
            super(id, data);
        }

        @Override
        public void setData(Object value) {
            this.setData(value, Vec3i.class);
        }
    }
}
