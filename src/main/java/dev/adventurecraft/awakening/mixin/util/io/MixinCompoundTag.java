package dev.adventurecraft.awakening.mixin.util.io;

import dev.adventurecraft.awakening.extension.nbt.ExTag;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(CompoundTag.class)
public abstract class MixinCompoundTag implements ExCompoundTag {

    @Shadow private Map<String, Tag> entries = new Object2ObjectOpenHashMap<>(4);

    @Shadow
    public abstract void putByte(String key, byte val);

    @Shadow
    public abstract void putShort(String key, short val);

    @Shadow
    public abstract void putInt(String key, int val);

    @Override
    @Shadow
    public abstract void putString(String key, String val);

    @Override
    @Shadow
    public abstract void putTag(String key, Tag val);

    @Inject(
        method = "putShort(Ljava/lang/String;S)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void putByteForShort(String key, short val, CallbackInfo ci) {
        byte n = (byte) val;
        if (n == val) {
            this.putByte(key, n);
            ci.cancel();
        }
    }

    @Inject(
        method = "putInt(Ljava/lang/String;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void putShortForInt(String key, int val, CallbackInfo ci) {
        short n = (short) val;
        if (n == val) {
            this.putShort(key, n);
            ci.cancel();
        }
    }

    @Inject(
        method = "putLong(Ljava/lang/String;J)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void putIntForLong(String key, long val, CallbackInfo ci) {
        int n = (int) val;
        if (n == val) {
            this.putInt(key, n);
            ci.cancel();
        }
    }

    public @Overwrite byte getByte(String key) {
        return this.findByte(key).orElse((byte) 0);
    }

    public @Overwrite short getShort(String key) {
        return this.findShort(key).orElse((short) 0);
    }

    public @Overwrite int getInt(String key) {
        return this.findInt(key).orElse(0);
    }

    public @Overwrite long getLong(String key) {
        return this.findLong(key).orElse(0L);
    }

    // Overwrite further getters to avoid double map lookups.

    public @Overwrite float getFloat(String key) {
        return this.findFloat(key).orElse(0.0F);
    }

    public @Overwrite double getDouble(String key) {
        return this.findDouble(key).orElse(0.0);
    }

    public @Overwrite String getString(String key) {
        return this.findString(key).orElse("");
    }

    public @Overwrite byte[] getByteArray(String key) {
        return this.findByteArray(key).orElse(ByteArrays.EMPTY_ARRAY);
    }

    public @Overwrite CompoundTag getCompoundTag(String key) {
        return this.findCompound(key).orElseGet(CompoundTag::new);
    }

    public @Overwrite ListTag getList(String key) {
        return this.findList(key).orElseGet(ListTag::new);
    }

    @Override
    public CompoundTag copy() {
        var compound = new CompoundTag();
        this.forEach((key, tag) -> compound.putTag(key, ((ExTag) tag).copy()));
        return compound;
    }

    @Override
    public void forEach(BiConsumer<String, Tag> consumer) {
        this.entries.forEach(consumer);
    }

    @Override
    public Set<String> getKeys() {
        return this.entries.keySet();
    }

    @Override
    public Tag getTag(String key) {
        return this.entries.get(key);
    }

    @Override
    public Optional<Tag> removeTag(String key) {
        return Optional.of(this.entries.remove(key));
    }
}
