package dev.adventurecraft.awakening.mixin.nbt;

import net.minecraft.nbt.StringTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(StringTag.class)
public abstract class MixinStringTag extends MixinTag {

    @Shadow public String contents;

    @Override
    public Optional<String> getString() {
        return Optional.of(this.contents);
    }

    @Override
    public StringTag copy() {
        return new StringTag(this.contents);
    }
}
