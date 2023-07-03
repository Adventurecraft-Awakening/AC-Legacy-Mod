package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_RenderBoomerang extends EntityRenderer {

    public AC_RenderBoomerang() {
        this.field_2678 = 0.15F;
        this.field_2679 = 12.0F / 16.0F;
    }

    public void doRenderItem(AC_EntityBoomerang entity, double x, double y, double z, float angle, float deltaTime) {
        float pitch = entity.prevPitch + (entity.pitch - entity.prevPitch) * deltaTime;
        float rotation = entity.prevBoomerangRotation + (entity.boomerangRotation - entity.prevBoomerangRotation) * deltaTime;
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
        this.bindTexture("/gui/items.png");
        Vec2 texSize = ((ExTextureManager) this.dispatcher.textureManager).getTextureResolution("/gui/items.png");
        int texCols = texSize.x / 16;
        int texRows = texSize.y / 16;
        float tReciX = 0.5F / (float) texSize.x;
        float tReciY = 0.5F / (float) texSize.y;
        Tessellator ts = Tessellator.INSTANCE;
        int texPos = AC_Items.boomerang.getTexturePosition(null);
        float tX1 = ((float) (texPos % 16 * 16) + 0.0F) / 256.0F;
        float tX2 = ((float) (texPos % 16 * 16) + 15.99F) / 256.0F;
        float tY1 = ((float) (texPos / 16 * 16) + 0.0F) / 256.0F;
        float tY2 = ((float) (texPos / 16 * 16) + 15.99F) / 256.0F;
        float vX = 1.0F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        float vY = 1.0F / 16.0F;
        GL11.glTranslatef(-0.5F, 0.0F, -0.5F);
        ts.start();
        ts.setNormal(0.0F, 0.0F, 1.0F);
        ts.vertex(0.0D, 0.0D - vY, 0.0D, tX2, tY2);
        ts.vertex(vX, 0.0D - vY, 0.0D, tX1, tY2);
        ts.vertex(vX, 0.0D - vY, 1.0D, tX1, tY1);
        ts.vertex(0.0D, 0.0D - vY, 1.0D, tX2, tY1);
        ts.tessellate();
        ts.start();
        ts.setNormal(0.0F, 0.0F, -1.0F);
        ts.vertex(0.0D, 0.0D, 1.0D, tX2, tY1);
        ts.vertex(vX, 0.0D, 1.0D, tX1, tY1);
        ts.vertex(vX, 0.0D, 0.0D, tX1, tY2);
        ts.vertex(0.0D, 0.0D, 0.0D, tX2, tY2);
        ts.tessellate();
        ts.start();
        ts.setNormal(-1.0F, 0.0F, 0.0F);

        for (int col = 0; col < texCols; ++col) {
            float d = (float) col / (float) texCols;
            float tX = tX2 + (tX1 - tX2) * d - tReciX;
            float pX = vX * d;
            ts.vertex(pX, 0.0F - vY, 1.0D, tX, tY1);
            ts.vertex(pX, 0.0D, 1.0D, tX, tY1);
            ts.vertex(pX, 0.0D, 0.0D, tX, tY2);
            ts.vertex(pX, 0.0F - vY, 0.0D, tX, tY2);
        }

        ts.tessellate();
        ts.start();
        ts.setNormal(1.0F, 0.0F, 0.0F);

        for (int col = 0; col < texCols; ++col) {
            float d = (float) col / (float) texCols;
            float tX = tX2 + (tX1 - tX2) * d - tReciX;
            float pX = vX * d + 1.0F / (float) texCols;
            ts.vertex(pX, 0.0F - vY, 0.0D, tX, tY2);
            ts.vertex(pX, 0.0D, 0.0D, tX, tY2);
            ts.vertex(pX, 0.0D, 1.0D, tX, tY1);
            ts.vertex(pX, 0.0F - vY, 1.0D, tX, tY1);
        }

        ts.tessellate();
        ts.start();
        ts.setNormal(0.0F, 1.0F, 0.0F);

        for (int row = 0; row < texRows; ++row) {
            float d = (float) row / (float) texRows;
            float tY = tY2 + (tY1 - tY2) * d - tReciY;
            float pZ = vX * d + 1.0F / (float) texRows;
            ts.vertex(0.0D, 0.0F - vY, pZ, tX2, tY);
            ts.vertex(vX, 0.0F - vY, pZ, tX1, tY);
            ts.vertex(vX, 0.0D, pZ, tX1, tY);
            ts.vertex(0.0D, 0.0D, pZ, tX2, tY);
        }

        ts.tessellate();
        ts.start();
        ts.setNormal(0.0F, -1.0F, 0.0F);

        for (int row = 0; row < texRows; ++row) {
            float d = (float) row / (float) texRows;
            float tY = tY2 + (tY1 - tY2) * d - tReciY;
            float pZ = vX * d;
            ts.vertex(vX, 0.0F - vY, pZ, tX1, tY);
            ts.vertex(0.0D, 0.0F - vY, pZ, tX2, tY);
            ts.vertex(0.0D, 0.0D, pZ, tX2, tY);
            ts.vertex(vX, 0.0D, pZ, tX1, tY);
        }

        ts.tessellate();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float angle, float deltaTime) {
        this.doRenderItem((AC_EntityBoomerang) entity, x, y, z, angle, deltaTime);
    }
}
