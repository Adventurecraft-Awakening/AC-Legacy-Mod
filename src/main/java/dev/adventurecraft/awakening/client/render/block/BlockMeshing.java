package dev.adventurecraft.awakening.client.render.block;

import dev.adventurecraft.awakening.client.render.chunk.LevelRegion;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;

/** Captures client policy once, outside the CPU geometry builder. */
public final class BlockMeshing {
    private static final BlockMesher.Activation INACTIVE = (x,y,z) -> false;
    private static final BlockMesher.Options INVENTORY = new BlockMesher.Options(false,false,true,1,INACTIVE);
    private BlockMeshing() { }
    public static BlockMesher.Options inventoryOptions() { return INVENTORY; }
    public static BlockMesher.Options options(LevelSource world) {
        BlockMesher.Activation activation = world instanceof LevelRegion region ? region::isActivated
            : world instanceof Level level ? (x,y,z) -> ((ExWorld)level).getTriggerManager().isActivated(x,y,z)
            : INACTIVE;
        return new BlockMesher.Options(Minecraft.useAmbientOcclusion(),GameRenderer.anaglyph3d,
            TileRenderer.fancy,((ExGameOptions)Minecraft.instance.options).ofAoLevel(),activation);
    }
}
