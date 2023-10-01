package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.common.instruments.Note;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class MusicPlayer {

    /**
     * @param world             The world in which the note will be played
     * @param sourceEntity      The entity from which the location will be taken to play the note
     * @param instrumentString  The instrument name from which the sound will be taken
     * @param noteChar          The note character from A to G
     * @param isSharp           Whether the note should be played half a step higher
     * @param basePitchModifier How much should the pitch be altered for the given note
     * @param volume            The volume of this note
     */
    public static void playNoteFromEntity(World world, Entity sourceEntity, String instrumentString, char noteChar, boolean isSharp, float basePitchModifier, float volume) {
        playNote(world, sourceEntity.x, sourceEntity.y, sourceEntity.z, instrumentString, noteChar, isSharp, basePitchModifier, volume);
    }

    /**
     * @param world             The world in which the note will be played
     * @param x                 The X position of the sound
     * @param y                 The Y position of the sound
     * @param z                 The Z position of the sound
     * @param instrumentString  The instrument name from which the sound will be taken
     * @param noteChar          The note character from A to G
     * @param isSharp           Whether the note should be played half a step higher
     * @param basePitchModifier How much should the pitch be altered for the given note
     * @param volume            The volume of this note
     */
    public static void playNote(World world, double x, double y, double z, String instrumentString, char noteChar, boolean isSharp, float basePitchModifier, float volume) {
        float noteFrequency = 1.189207F; // Mod to F# to be A
        switch (noteChar) { // Mod to A to be whatever note was played
            case 'A':
                break;
            case 'B':
                noteFrequency *= 1.122462F;
                break;
            case 'C':
                noteFrequency *= 1.189207F;
                break;
            case 'D':
                noteFrequency *= 1.33484F;
                break;
            case 'E':
                noteFrequency *= 1.498307F;
                break;
            case 'F':
                noteFrequency *= 1.587401F;
                break;
            case 'G':
                noteFrequency *= 1.781797F;
                break;
            default:
                return;
        }

        if (isSharp) {
            noteFrequency = (float) ((double) noteFrequency * 1.059463D); // Mod to the note that was played to give it half a step upwards
        }

        world.playSound(x, y, z, instrumentString, volume, noteFrequency * basePitchModifier);
    }

    /**
     * @param world            The world where the sound will be played
     * @param x                The X position in said world
     * @param y                The Y position in said world
     * @param z                The Z position in said world
     * @param instrumentString The sound uri to be used
     * @param note             The note to be played
     * @param volume           The volume that the note will be played at
     */
    public static void playNote(World world, double x, double y, double z, String instrumentString, Note note, float volume) {
        world.playSound(x, y, z, instrumentString, volume, note.getFrequency());
    }
}
