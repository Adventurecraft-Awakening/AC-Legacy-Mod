package dev.adventurecraft.awakening.mixin.client.render.entity;

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
import org.lwjgl.opengl.GL11;
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
        GL11.glPushMatrix();
        GL11.glTranslatef((float) rX, (float) rY, (float) rZ);
        Tile block = Tile.tiles[entity.tileId];
        int textureNum = ((ExBlock) block).getTextureNum();
        if (textureNum == 0) {
            this.bindTexture("/terrain.png");
        } else {
            this.bindTexture(String.format("/terrain%d.png", textureNum));
        }

        Level world = entity.getLevel();
        GL11.glDisable(GL11.GL_LIGHTING);
        int id = world.getTile(bX, bY, bZ);
        int meta = world.getData(bX, bY, bZ);
        int entityMeta = ((ExFallingBlockEntity) entity).getMetadata();
        ((ExWorld) world).setBlockAndMetadataTemp(bX, bY, bZ, entity.tileId, entityMeta);
        this.tileRenderer.renderBlock(block, world, bX, bY, bZ);
        ((ExWorld) world).setBlockAndMetadataTemp(bX, bY, bZ, id, meta);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
