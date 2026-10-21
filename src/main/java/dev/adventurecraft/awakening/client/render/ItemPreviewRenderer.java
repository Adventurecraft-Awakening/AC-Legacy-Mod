package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.IntCorner;
import dev.adventurecraft.awakening.layout.Rect;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.tile.Tile;

/** Explicit item UI drawing, including isolated depth and preview lighting for 3D blocks. */
public final class ItemPreviewRenderer {
    private ItemPreviewRenderer() { }
    public static String atlas(int item) {
        if(item>=256) return "/gui/items.png";
        int number=((ExBlock)Tile.tiles[item]).getTextureNum();
        return number==0?"/terrain.png":"/terrain%d.png".formatted(number);
    }
    public static void render(UiDrawContext ui,int id,int metadata,int icon,int x,int y,boolean colored,boolean block) {
        if(id<=0 || Item.items[id]==null) return;
        var texture=ui.texture(atlas(id));
        if(block) {
            ui.flush();
            var parameters=ui.parameters();
            var root=parameters.object().model().translated((float)ui.offsetX(),(float)ui.offsetY(),0);
            parameters=HandLighting.apply(parameters,parameters.view().view().multiply(root).rotated(120,1,0,0));
            var model=root.translated(x-2,y+3,-3).scaled(10,10,10).translated(1,.5F,1)
                .scaled(1,1,-1).rotated(210,1,0,0).rotated(45,0,1,0).rotated(-90,0,1,0);
            parameters=MaterialRecipe.MODEL_PREVIEW.apply(parameters)
                .withObject(parameters.object().withModel(model).withTint(Color4.WHITE))
                .withRaster(MaterialRecipe.MODEL_PREVIEW.raster(parameters.raster()).withCull(CullMode.BACK));
            NativeRenderer.drawMesh(BlockMeshes.inventory(Tile.tiles[id],metadata,colored),texture,parameters);
        } else if(icon>=0) {
            int color=colored?Rgba.fromBgra(Item.items[id].getItemColor(metadata)|0xff000000):Rgba.WHITE;
            var sprites=ui.withParameters(MaterialRecipe.UI_CUTOUT.apply(ui.parameters()));
            sprites.image(texture,new Rect(x,y,16,16),new Rect((icon%16)/16.0,(icon/16)/16.0,1.0/16,1.0/16),new IntCorner(color));
        }
    }
    public static void decorations(UiDrawContext ui,ItemInstance stack,int x,int y) {
        if(stack==null || Item.items[stack.id]==null) return;
        String count=stack.count>1?String.valueOf(stack.count):stack.count<0?"\u00ec":null;
        if(count!=null) ui.text(count,x+17-ui.font().width(count),y+9,Rgba.WHITE,true);
        if(stack.isDamaged() || Item.items[stack.id] instanceof dev.adventurecraft.awakening.item.AC_IItemReload) {
            double damage=stack.getDamageValue(), maximum=stack.getMaxDamage();
            int width=(int)Math.round(13-damage*13/maximum), green=(int)Math.round(255-damage*255/maximum);
            ui.fill(new Rect(x+2,y+13,13,2),Rgba.BLACK);
            ui.fill(new Rect(x+2,y+13,12,1),Rgba.fromRgb8((255-green)/4,63,0));
            ui.fill(new Rect(x+2,y+13,width,1),Rgba.fromRgb8(255-green,green,0));
        }
    }
}
