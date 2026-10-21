package dev.adventurecraft.awakening.client.render.state;

public record DrawParameters(FrameView view, ObjectData object, MaterialState material, RasterState raster) {
    public DrawParameters { java.util.Objects.requireNonNull(view); java.util.Objects.requireNonNull(object); java.util.Objects.requireNonNull(material); java.util.Objects.requireNonNull(raster); }
    public DrawParameters withView(FrameView value) { return new DrawParameters(value, object, material, raster); }
    public DrawParameters withObject(ObjectData value) { return new DrawParameters(view, value, material, raster); }
    public DrawParameters withMaterial(MaterialState value) { return new DrawParameters(view, object, value, raster); }
    public DrawParameters withRaster(RasterState value) { return new DrawParameters(view, object, material, value); }
}
