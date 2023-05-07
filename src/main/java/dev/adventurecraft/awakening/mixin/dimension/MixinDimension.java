package dev.adventurecraft.awakening.mixin.dimension;

import dev.adventurecraft.awakening.common.AC_ChunkProviderHeightMapGenerate;
import dev.adventurecraft.awakening.common.WorldGenProperties;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.extension.world.source.ExOverworldWorldSource;
import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.source.OverworldWorldSource;
import net.minecraft.world.source.WorldSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Dimension.class)
public abstract class MixinDimension {

    @Shadow
    public World world;

    @Inject(method = "createWorldSource", at = @At("HEAD"), cancellable = true)
    private void injectHeightMapGenerator(CallbackInfoReturnable<WorldSource> cir) {
        if (((ExWorldProperties) this.world.properties).getWorldGenProps().useImages) {
            var generator = new AC_ChunkProviderHeightMapGenerate(this.world, this.world.getSeed());
            cir.setReturnValue(generator);
        }
    }

    @Inject(method = "createWorldSource", at = @At(value = "RETURN"))
    public void createWorldSource(CallbackInfoReturnable<WorldSource> cir) {
        var propCopy = new WorldGenProperties();
        WorldGenProperties props = ((ExWorldProperties) this.world.properties).getWorldGenProps();
        props.copyTo(propCopy);

        var source = (OverworldWorldSource) cir.getReturnValue();
        ((ExOverworldWorldSource) source).setWorldGenProps(propCopy);
    }

    @Overwrite
    public boolean canSpawnOn(int var1, int var2) {
        int id = this.world.getSurfaceBlockId(var1, var2);
        return id != 0 && Block.BY_ID[id] != null && !(Block.BY_ID[id] instanceof AbstractFluidBlock);
    }
}
