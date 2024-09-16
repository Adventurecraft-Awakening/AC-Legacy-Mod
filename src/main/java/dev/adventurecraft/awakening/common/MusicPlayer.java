package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.common.instruments.IInstrumentConfig;
import dev.adventurecraft.awakening.common.instruments.Note;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class MusicPlayer {
    /**
     * @param world            The world where the sound will be played
     * @param x                The X position in said world
     * @param y                The Y position in said world
     * @param z                The Z position in said world
     * @param instrumentConfig The instrument to be used to play the note
     * @param note             The note to be played
     * @param volume           The volume that the note will be played at
     */
    public static void playNote(Level world, double x, double y, double z, IInstrumentConfig instrumentConfig, Note note, float volume) {
        playNote(world, x, y, z, instrumentConfig.getSoundString(note), instrumentConfig.getPitchModifier() * note.getPitch(), instrumentConfig.getVolumeModifier() * volume);
    }

    /**
     * @param world    The world where the sound will be played
     * @param x        The X position in said world
     * @param y        The Y position in said world
     * @param z        The Z position in said world
     * @param soundURI The instrument to be used on playing the note
     * @param pitchMod The note to be played
     * @param volume   The volume that the note will be played at
     */
    public static void playNote(Level world, double x, double y, double z, String soundURI, float pitchMod, float volume) {
        world.playSound(x, y, z, soundURI, volume, pitchMod);
    }

    public static void playNoteFromEntity(Entity entity, IInstrumentConfig instrumentConfig, Note note, float volume) {
        playNote(entity.level, entity.x, entity.y, entity.z, instrumentConfig, note, volume);
    }
}
