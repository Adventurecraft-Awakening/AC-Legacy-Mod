package dev.adventurecraft.awakening.mixin.nbt;

import net.minecraft.nbt.ByteArrayTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ByteArrayTag.class)
public abstract class MixinByteArrayTag extends MixinTag {

    @Shadow public byte[] data;

    @Override
    public ByteArrayTag copy() {
        return new ByteArrayTag(this.data.clone());
    }
}
