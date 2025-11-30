package dev.adventurecraft.awakening.mixin.nbt;

import net.minecraft.nbt.IntTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(IntTag.class)
public abstract class MixinIntTag extends MixinTag {

    @Shadow public int data;

    @Override
    public Optional<Integer> getInt() {
        return Optional.of(this.data);
    }

    @Override
    public IntTag copy() {
        return new IntTag(this.data);
    }
}
