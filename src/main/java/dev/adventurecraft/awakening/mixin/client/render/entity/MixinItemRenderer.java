package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.state.MaterialRecipe;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.util.Mth;
import java.util.Random;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.Rect;
import dev.adventurecraft.awakening.client.WikiAssetExporter;
import dev.adventurecraft.awakening.item.AC_IItemReload;
import dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer extends EntityRenderer implements ExItemRenderer {

    @Shadow public boolean field_1707;
    @Shadow private Random random;

    public float scale = 1.0F;

    @Unique private boolean ac$blockPreview(int id) {
        if(id>=256) return false;
        int shape=Tile.tiles[id].getRenderShape();
        return BlockMeshes.standard3D(shape) || WikiAssetExporter.isEnabled() && BlockMeshes.custom3D(shape);
    }

    @Overwrite
    public void render(ItemEntity entity,double x,double y,double z,float yaw,float tick) {
        this.random.setSeed(187);
        var stack=entity.item;
        var parameters=LegacyPassBridge.worldEntry();
        var pose=new PoseStack(parameters);
        float bob=Mth.sin((entity.age+tick)/10+entity.bobOffs)*.1F+.1F;
        float spin=((entity.age+tick)/20+entity.bobOffs)*57.295776F;
        int copies=stack.count>20?4:stack.count>5?3:stack.count>1?2:1;
        pose.translate((float)x,(float)y+bob,(float)z);
        var textures=(ExTextureManager)Minecraft.instance.textures;
        var texture=textures.texture(ItemPreviewRenderer.atlas(stack.id));
        if(stack.id<256 && BlockMeshes.standard3D(Tile.tiles[stack.id].getRenderShape())) {
            var tile=Tile.tiles[stack.id];
            float scale=!tile.isCubeShaped() && stack.id!=Tile.SLAB.id && tile.getRenderShape()!=16?.5F:.25F;
            pose.rotate(spin,0,1,0);pose.scale(scale*this.scale,scale*this.scale,scale*this.scale);
            float brightness=entity.getBrightness(tick);
            pose.tint(brightness,brightness,brightness,1);
            var mesh=BlockMeshes.inventory(tile,stack.getAuxValue(),true);
            for(int i=0;i<copies;i++) {
                pose.push();
                if(i>0) pose.translate((this.random.nextFloat()*2-1)*.2F/scale,
                    (this.random.nextFloat()*2-1)*.2F/scale,(this.random.nextFloat()*2-1)*.2F/scale);
                NativeRenderer.drawMesh(mesh,texture,pose.parameters());pose.pop();
            }
        } else {
            pose.scale(.5F*this.scale,.5F*this.scale,.5F*this.scale);
            if(this.field_1707) {
                int color=Item.items[stack.id].getItemColor(stack.getAuxValue());float brightness=entity.getBrightness(tick);
                pose.tint((color>>16&255)/255F*brightness,(color>>8&255)/255F*brightness,(color&255)/255F*brightness,1);
            }
            int icon=stack.getIcon();
            var mesh=EntityMeshes.mesh("item-billboard:"+icon,() -> {
                var out=new QuadBuilder(4);float u=(icon%16)/16F,v=(icon/16)/16F;out.normal(0,1,0);
                out.vertexUV(-.5,-.25,0,u,v+1F/16);out.vertexUV(.5,-.25,0,u+1F/16,v+1F/16);
                out.vertexUV(.5,.75,0,u+1F/16,v);out.vertexUV(-.5,.75,0,u,v);return out.vertices();
            });
            for(int i=0;i<copies;i++) {
                pose.push();
                if(i>0) pose.translate((this.random.nextFloat()*2-1)*.3F,(this.random.nextFloat()*2-1)*.3F,(this.random.nextFloat()*2-1)*.3F);
                pose.rotate(180-this.entityRenderDispatcher.playerRotY,0,1,0);
                NativeRenderer.drawMesh(mesh,texture,pose.parameters());pose.pop();
            }
        }
    }

    @Overwrite
    public void renderGuiItem(Font font,Textures textures,int id,int metadata,int icon,int x,int y) {
        var parameters=MaterialRecipe.UI.apply(LegacyPassBridge.capture(false));
        try(var ui=LegacyPassBridge.uiContext(parameters,font,textures)) {
            ItemPreviewRenderer.render(ui,id,metadata,icon,x,y,this.field_1707,this.ac$blockPreview(id));
        }
    }

    @Override
    public void renderGuiItem(UiDrawContext ui,ItemInstance stack,int x,int y) {
        if(stack!=null) ItemPreviewRenderer.render(ui,stack.id,stack.getAuxValue(),stack.getIcon(),x,y,
            this.field_1707,this.ac$blockPreview(stack.id));
    }

    @Override
    public void renderGuiItemDecorations(UiDrawContext ui,ItemInstance stack,int x,int y) {
        ItemPreviewRenderer.decorations(ui,stack,x,y);
    }

    @Overwrite
    public void renderAndDecorateItem(Font textRenderer, Textures texManager, ItemInstance stack, int x, int y) {
        if (stack != null && Item.items[stack.id] != null) {
            this.renderGuiItem(textRenderer, texManager, stack.id, stack.getAuxValue(), stack.getIcon(), x, y);
        }
    }

    @Overwrite
    public void renderGuiItemDecorations(Font textRenderer, Textures texManager, ItemInstance stack, int x, int y) {
        if (stack == null) {
            return;
        }
        Item item = Item.items[stack.id];
        if (item == null) {
            return;
        }

        boolean drawDurability = stack.isDamaged() || item instanceof AC_IItemReload;
        if (stack.count == 0 && !drawDurability) {
            return;
        }

        // Slot coordinates are local to the inherited item's current pose. A
        // fresh screen-root UI state would lose the container/scroll offset.
        var parameters = MaterialRecipe.UI.apply(LegacyPassBridge.capture(false));
        try (var ui = LegacyPassBridge.uiContext(parameters, textRenderer, texManager)) {
            ItemPreviewRenderer.decorations(ui,stack,x,y);
        }
    }

    @Override
    public void setScale(float value) {
        this.scale = value;
    }
}
