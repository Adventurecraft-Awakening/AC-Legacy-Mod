package dev.adventurecraft.awakening.mixin.world.entity;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.world.entity.SynchedEntityData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

import java.util.Map;

@Mixin(SynchedEntityData.DataItem.class)
public abstract class MixinSynchedEntityData$DataItem
    implements Int2ObjectMap<SynchedEntityData.DataItem>, Int2ObjectMap.Entry<SynchedEntityData.DataItem> {

    @Shadow @Final private int id;

    @Override
    public int getIntKey() {
        return this.id;
    }

    @Override
    public SynchedEntityData.DataItem getValue() {
        return (SynchedEntityData.DataItem) (Object) this;
    }

    @Override
    public SynchedEntityData.DataItem setValue(SynchedEntityData.DataItem dataItem) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        return o == this.getValue();
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends SynchedEntityData.DataItem> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(SynchedEntityData.DataItem rv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SynchedEntityData.DataItem defaultReturnValue() {
        return null;
    }

    @Override
    public ObjectSet<Entry<SynchedEntityData.DataItem>> int2ObjectEntrySet() {
        return ObjectSet.of(this);
    }

    @Override
    public @NotNull IntSet keySet() {
        return IntSet.of(this.getIntKey());
    }

    @Override
    public @NotNull ObjectCollection<SynchedEntityData.DataItem> values() {
        return ObjectSet.of(this.getValue());
    }

    @Override
    public SynchedEntityData.DataItem get(int key) {
        if (key == this.getIntKey()) {
            return this.getValue();
        }
        return this.defaultReturnValue();
    }

    @Override
    public boolean containsKey(int key) {
        return key == this.getIntKey();
    }
}