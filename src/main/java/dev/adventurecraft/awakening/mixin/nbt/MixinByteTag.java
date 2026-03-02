package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.ByteTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ByteTag.class)
public abstract class MixinByteTag extends MixinTag implements NumericTag {

    @Shadow public byte data;

    @Override
    public Optional<Integer> getInt() {
        return Optional.of((int) this.data);
    }

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((ByteTag) (Object) this);
    }

    @Override
    public ByteTag copy() {
        return new ByteTag(this.data);
    }
}
