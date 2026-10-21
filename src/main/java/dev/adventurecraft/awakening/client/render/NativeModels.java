package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.model.*;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;

/** Model geometry receives one explicit context; parts only append their own local pose. */
public final class NativeModels {
    private NativeModels() { }
    public static void render(Model model,ModelRenderContext context,Matrix4 pose,Color4 tint,
                              float time,float strength,float bob,float yaw,float pitch,float scale) {
        ((ExModel)model).ac$render(context,pose,tint,time,strength,bob,yaw,pitch,scale);
    }
    public static void part(ModelPart part,ModelRenderContext context,Matrix4 pose,Color4 tint,float scale,boolean rollable) {
        if(part!=null) ((ExCuboid)part).render(context,pose,tint,scale,rollable);
    }
    public static void quadruped(QuadrupedModel model,ModelRenderContext context,Matrix4 pose,Color4 tint,
                                float time,float strength,float bob,float yaw,float pitch,float scale) {
        model.setupAnim(time,strength,bob,yaw,pitch,scale);
        part(model.head,context,pose,tint,scale,false);part(model.body,context,pose,tint,scale,false);
        part(model.leg0,context,pose,tint,scale,false);part(model.leg1,context,pose,tint,scale,false);
        part(model.leg2,context,pose,tint,scale,false);part(model.leg3,context,pose,tint,scale,false);
    }
}
