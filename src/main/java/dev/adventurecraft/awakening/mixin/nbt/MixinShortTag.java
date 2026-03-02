package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.ShortTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ShortTag.class)
public abstract class MixinShortTag extends MixinTag implements NumericTag {

    @Shadow public short data;

    @Override
    public Optional<Integer> getInt() {
        return Optional.of((int) this.data);
    }

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((ShortTag) (Object) this);
    }

    @Override
    public ShortTag copy() {
        return new ShortTag(this.data);
    }
}
