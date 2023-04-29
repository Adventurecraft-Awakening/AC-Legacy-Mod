package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.FallingBlockRenderer;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FallingBlockRenderer.class)
public abstract class MixinFallingBlockRenderer extends EntityRenderer {

    @Shadow
    private BlockRenderer field_857;

    @Overwrite
    public void render(FallingBlockEntity entity, double rX, double rY, double rZ, float var8, float var9) {
        int bX = MathHelper.floor(entity.x);
        int bY = MathHelper.floor(entity.y);
        int bZ = MathHelper.floor(entity.z);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) rX, (float) rY, (float) rZ);
        Block block = Block.BY_ID[entity.blockId];
        int textureNum = ((ExBlock) block).getTextureNum();
        if (textureNum == 0) {
            this.bindTexture("/terrain.png");
        } else {
            this.bindTexture(String.format("/terrain%d.png", textureNum));
        }

        World world = entity.getFallingLevel();
        GL11.glDisable(GL11.GL_LIGHTING);
        int id = world.getBlockId(bX, bY, bZ);
        int meta = world.getBlockMeta(bX, bY, bZ);
        ((ExWorld) world).setBlockAndMetadataTemp(bX, bY, bZ, entity.blockId, ((ExFallingBlockEntity) entity).getBlockMeta());
        this.field_857.method_53(block, world, bX, bY, bZ);
        ((ExWorld) world).setBlockAndMetadataTemp(bX, bY, bZ, id, meta);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
