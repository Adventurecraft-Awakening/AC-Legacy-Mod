package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.IntTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(IntTag.class)
public abstract class MixinIntTag extends MixinTag implements NumericTag {

    @Shadow public int data;

    @Override
    public Optional<Integer> getInt() {
        return Optional.of(this.data);
    }

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((IntTag) (Object) this);
    }

    @Override
    public IntTag copy() {
        return new IntTag(this.data);
    }
}
