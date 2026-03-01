package dev.adventurecraft.awakening.mixin.nbt;

import net.minecraft.nbt.ShortTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ShortTag.class)
public abstract class MixinShortTag extends MixinTag {

    @Shadow public short data;

    @Override
    public Optional<Integer> getInt() {
        return Optional.of((int) this.data);
    }

    @Override
    public ShortTag copy() {
        return new ShortTag(this.data);
    }
}
