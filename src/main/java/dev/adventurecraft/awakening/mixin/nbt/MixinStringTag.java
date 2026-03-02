package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.PrimitiveTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.StringTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(StringTag.class)
public abstract class MixinStringTag extends MixinTag implements PrimitiveTag {

    @Shadow public String contents;

    @Override
    public Optional<String> getString() {
        return Optional.of(this.contents);
    }

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((StringTag) (Object) this);
    }

    @Override
    public StringTag copy() {
        return new StringTag(this.contents);
    }
}
