package dev.adventurecraft.awakening.client.render.state;

public record RasterState(boolean depthTest, boolean depthWrite, CompareOp depthCompare, BlendState blend,
                          CullMode cull, Winding winding, ColorMask colorMask, Rect viewport, Rect scissor,
                          float minDepth, float maxDepth, float lineWidth, DepthBias depthBias) {
    public RasterState { java.util.Objects.requireNonNull(depthCompare); java.util.Objects.requireNonNull(blend); java.util.Objects.requireNonNull(cull); java.util.Objects.requireNonNull(winding); java.util.Objects.requireNonNull(colorMask); java.util.Objects.requireNonNull(depthBias); }
    public static RasterState defaults() { return new RasterState(true, true, CompareOp.LESS_OR_EQUAL, BlendState.OPAQUE, CullMode.BACK, Winding.COUNTER_CLOCKWISE, ColorMask.RGBA, null, null, 0, 1, 1, DepthBias.NONE); }
    public static RasterState ui() { return MaterialRecipe.UI.raster(defaults()); }
    public RasterState withDepth(boolean test, boolean write) { return new RasterState(test, write, depthCompare, blend, cull, winding, colorMask, viewport, scissor, minDepth, maxDepth, lineWidth, depthBias); }
    public RasterState withBlend(BlendState value) { return new RasterState(depthTest, depthWrite, depthCompare, value, cull, winding, colorMask, viewport, scissor, minDepth, maxDepth, lineWidth, depthBias); }
    public RasterState withCull(CullMode value) { return new RasterState(depthTest, depthWrite, depthCompare, blend, value, winding, colorMask, viewport, scissor, minDepth, maxDepth, lineWidth, depthBias); }
    public RasterState withScissor(Rect value) { return new RasterState(depthTest, depthWrite, depthCompare, blend, cull, winding, colorMask, viewport, value, minDepth, maxDepth, lineWidth, depthBias); }
    public RasterState withViewport(Rect value) { return new RasterState(depthTest, depthWrite, depthCompare, blend, cull, winding, colorMask, value, scissor, minDepth, maxDepth, lineWidth, depthBias); }
    public RasterState withLineWidth(float value) { return new RasterState(depthTest, depthWrite, depthCompare, blend, cull, winding, colorMask, viewport, scissor, minDepth, maxDepth, value, depthBias); }
    public RasterState withColorMask(ColorMask value) { return new RasterState(depthTest,depthWrite,depthCompare,blend,cull,winding,value,viewport,scissor,minDepth,maxDepth,lineWidth,depthBias); }
    public RasterState withWinding(Winding value) { return new RasterState(depthTest,depthWrite,depthCompare,blend,cull,value,colorMask,viewport,scissor,minDepth,maxDepth,lineWidth,depthBias); }
    public RasterState withDepthCompare(CompareOp value) { return new RasterState(depthTest,depthWrite,value,blend,cull,winding,colorMask,viewport,scissor,minDepth,maxDepth,lineWidth,depthBias); }
}
