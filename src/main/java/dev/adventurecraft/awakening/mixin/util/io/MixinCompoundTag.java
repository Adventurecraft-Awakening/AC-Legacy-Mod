package dev.adventurecraft.awakening.mixin.util.io;

import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(CompoundTag.class)
public abstract class MixinCompoundTag implements ExCompoundTag {

    @Shadow
    private Map<String, Tag> entries = new Object2ObjectOpenHashMap<>(4);

    @Shadow
    public abstract void putByte(String var1, byte var2);

    @Shadow
    public abstract void putShort(String var1, short var2);

    @Shadow
    public abstract void putInt(String var1, int var2);

    @Inject(method = "putShort(Ljava/lang/String;S)V", at = @At("HEAD"), cancellable = true)
    private void putByteForShort(String var1, short var2, CallbackInfo ci) {
        if ((byte) var2 == var2) {
            this.putByte(var1, (byte) var2);
            ci.cancel();
        }
    }

    @Inject(method = "putInt(Ljava/lang/String;I)V", at = @At("HEAD"), cancellable = true)
    private void putShortForInt(String var1, int var2, CallbackInfo ci) {
        if ((short) var2 == var2) {
            this.putShort(var1, (short) var2);
            ci.cancel();
        }
    }

    @Inject(method = "putLong(Ljava/lang/String;J)V", at = @At("HEAD"), cancellable = true)
    private void putIntForLong(String var1, long var2, CallbackInfo ci) {
        if ((int) var2 == var2) {
            this.putInt(var1, (int) var2);
            ci.cancel();
        }
    }

    @Override
    public Optional<Byte> findByte(String key) {
        Tag tag = this.getTag(key);
        if (tag instanceof ByteTag bTag) {
            return Optional.of(bTag.data);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Short> findShort(String key) {
        Tag tag = this.getTag(key);
        if (tag instanceof ShortTag sTag) {
            return Optional.of(sTag.data);
        } else if (tag instanceof ByteTag bTag) {
            return Optional.of((short) bTag.data);
        }
        return Optional.empty();
    }

    @Overwrite
    public short getShort(String key) {
        return this.findShort(key).orElse((short) 0);
    }

    @Override
    public Optional<Integer> findInt(String key) {
        Tag tag = this.getTag(key);
        if (tag instanceof IntTag iTag) {
            return Optional.of(iTag.data);
        } else if (tag instanceof ShortTag sTag) {
            return Optional.of((int) sTag.data);
        } else if (tag instanceof ByteTag bTag) {
            return Optional.of((int) bTag.data);
        }
        return Optional.empty();
    }

    @Overwrite
    public int getInt(String key) {
        return this.findInt(key).orElse(0);
    }

    @Override
    public Optional<Long> findLong(String key) {
        Tag tag = this.getTag(key);
        if (tag instanceof LongTag lTag) {
            return Optional.of(lTag.data);
        } else if (tag instanceof IntTag iTag) {
            return Optional.of((long) iTag.data);
        } else if (tag instanceof ShortTag sTag) {
            return Optional.of((long) sTag.data);
        } else if (tag instanceof ByteTag bTag) {
            return Optional.of((long) bTag.data);
        }
        return Optional.empty();
    }

    @Overwrite
    public long getLong(String key) {
        return this.findLong(key).orElse(0L);
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
    public Optional<Tag> findTag(String key) {
        return Optional.ofNullable(this.getTag(key));
    }
}
