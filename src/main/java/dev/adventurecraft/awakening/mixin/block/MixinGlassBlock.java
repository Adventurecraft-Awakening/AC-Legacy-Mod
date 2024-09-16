package dev.adventurecraft.awakening.mixin.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.tile.GlassTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GlassTile.class)
public abstract class MixinGlassBlock {

    @Environment(EnvType.CLIENT)
    @Overwrite
    public int getRenderPass() {
        return 1;
    }
}
