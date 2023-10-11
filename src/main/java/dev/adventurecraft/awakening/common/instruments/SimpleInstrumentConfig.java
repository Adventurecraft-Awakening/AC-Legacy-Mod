package dev.adventurecraft.awakening.common.instruments;

public class SimpleInstrumentConfig implements IInstrumentConfig {

    public final String soundURI;
    public final int tuning;

    public SimpleInstrumentConfig(String soundURI) {
        this(soundURI, 3);
    }

    public SimpleInstrumentConfig(String soundURI, int sampleNote) {
        this.soundURI = soundURI;
        this.tuning = sampleNote;
    }

    @Override
    public String getSoundString(Note note) {
        return soundURI;
    }

    @Override
    public float getPitchModifier() {
        return 1;
    }

    @Override
    public float getVolumeModifier() {
        return 1;
    }

    @Override
    public int getTuning() {
        return tuning;
    }
}
