package dev.adventurecraft.awakening.client.options;

/** The game operations an option may request after its value has changed. */
public interface OptionEffects {

    void reloadOptionTextures();

    void rebuildOptionTerrain();

    void updateOptionBrightness();

    void updateOptionWaterOpacity();

    void updateOptionAmbientOcclusion(boolean enabled, boolean rebuildTerrain);
}
