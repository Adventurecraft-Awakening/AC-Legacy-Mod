package dev.adventurecraft.awakening.mixin.world.entity;

import dev.adventurecraft.awakening.world.entity.DataItemArrayMap;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.world.entity.SynchedEntityData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(SynchedEntityData.class)
public abstract class MixinSynchedEntityData {

    private static final Map<Integer, SynchedEntityData.DataItem> EMPTY_MAP = Int2ObjectMaps.emptyMap();

    // Most entities have only one DATA_FLAGS byte entry.
    // Overhead is reduced by using specialized hash-maps.
    @Shadow @Final @Mutable private Map<Integer, SynchedEntityData.DataItem> itemsById = EMPTY_MAP;

    @Redirect(
        method = "define",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private Object ac$redirectPut(Map<Integer, SynchedEntityData.DataItem> map, Object oKey, Object oVal) {
        int key = (int) oKey;
        var val = (SynchedEntityData.DataItem) oVal;
        if (key != val.getId()) {
            throw new UnsupportedOperationException("DataItem Id does not match given key.");
        }

        // no need to check id on put since replacing items is not allowed
        if (map == EMPTY_MAP) {
            this.itemsById = (Int2ObjectMap<SynchedEntityData.DataItem>) val;
        }
        else if (map instanceof SynchedEntityData.DataItem item) {
            this.itemsById = new DataItemArrayMap(item, val);
        }
        else {
            this.itemsById = new DataItemArrayMap(this.itemsById.values(), val);
        }
        return null;
    }
}