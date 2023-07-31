package dev.adventurecraft.awakening.mixin.util.io;

import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import net.minecraft.util.io.*;
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
    private Map<String, AbstractTag> data;

    @Shadow
    public abstract void put(String var1, byte var2);

    @Shadow
    public abstract void put(String var1, short var2);

    @Shadow
    public abstract void put(String var1, int var2);

    @Inject(method = "put(Ljava/lang/String;S)V", at = @At("HEAD"), cancellable = true)
    private void putByteForShort(String var1, short var2, CallbackInfo ci) {
        if ((byte) var2 == var2) {
            this.put(var1, (byte) var2);
            ci.cancel();
        }
    }

    @Inject(method = "put(Ljava/lang/String;I)V", at = @At("HEAD"), cancellable = true)
    private void putShortForInt(String var1, int var2, CallbackInfo ci) {
        if ((short) var2 == var2) {
            this.put(var1, (short) var2);
            ci.cancel();
        }
    }

    @Inject(method = "put(Ljava/lang/String;J)V", at = @At("HEAD"), cancellable = true)
    private void putIntForLong(String var1, long var2, CallbackInfo ci) {
        if ((int) var2 == var2) {
            this.put(var1, (int) var2);
            ci.cancel();
        }
    }

    @Overwrite
    public short getShort(String var1) {
        AbstractTag tag = this.data.get(var1);
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
        AbstractTag tag = this.data.get(var1);
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
        AbstractTag tag = this.data.get(var1);
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
        return this.data.keySet();
    }

    @Override
    public Object getValue(String key) {
        return this.data.get(key);
    }
}
