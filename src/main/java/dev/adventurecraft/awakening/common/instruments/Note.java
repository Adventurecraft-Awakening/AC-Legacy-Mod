package dev.adventurecraft.awakening.common.instruments;

/**
 * Represents a frequency of a standard note.
 * A note of value 3 and octave 4 is the middle C.
 */
public class Note {

    /**
     * Precalculated values 0 to 11 of note coefficients.
     * <a href="https://en.wikipedia.org/wiki/Twelfth_root_of_two">Wikipedia Link</a>
     */
    private static final double[] noteCoefficient = {
        1,
        1.0594630943592952646D,
        1.122462048309372981514422431964D,
        1.1892071150027210668460489434449D,
        1.25992104989487316494880113073D,
        1.3348398541700343650713174535376D,
        1.4142135623730950491074314305706D,
        1.4983070768766814991771910314151D,
        1.5874010519681994752092850851889D,
        1.6817928305074290866076380551132D,
        1.7817974362806786101224718638408D,
        1.8877486253633869940320418208842D,
    };

    /**
     * A list that maps the characters 'A' through 'G' to a base 0 note.
     */
    private static final int[] charToNote = {0, 2, 3, 5, 7, 8, 10};

    private int value;

    /**
     * The octave of the note.
     */
    public int octave;

    private static final int baseOctave = 4;

    /**
     * Creates a note starting out from the 4th octave.
     *
     * @param value The value of the note.
     */
    public Note(int value) {
        this(value, baseOctave);
    }


    /**
     * @param sourceChar The note's source character. <code>'A'</code> for 0.
     *                   Invalid chars are set to 0.
     */
    public Note(char sourceChar, int octave) {
        this((sourceChar >= 'A' && sourceChar <= 'G') ? (charToNote[sourceChar - 'A']) : 0, octave);
    }

    public Note(int value, int octave) {
        this.octave = octave;
        this.setValue(value);
    }


    /**
     * @return The change in pitch of the note to be played
     */
    public float getPitch() {
        float product = (float) Math.pow(2, octave);
        return (float) (noteCoefficient[this.getValue()] * product);
    }

    public void shiftValue(int amount) {
        this.setValue(this.getValue() + amount);
    }

    public Note withShiftedValue(int amount) {
        return new Note(this.getValue() + amount, this.octave);
    }

    /**
     * The value of the note.
     * 0 for A
     * 1 for A#
     * 2 for D
     * 10 for G
     * and 11 for G#
     */
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        while (value < 0) {
            value += 12;
            octave -= 1;
        }
        while (value > 11) {
            value -= 12;
            octave += 1;
        }
        this.value = value;
    }
}
