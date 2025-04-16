package dev.adventurecraft.awakening.mixin.world.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.world.entity.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(SynchedEntityData.class)
public abstract class MixinSynchedEntityData {

    @Shadow
    private final Map<Integer, SynchedEntityData.DataItem> itemsById = new Int2ObjectArrayMap<>();
}
