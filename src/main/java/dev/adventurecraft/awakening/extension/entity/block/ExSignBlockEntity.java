package dev.adventurecraft.awakening.extension.entity.block;

public interface ExSignBlockEntity {

    /**
     * Plays the stored song with the given instrument string.
     *
     * @param instrumentString the instrument string, the sound from the files that will be played.
     */
    void playSong(String instrumentString);
}
