package dev.adventurecraft.awakening.client.render.shader.sources;

public final class NullShaderpackSource extends ShaderpackSource {

    public static final NullShaderpackSource instance = new NullShaderpackSource();

    private NullShaderpackSource() {
    }

    @Override
    public String getName() {
        return "internal" + TypeSeparator + "OFF";
    }

    @Override
    public String getType() {
        return "internal";
    }
}
