package dev.adventurecraft.awakening.client.render.compat;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.model.ExModel;
import net.minecraft.client.model.Model;

/** A single boundary for inherited renderer hooks; native model parts never read ambient state. */
public final class LegacyModelBridge {
    @FunctionalInterface private interface LegacyRender extends java.io.Serializable {
        void draw(Model model,float time,float strength,float bob,float yaw,float pitch,float scale);
    }
    private static final String RENDER=NativeDispatch.methodName((LegacyRender)Model::render);
    private static final ClassValue<Boolean> NATIVE=new ClassValue<>() {
        @Override protected Boolean computeValue(Class<?> type) {
            return NativeDispatch.ownsOverride(type,RENDER,new Class<?>[]{float.class,float.class,float.class,float.class,float.class,float.class},
                "ac$render",new Class<?>[]{ModelRenderContext.class,Matrix4.class,Color4.class,float.class,float.class,float.class,float.class,float.class,float.class});
        }
    };
    private static Matrix4 inverseViewSource;
    private static Matrix4 inverseView;
    private LegacyModelBridge() { }
    public record Entry(ModelRenderContext context,Matrix4 pose,Color4 tint) { }
    public static Entry entry() {
        var captured=LegacyPassBridge.capture();
        var view=captured.view(); var pose=captured.object().model();
        if(RenderScene.hasWorld() && RenderSystem.phase()!=RenderPhase.UI && RenderSystem.phase()!=RenderPhase.HANDS) {
            var world=RenderScene.world();
            if(inverseViewSource!=world.view()) {
                double[] inverse=RenderMath.inverse(world.view());float[] values=new float[16];
                for(int i=0;i<16;i++) values[i]=(float)inverse[i];
                inverseView=new Matrix4(values);inverseViewSource=world.view();
            }
            pose=inverseView.multiply(captured.view().view().multiply(pose));
            view=world.withFog(captured.view().fog()).withLights(captured.view().lights());
        }
        return new Entry(new ModelRenderContext(view,captured.material(),captured.raster(),LegacyPassBridge.currentTexture(),captured.object().textureTransform()),
            pose,captured.object().tint());
    }
    public static void render(Model model,float time,float strength,float bob,float yaw,float pitch,float scale) {
        if(!((ExModel)model).ac$nativeModel() || !NATIVE.get(model.getClass())) { model.render(time,strength,bob,yaw,pitch,scale);return; }
        var entry=entry();
        NativeModels.render(model,entry.context(),entry.pose(),entry.tint(),time,strength,bob,yaw,pitch,scale);
    }
}
