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

    @Overwrite
    public short getShort(String var1) {
        Tag tag = this.entries.get(var1);
        if (tag == null) {
            return 0;
        } else if (tag instanceof ShortTag sTag) {
            return sTag.data;
        } else {
            return ((ByteTag) tag).data;
        }
    }

    @Overwrite
    public int getInt(String var1) {
        Tag tag = this.entries.get(var1);
        if (tag == null) {
            return 0;
        } else if (tag instanceof IntTag iTag) {
            return iTag.data;
        } else if (tag instanceof ShortTag sTag) {
            return sTag.data;
        } else {
            return ((ByteTag) tag).data;
        }
    }

    @Overwrite
    public long getLong(String var1) {
        Tag tag = this.entries.get(var1);
        if (tag == null) {
            return 0;
        } else if (tag instanceof LongTag lTag) {
            return lTag.data;
        } else if (tag instanceof IntTag iTag) {
            return iTag.data;
        } else if (tag instanceof ShortTag sTag) {
            return sTag.data;
        } else {
            return ((ByteTag) tag).data;
        }
    }

    @Override
    public Set<String> getKeys() {
        return this.entries.keySet();
    }

    @Override
    public Object getValue(String key) {
        return this.entries.get(key);
    }
}
