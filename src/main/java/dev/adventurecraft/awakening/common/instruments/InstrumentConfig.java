package dev.adventurecraft.awakening.common.instruments;

public class InstrumentConfig {

    public final String soundURI;
    public final int tuning;

    public InstrumentConfig(String soundURI) {
        this(soundURI, 3);
    }

    public InstrumentConfig(String soundURI, int sampleNote) {
        this.soundURI = soundURI;
        this.tuning = sampleNote;
    }
}
