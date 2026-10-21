package dev.adventurecraft.awakening.client.render.state;

/** Canonical native material/raster policies, also used for pipeline warmup. */
public enum MaterialRecipe {
    TERRAIN_OPAQUE(cutout(false,ColorMaterial.NONE),true,true,BlendState.OPAQUE,CullMode.BACK,true),
    TERRAIN_TRANSLUCENT(cutout(false,ColorMaterial.NONE),true,true,BlendState.ALPHA,CullMode.NONE,true),
    ENTITY_CUTOUT(cutout(true,ColorMaterial.AMBIENT_AND_DIFFUSE),true,true,BlendState.OPAQUE,CullMode.BACK,true),
    ENTITY_TRANSLUCENT(cutout(true,ColorMaterial.AMBIENT_AND_DIFFUSE),true,true,BlendState.ALPHA,CullMode.BACK,true),
    UI(MaterialState.UNLIT,false,false,BlendState.ALPHA,CullMode.NONE,false),
    UI_CUTOUT(cutout(false,ColorMaterial.NONE),false,false,BlendState.OPAQUE,CullMode.NONE,false),
    UI_INVERT(cutout(false,ColorMaterial.NONE),false,false,
        new BlendState(true,BlendFactor.ONE_MINUS_DST_COLOR,BlendFactor.ONE_MINUS_SRC_COLOR,Color4.TRANSPARENT),CullMode.NONE,false),
    MODEL_PREVIEW(cutout(true,ColorMaterial.AMBIENT_AND_DIFFUSE),true,true,BlendState.OPAQUE,CullMode.NONE,false),
    DEBUG_LINES(MaterialState.UNLIT,true,false,BlendState.ALPHA,CullMode.NONE,true),
    WEATHER(new MaterialState(CompareOp.GREATER,.01F,false,ColorMaterial.NONE),true,true,BlendState.ALPHA,CullMode.NONE,true),
    SKY(MaterialState.UNLIT,true,false,BlendState.OPAQUE,CullMode.BACK,true);

    private final MaterialState material;
    private final boolean depthTest,depthWrite;
    private final BlendState blend;
    private final CullMode cull;
    private final boolean supportsFog;

    MaterialRecipe(MaterialState material,boolean depthTest,boolean depthWrite,BlendState blend,CullMode cull,boolean supportsFog) {
        this.material=material; this.depthTest=depthTest; this.depthWrite=depthWrite;
        this.blend=blend; this.cull=cull; this.supportsFog=supportsFog;
    }
    private static MaterialState cutout(boolean lighting,ColorMaterial tracking) {
        return new MaterialState(CompareOp.GREATER,.1F,lighting,tracking);
    }
    public MaterialState material() { return material; }
    public boolean supportsFog() { return supportsFog; }
    public RasterState raster(RasterState context) {
        return context.withDepth(depthTest,depthWrite).withDepthCompare(CompareOp.LESS_OR_EQUAL).withBlend(blend).withCull(cull);
    }
    public DrawParameters apply(DrawParameters context) {
        var view=supportsFog?context.view():context.view().withFog(FogState.NONE);
        return new DrawParameters(view,context.object(),material,raster(context.raster()));
    }
    /** Animated models keep their caller's lighting, clipping and culling overrides. */
    public static DrawParameters model(DrawParameters context) {
        return context.object().tint().a()<1
            ? context.withRaster(context.raster().withBlend(ENTITY_TRANSLUCENT.blend)) : context;
    }
}
