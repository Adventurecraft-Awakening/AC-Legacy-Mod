package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.EntityMeshes;
import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.PoseStack;
import dev.adventurecraft.awakening.client.render.QuadBuilder;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.item.ItemEntity;

public class AC_RenderBomb extends ItemRenderer {

    public AC_RenderBomb() {
        this.shadowRadius = 0.35F;
    }

    public void render(ItemEntity var1, double var2, double var4, double var6, float var8, float var9) {
        int icon = var1.item.getIcon();
        var pose = new PoseStack(LegacyPassBridge.worldEntry());
        pose.translate((float)var2,(float)var4+.1f,(float)var6);
        pose.rotate(180-this.entityRenderDispatcher.playerRotY,0,1,0);
        var mesh = EntityMeshes.mesh("bomb:"+icon, () -> {
            var out = new QuadBuilder(4);
            float u=(icon%16)/16f,v=(icon/16)/16f;
            out.normal(0,1,0);
            out.vertexUV(-.5,-.25,0,u,v+1f/16); out.vertexUV(.5,-.25,0,u+1f/16,v+1f/16);
            out.vertexUV(.5,.75,0,u+1f/16,v); out.vertexUV(-.5,.75,0,u,v);
            return out.vertices();
        });
        NativeRenderer.drawMesh(mesh,
            ((dev.adventurecraft.awakening.extension.client.ExTextureManager) Minecraft.instance.textures).texture("/gui/items.png"),pose.parameters());

    }
}
