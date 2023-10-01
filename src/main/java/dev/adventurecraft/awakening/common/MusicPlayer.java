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
     * @param world            The world where the note will be played
     * @param x                The x position in the world where the note will be played
     * @param y                The y position in the world where the note will be played
     * @param z                The z position in the world where the note will be played
     * @param instrumentString The instrument that will be played
     * @param songString       The string that contains the song
     * @param noteIndex        The note to be played
     * @param volume           The volume in which the song will be played
     */
    public static void playNoteFromSong(World world, double x, double y, double z, String instrumentString, String songString, int noteIndex, float volume) {
        int stringIterationIndex = 0; // Current index on the string iteration
        int noteIterationIndex = 0;  // Current note of the song
        boolean isFlat = false;
        boolean isSharp = false;
        char noteToPlay = 'A';

        float basePitchModifier;
        char iterationChar;

        // Count the octave changes before up to the current note
        for (basePitchModifier = 1.0F; noteIterationIndex <= noteIndex && stringIterationIndex < songString.length(); ++stringIterationIndex) {
            iterationChar = songString.charAt(stringIterationIndex);
            if (iterationChar == '+') { // Increase the octave of the note
                basePitchModifier *= 2.0F;
            } else if (iterationChar == '-') { // Lower the octave of the note
                basePitchModifier *= 0.5F;
            } else if (iterationChar != '#' && iterationChar != 'b') { // Ignore sharps and flats
                noteToPlay = iterationChar; // Set this as the current note
                ++noteIterationIndex;
            }
        }


        // Check the next character for a sharp or a flat (if it can exist)
        if (stringIterationIndex < songString.length()) {
            iterationChar = songString.charAt(stringIterationIndex);
            if (iterationChar == '#') {
                isSharp = true;
            } else if (iterationChar == 'b') {
                isFlat = true;
            }
        }

        // Translate all flats to sharps instead, by reducing its char value (or rolling over if it's A, the lowest ASCII).
        if (isFlat) {
            if (noteToPlay == 'A') {
                basePitchModifier *= 0.5F;
                noteToPlay = 'G';
            } else {
                --noteToPlay;
            }

            isSharp = true;
        }

        // Finally play that note
        playNote(world, x, y, z, instrumentString, noteToPlay, isSharp, basePitchModifier, volume);
    }

    /**
     * @param songString The string representation of the song
     * @return the amount of notes in the string
     */
    public static int countNotes(String songString) {
        int i = 0;

        int noteCount;
        for (noteCount = 0; i < songString.length(); ++i) {
            char currentNote = songString.charAt(i);
            if (currentNote != '+' && currentNote != '-' && currentNote != '#' && currentNote != 'b') {
                ++noteCount;
            }
        }

        return noteCount;
    }

    public static void
    playNote(World world, double x, double y, double z, String instrumentString, Note note, float volume) {
        world.playSound(x, y, z, instrumentString, volume, note.getFrequency());
    }
}
