package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.render.entity.block.BlockEntityRenderer;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class AC_TileEntityStoreRenderer extends BlockEntityRenderer {
    static ItemStack item = new ItemStack(0, 0, 0);
    static ItemEntity eItem = new ItemEntity((World) null, 0.0D, 0.0D, 0.0D, item);
    static ItemRenderer renderItem = new ItemRenderer();

    public AC_TileEntityStoreRenderer() {
        renderItem.setDispatcher(EntityRenderDispatcher.INSTANCE);
    }

    public void renderTileEntityStore(AC_TileEntityStore var1, double var2, double var4, double var6, float var8) {
        if (var1.buySupplyLeft != 0 && var1.buyItemID != 0) {
            item.itemId = var1.buyItemID;
            item.count = var1.buyItemAmount;
            item.setMeta(var1.buyItemDamage);
            eItem.world = var1.world;
            eItem.setPosition((double) var1.x, (double) var1.y, (double) var1.z);
            renderItem.render(eItem, var2 + 0.5D, var4 + 0.125D, var6 + 0.5D, 0.0F, 0.0F);
        }

        if (AC_DebugMode.active && var1.tradeTrigger != null) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glTranslatef((float) var2 + 0.5F, (float) var4 + 0.5F, (float) var6 + 0.5F);
            GL11.glLineWidth(6.0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glBegin(GL11.GL_LINES);

            for (int var9 = var1.tradeTrigger.minX; var9 <= var1.tradeTrigger.maxX; ++var9) {
                for (int var10 = var1.tradeTrigger.minY; var10 <= var1.tradeTrigger.maxY; ++var10) {
                    for (int var11 = var1.tradeTrigger.minZ; var11 <= var1.tradeTrigger.maxZ; ++var11) {
                        Block var12 = Block.BY_ID[var1.world.getBlockId(var9, var10, var11)];
                        if (var12 != null && ((ExBlock) var12).canBeTriggered()) {
                            GL11.glColor3f(0.0F, 0.0F, 0.0F);
                            GL11.glVertex3f(0.0F, 0.0F, 0.0F);
                            GL11.glColor3f(1.0F, 1.0F, 1.0F);
                            GL11.glVertex3f((float) (var9 - var1.x), (float) (var10 - var1.y), (float) (var11 - var1.z));
                        }
                    }
                }
            }

            GL11.glEnd();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glLineWidth(1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }

    }

    public void render(BlockEntity var1, double var2, double var4, double var6, float var8) {
        this.renderTileEntityStore((AC_TileEntityStore) var1, var2, var4, var6, var8);
    }

    static {
        ((ExItemRenderer) renderItem).setScale(1.5F);
    }
}
