package dev.adventurecraft.awakening.client.render.compat;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.MaterialRecipe;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tesselator;

/** Only unknown external particle overrides use the inherited ABI. Native particles never mutate static origins. */
public final class LegacyParticleBridge {
    @FunctionalInterface private interface LegacyRender extends java.io.Serializable {
        void draw(Particle particle,Tesselator vertices,float tick,float x0,float y0,float z0,float x1,float z1);
    }
    private static final String RENDER=NativeDispatch.methodName((LegacyRender)Particle::render);
    private static final Class<?>[] LEGACY={Tesselator.class,float.class,float.class,float.class,float.class,float.class,float.class};
    private static final ClassValue<Boolean> NORMAL=new ClassValue<>() {
        @Override protected Boolean computeValue(Class<?> type) {
            return NativeDispatch.ownsOverride(type,RENDER,LEGACY,"ac$render",new Class<?>[]{ParticleRenderContext.class,VertexSink.class});
        }
    };
    private static final ClassValue<Boolean> LIT=new ClassValue<>() {
        @Override protected Boolean computeValue(Class<?> type) {
            return NativeDispatch.ownsOverride(type,RENDER,LEGACY,"ac$renderLit",new Class<?>[]{ParticleRenderContext.class});
        }
    };
    private LegacyParticleBridge() { }
    public static boolean nativeNormal(Particle particle) { return NORMAL.get(particle.getClass()); }
    public static boolean nativeLit(Particle particle) { return LIT.get(particle.getClass()); }
    public static void render(Particle particle,ParticleRenderContext context,RenderTexture texture,boolean lit) {
        var parameters=lit?context.parameters().withMaterial(MaterialRecipe.ENTITY_CUTOUT.material()):context.parameters();
        LegacyPassBridge.drawLegacy(parameters,texture,() -> {
            double x=Particle.xOff,y=Particle.yOff,z=Particle.zOff;
            var vertices=Tesselator.instance;var origin=(VertexSink)vertices;
            double ox=origin.getX(),oy=origin.getY(),oz=origin.getZ();
            Particle.xOff=context.x();Particle.yOff=context.y();Particle.zOff=context.z();vertices.offset(0,0,0);
            try {
                if(!lit) vertices.begin();
                particle.render(vertices,context.partialTick(),context.x0(),context.y0(),context.z0(),context.x1(),context.z1());
            } finally {
                if(vertices.tesselating) vertices.end();
                vertices.offset(ox,oy,oz);Particle.xOff=x;Particle.yOff=y;Particle.zOff=z;
            }
        });
    }
}
