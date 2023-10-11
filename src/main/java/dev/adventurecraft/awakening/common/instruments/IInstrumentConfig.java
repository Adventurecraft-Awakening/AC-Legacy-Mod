package dev.adventurecraft.awakening.common.instruments;

public interface IInstrumentConfig {
    String getSoundString(Note note);

    float getPitchModifier();

    float getVolumeModifier();

    int getTuning();
}
