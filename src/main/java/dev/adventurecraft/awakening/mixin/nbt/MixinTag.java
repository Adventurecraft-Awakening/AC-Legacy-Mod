package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.extension.nbt.ExTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@Mixin(Tag.class)
public abstract class MixinTag implements ExTag {

    @Invoker
    public abstract void invokeWrite(DataOutput output)
        throws IOException;

    @Invoker
    public abstract void invokeRead(DataInput input)
        throws IOException;

    @Override
    public abstract Tag copy();
}
