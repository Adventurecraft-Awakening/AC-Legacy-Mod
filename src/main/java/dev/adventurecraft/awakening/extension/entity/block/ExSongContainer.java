package dev.adventurecraft.awakening.extension.entity.block;

import dev.adventurecraft.awakening.common.instruments.IInstrumentConfig;

public interface ExSongContainer {

    /**
     * Plays the stored song with the given instrument string.
     *
     * @param instrumentUri the instrument's uri, the sound from the files that will be played.
     */
    void playSong(IInstrumentConfig instrumentUri);

    String getSong();
}
