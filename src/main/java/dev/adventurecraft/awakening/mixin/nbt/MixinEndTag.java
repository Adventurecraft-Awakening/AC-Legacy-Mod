package dev.adventurecraft.awakening.mixin.nbt;

import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.LongTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(EndTag.class)
public abstract class MixinEndTag extends MixinTag {

    @Override
    public EndTag copy() {
        return new EndTag();
    }
}
