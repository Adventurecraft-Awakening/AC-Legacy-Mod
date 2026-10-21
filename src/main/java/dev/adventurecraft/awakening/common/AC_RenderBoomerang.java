package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.EntityMeshes;
import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.PoseStack;
import dev.adventurecraft.awakening.client.render.QuadBuilder;
import net.minecraft.client.Minecraft;
import dev.adventurecraft.awakening.entity.AC_EntityBoomerang;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.layout.Size;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;

public class AC_RenderBoomerang extends EntityRenderer {

    public AC_RenderBoomerang() {
        this.shadowRadius = 0.15F;
        this.shadowStrength = 12.0F / 16.0F;
    }

    public void doRenderItem(AC_EntityBoomerang entity, double x, double y, double z, float angle, float deltaTime) {
        float pitch = entity.xRotO + (entity.xRot - entity.xRotO) * deltaTime;
        float rotation =
            entity.prevBoomerangRotation + (entity.boomerangRotation - entity.prevBoomerangRotation) * deltaTime;
        var pose = new PoseStack(LegacyPassBridge.worldEntry());
        pose.translate((float)x,(float)y,(float)z); pose.rotate(-angle,0,1,0);
        pose.rotate(pitch,1,0,0); pose.rotate(rotation,0,1,0); pose.translate(-.5f,0,-.5f);
        var texture = ((dev.adventurecraft.awakening.extension.client.ExTextureManager) Minecraft.instance.textures).texture("/gui/items.png");
        Size texSize = texture.size(0);
        var mesh = EntityMeshes.mesh("boomerang:"+texSize.w+":"+texSize.h, () -> {
        int texCols = texSize.w / 16;
        int texRows = texSize.h / 16;
        float tReciX = 0.5F / (float) texSize.w;
        float tReciY = 0.5F / (float) texSize.h;
        int texPos = AC_Items.boomerang.getIcon(null);
        float tX1 = ((float) (texPos % 16 * 16) + 0.0F) / 256.0F;
        float tX2 = ((float) (texPos % 16 * 16) + 15.99F) / 256.0F;
        float tY1 = ((float) (texPos / 16 * 16) + 0.0F) / 256.0F;
        float tY2 = ((float) (texPos / 16 * 16) + 15.99F) / 256.0F;
        float vX = 1.0F;
        float vY = 1.0F / 16.0F;

        QuadBuilder ts = new QuadBuilder(8 + (texCols + texRows) * 8);

        ts.normal(0.0F, 0.0F, 1.0F);
        ts.vertexUV(0.0D, 0.0D - vY, 0.0D, tX2, tY2);
        ts.vertexUV(vX, 0.0D - vY, 0.0D, tX1, tY2);
        ts.vertexUV(vX, 0.0D - vY, 1.0D, tX1, tY1);
        ts.vertexUV(0.0D, 0.0D - vY, 1.0D, tX2, tY1);

        ts.normal(0.0F, 0.0F, -1.0F);
        ts.vertexUV(0.0D, 0.0D, 1.0D, tX2, tY1);
        ts.vertexUV(vX, 0.0D, 1.0D, tX1, tY1);
        ts.vertexUV(vX, 0.0D, 0.0D, tX1, tY2);
        ts.vertexUV(0.0D, 0.0D, 0.0D, tX2, tY2);

        ts.normal(-1.0F, 0.0F, 0.0F);
        for (int col = 0; col < texCols; ++col) {
            float d = (float) col / (float) texCols;
            float tX = tX2 + (tX1 - tX2) * d - tReciX;
            float pX = vX * d;
            ts.vertexUV(pX, 0.0F - vY, 1.0D, tX, tY1);
            ts.vertexUV(pX, 0.0D, 1.0D, tX, tY1);
            ts.vertexUV(pX, 0.0D, 0.0D, tX, tY2);
            ts.vertexUV(pX, 0.0F - vY, 0.0D, tX, tY2);
        }

        ts.normal(1.0F, 0.0F, 0.0F);
        for (int col = 0; col < texCols; ++col) {
            float d = (float) col / (float) texCols;
            float tX = tX2 + (tX1 - tX2) * d - tReciX;
            float pX = vX * d + 1.0F / (float) texCols;
            ts.vertexUV(pX, 0.0F - vY, 0.0D, tX, tY2);
            ts.vertexUV(pX, 0.0D, 0.0D, tX, tY2);
            ts.vertexUV(pX, 0.0D, 1.0D, tX, tY1);
            ts.vertexUV(pX, 0.0F - vY, 1.0D, tX, tY1);
        }

        ts.normal(0.0F, 1.0F, 0.0F);
        for (int row = 0; row < texRows; ++row) {
            float d = (float) row / (float) texRows;
            float tY = tY2 + (tY1 - tY2) * d - tReciY;
            float pZ = vX * d + 1.0F / (float) texRows;
            ts.vertexUV(0.0D, 0.0F - vY, pZ, tX2, tY);
            ts.vertexUV(vX, 0.0F - vY, pZ, tX1, tY);
            ts.vertexUV(vX, 0.0D, pZ, tX1, tY);
            ts.vertexUV(0.0D, 0.0D, pZ, tX2, tY);
        }

        ts.normal(0.0F, -1.0F, 0.0F);
        for (int row = 0; row < texRows; ++row) {
            float d = (float) row / (float) texRows;
            float tY = tY2 + (tY1 - tY2) * d - tReciY;
            float pZ = vX * d;
            ts.vertexUV(vX, 0.0F - vY, pZ, tX1, tY);
            ts.vertexUV(0.0D, 0.0F - vY, pZ, tX2, tY);
            ts.vertexUV(0.0D, 0.0D, pZ, tX2, tY);
            ts.vertexUV(vX, 0.0D, pZ, tX1, tY);
        }

            return ts.vertices();
        });
        NativeRenderer.drawMesh(mesh,texture,pose.parameters());
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float angle, float deltaTime) {
        this.doRenderItem((AC_EntityBoomerang) entity, x, y, z, angle, deltaTime);
    }
}
