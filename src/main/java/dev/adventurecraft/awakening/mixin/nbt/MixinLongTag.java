package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.LongTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(LongTag.class)
public abstract class MixinLongTag extends MixinTag implements NumericTag {

    @Shadow public long data;

    @Override
    public Optional<Integer> getInt() {
        long l = this.data;
        int i = (int) l;
        return i == l ? Optional.of(i) : Optional.empty();
    }

    public Optional<Long> getLong() {
        return Optional.of(this.data);
    }

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((LongTag) (Object) this);
    }

    @Override
    public LongTag copy() {
        return new LongTag(this.data);
    }
}
