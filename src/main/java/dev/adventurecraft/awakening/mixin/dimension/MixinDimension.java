package dev.adventurecraft.awakening.mixin.dimension;

import dev.adventurecraft.awakening.common.AC_ChunkProviderHeightMapGenerate;
import dev.adventurecraft.awakening.common.WorldGenProperties;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.extension.world.source.ExOverworldWorldSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.levelgen.RandomLevelSource;
import net.minecraft.world.level.tile.LiquidTile;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Dimension.class)
public abstract class MixinDimension {

    @Shadow
    public Level level;

    @Inject(method = "createRandomLevelSource", at = @At("HEAD"), cancellable = true)
    private void injectHeightMapGenerator(CallbackInfoReturnable<ChunkSource> cir) {
        if (((ExWorldProperties) this.level.levelData).getWorldGenProps().useImages) {
            var generator = new AC_ChunkProviderHeightMapGenerate(this.level, this.level.getSeed());
            cir.setReturnValue(generator);
        }
    }

    @Inject(method = "createRandomLevelSource", at = @At(value = "RETURN"))
    public void createWorldSource(CallbackInfoReturnable<ChunkSource> cir) {
        var propCopy = new WorldGenProperties();
        WorldGenProperties props = ((ExWorldProperties) this.level.levelData).getWorldGenProps();
        props.copyTo(propCopy);

        var source = (RandomLevelSource) cir.getReturnValue();
        ((ExOverworldWorldSource) source).setWorldGenProps(propCopy);
    }

    @Overwrite
    public boolean isValidSpawn(int var1, int var2) {
        int id = this.level.getTopTile(var1, var2);
        return id != 0 && Tile.tiles[id] != null && !(Tile.tiles[id] instanceof LiquidTile);
    }
}
