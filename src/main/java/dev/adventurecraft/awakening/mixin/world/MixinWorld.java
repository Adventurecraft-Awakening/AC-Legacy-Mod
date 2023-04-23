package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.common.AC_TriggerManager;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class MixinWorld implements ExWorld {

    private AC_TriggerManager triggerManager = new AC_TriggerManager((World) (Object) this);

    public AC_TriggerManager getTriggerManager() {
        return this.triggerManager;
    }
}
