package dev.adventurecraft.awakening.common.instruments;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list of notes that can be played.
 */
public class Song implements Iterable<Note> {

    private static final char SHARP = '#';
    private static final char FLAT = 'b';
    private static final char RAISE_OCTAVE = '+';
    private static final char LOWER_OCTAVE = '-';

    /**
     * A list of notes. If a given note is null it is not meant to be played.
     */
    private final List<Note> notes;

    public Song(String sourceString) {
        this(sourceString, 0);
    }

    public Song(String sourceString, int baseShift) {
        // Initialize the list
        this.notes = new ArrayList<>();
        int currentOctave = 0;
        int noteCount = 0;
        // Loop through the chars of the song
        for (int i = 0; i < sourceString.length(); i++) {
            char currentChar = sourceString.charAt(i);

            if (charIsANote(currentChar)) {
                Note newNote = new Note(currentChar, currentOctave);
                newNote.ShiftValue(baseShift);
                this.notes.add(newNote);
                noteCount++;
            } else {
                switch (currentChar) {
                    case RAISE_OCTAVE -> currentOctave += 1;
                    case LOWER_OCTAVE -> currentOctave -= 1;
                    case SHARP -> {
                        if (noteCount > 0) {
                            this.notes.get(noteCount - 1).ShiftValue(1);
                        }
                    }
                    case FLAT -> {
                        if (noteCount > 0) {
                            this.notes.get(noteCount - 1).ShiftValue(-1);
                        }
                    }
                    default -> this.notes.add(null);
                }
            }
        }
    }

    /**
     * @param checkChar The char to be checked
     * @return Whether the character represents a note (A-G)
     */
    private boolean charIsANote(char checkChar) {
        return checkChar >= 'A' && checkChar <= 'G';
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<Note> iterator() {
        return notes.iterator();
    }
}
