package dev.adventurecraft.awakening.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import dev.adventurecraft.awakening.common.CommandUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ArgumentCommandNode.class, remap = false)
public abstract class MixinArgumentCommandNode<S, T> {

    @Shadow @Final private static String USAGE_ARGUMENT_OPEN;
    @Shadow @Final private static String USAGE_ARGUMENT_CLOSE;

    public abstract @Shadow String getName();

    @Shadow
    public abstract ArgumentType<S> getType();

    @Overwrite
    public String getUsageText() {
        String typeName = CommandUtils.getShortName(CommandUtils.getClass(this.getType()));
        return USAGE_ARGUMENT_OPEN + this.getName() + ":" + typeName + USAGE_ARGUMENT_CLOSE;
    }
}
