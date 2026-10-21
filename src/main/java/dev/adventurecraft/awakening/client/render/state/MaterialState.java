package dev.adventurecraft.awakening.client.render.state;

public record MaterialState(CompareOp alphaCompare, float alphaReference, boolean lighting, ColorMaterial colorMaterial,
                            MaterialEffect effect) {
    public static final MaterialState UNLIT = new MaterialState(CompareOp.ALWAYS, 0, false, ColorMaterial.NONE);

    public MaterialState(CompareOp alphaCompare, float alphaReference, boolean lighting, ColorMaterial colorMaterial) {
        this(alphaCompare, alphaReference, lighting, colorMaterial, MaterialEffect.NORMAL);
    }

    public MaterialState {
        java.util.Objects.requireNonNull(alphaCompare);
        java.util.Objects.requireNonNull(colorMaterial);
        java.util.Objects.requireNonNull(effect);
    }

    public MaterialState withLighting(boolean value) { return new MaterialState(alphaCompare, alphaReference, value, colorMaterial, effect); }
    public MaterialState withAlpha(CompareOp compare, float reference) { return new MaterialState(compare, reference, lighting, colorMaterial, effect); }
    public MaterialState withColorMaterial(ColorMaterial value) { return new MaterialState(alphaCompare, alphaReference, lighting, value, effect); }
    public MaterialState withEffect(MaterialEffect value) { return new MaterialState(alphaCompare, alphaReference, lighting, colorMaterial, value); }
}
