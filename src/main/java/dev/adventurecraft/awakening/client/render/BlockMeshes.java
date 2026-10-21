package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.block.BlockMesher;
import dev.adventurecraft.awakening.client.render.block.BlockMeshing;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import dev.adventurecraft.awakening.tile.AC_BlockShapes;
import net.minecraft.world.level.tile.Tile;

/** Shared retained inventory geometry for held blocks, dropped blocks and GUI previews. */
public final class BlockMeshes {
    private BlockMeshes() { }
    public static boolean standard3D(int shape) { return BlockMesher.canRender(shape); }
    public static boolean custom3D(int shape) {
        return switch(shape) {
            case AC_BlockShapes.REDSTONE_POWER,AC_BlockShapes.SPIKES,AC_BlockShapes.TABLE,AC_BlockShapes.CHAIR,
                AC_BlockShapes.ROPE,AC_BlockShapes.BLOCK_TREE,AC_BlockShapes.BLOCK_OVERLAY,AC_BlockShapes.BLOCK_SLOPE -> true;
            default -> false;
        };
    }
    public static VulkanRenderer.Mesh inventory(Tile tile,int metadata,boolean colored) {
        return EntityMeshes.mesh("inventory:"+tile.id+":"+metadata+":"+colored, () -> {
            var vertices=new QuadBuilder(256);
            var mesher=new BlockMesher(null,vertices,0,0,0,BlockMeshing.inventoryOptions());
            mesher.inventoryColors(colored);
            mesher.inventory(tile,metadata,1);
            return vertices.vertices();
        });
    }
}
