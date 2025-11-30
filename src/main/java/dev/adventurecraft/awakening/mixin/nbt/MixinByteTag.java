package dev.adventurecraft.awakening.mixin.nbt;

import net.minecraft.nbt.ByteTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ByteTag.class)
public abstract class MixinByteTag extends MixinTag {

    @Shadow public byte data;

    @Override
    public Optional<Integer> getInt() {
        return Optional.of((int) this.data);
    }

    @Override
    public ByteTag copy() {
        return new ByteTag(this.data);
    }
}
