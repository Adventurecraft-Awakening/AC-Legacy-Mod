package dev.adventurecraft.awakening.client.render.state;

public record ObjectData(Matrix4 model, Matrix4 textureTransform, Color4 tint) {
    public static final ObjectData IDENTITY = new ObjectData(Matrix4.identity(), Matrix4.identity(), Color4.WHITE);
    public ObjectData { java.util.Objects.requireNonNull(model); java.util.Objects.requireNonNull(textureTransform); java.util.Objects.requireNonNull(tint); }
    public ObjectData withModel(Matrix4 value) { return new ObjectData(value, textureTransform, tint); }
    public ObjectData withTint(Color4 value) { return new ObjectData(model, textureTransform, value); }
    public ObjectData withTextureTransform(Matrix4 value) { return new ObjectData(model, value, tint); }
}
