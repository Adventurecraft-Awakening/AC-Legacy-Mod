package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;

/** Explicit view, material and texture shared by all parts of one model. */
public record ModelRenderContext(FrameView view, MaterialState material, RasterState raster, RenderTexture texture,Matrix4 textureTransform) {
    public ModelRenderContext(FrameView view,MaterialState material,RasterState raster,RenderTexture texture) {
        this(view,material,raster,texture,Matrix4.identity());
    }
    public static ModelRenderContext of(DrawParameters parameters,RenderTexture texture) {
        return new ModelRenderContext(parameters.view(),parameters.material(),parameters.raster(),texture,parameters.object().textureTransform());
    }
    public ModelRenderContext withTexture(RenderTexture texture) { return new ModelRenderContext(view, material, raster, texture,textureTransform); }
    public ModelRenderContext withMaterial(MaterialState material) { return new ModelRenderContext(view, material, raster, texture,textureTransform); }
    public ModelRenderContext withRaster(RasterState value) { return new ModelRenderContext(view,material,value,texture,textureTransform); }
    public DrawParameters parameters(Matrix4 model, Color4 tint) {
        var state = new DrawParameters(view, new ObjectData(model, textureTransform, tint), material, raster);
        return MaterialRecipe.model(state);
    }
    public void draw(VulkanRenderer.Mesh mesh, Matrix4 model, Color4 tint) {
        NativeRenderer.drawMesh(mesh, texture, parameters(model, tint));
    }
}
