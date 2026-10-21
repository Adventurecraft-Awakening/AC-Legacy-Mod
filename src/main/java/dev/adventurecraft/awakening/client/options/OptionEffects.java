package dev.adventurecraft.awakening.client.options;

/** The game operations an option may request after its value has changed. */
public interface OptionEffects {

    void reloadOptionTextures();

    void rebuildOptionTerrain();

    void updateOptionBrightness();

    void updateOptionRayTracing(boolean enabled);

    void updateOptionIndirectLighting(int mode);

    void updateOptionTemporalAa(boolean enabled);

    void updateOptionRenderScale(int mode);

    void updateOptionParallaxMapping(int mode);

    void updateOptionRayReconstruction(int mode);

    void updateOptionUpscaler(int mode);

    void updateOptionFrameGeneration(int mode);

    void updateOptionWaterOpacity();

    void updateOptionAmbientOcclusion(boolean enabled, boolean rebuildTerrain);
}
