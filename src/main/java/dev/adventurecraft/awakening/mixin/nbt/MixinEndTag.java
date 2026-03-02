package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.EndTag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndTag.class)
public abstract class MixinEndTag extends MixinTag {

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((EndTag) (Object) this);
    }

    @Override
    public EndTag copy() {
        return new EndTag();
    }
}
