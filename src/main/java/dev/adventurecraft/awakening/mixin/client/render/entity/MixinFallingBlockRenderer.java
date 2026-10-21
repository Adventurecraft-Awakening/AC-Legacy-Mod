package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.client.render.block.BlockMesher;
import dev.adventurecraft.awakening.client.render.block.BlockMeshing;
import dev.adventurecraft.awakening.client.render.block.BlockPreviewView;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import net.minecraft.client.Minecraft;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.FallingTileRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FallingTileRenderer.class)
public abstract class MixinFallingBlockRenderer extends EntityRenderer {

    @Shadow
    private TileRenderer tileRenderer;

    @Overwrite
    public void render(FallingTile entity, double rX, double rY, double rZ, float var8, float var9) {
        int bX = Mth.floor(entity.x);
        int bY = Mth.floor(entity.y);
        int bZ = Mth.floor(entity.z);
        var pose = new PoseStack(LegacyPassBridge.worldEntry());
        pose.translate((float) rX, (float) rY, (float) rZ);
        Tile block = Tile.tiles[entity.tileId];
        int textureNum = ((ExBlock) block).getTextureNum();
        String path = textureNum == 0 ? "/terrain.png" : "/terrain%d.png".formatted(textureNum);
        var texture = ((ExTextureManager)Minecraft.instance.textures).texture(path);
        var parameters = pose.parameters().withMaterial(pose.parameters().material().withLighting(false));
        Level world = entity.getLevel();
        var preview=new BlockPreviewView(world,bX,bY,bZ,entity.tileId,((ExFallingBlockEntity)entity).getMetadata());
        var vertices=new QuadBuilder(128);
        var mesher=new BlockMesher(preview,vertices,bX+.5,bY+.5,bZ+.5,BlockMeshing.options(world));
        mesher.worldUnculled(block,bX,bY,bZ);
        NativeRenderer.drawQuads(vertices.vertices(),texture,parameters);

    }
}
