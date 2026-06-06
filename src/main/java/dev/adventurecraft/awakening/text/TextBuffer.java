package dev.adventurecraft.awakening.text;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface TextBuffer extends Appendable {

    @Override
    TextBuffer append(CharSequence text, int start, int end);

    @Override
    default TextBuffer append(CharSequence text) {
        return this.append(text, 0, text.length());
    }

    @Override
    TextBuffer append(char value);

    default TextBuffer append(int value) {
        return this.append(Integer.toString(value));
    }

    default TextBuffer append(long value) {
        return this.append(Long.toString(value));
    }

    default TextBuffer append(float value) {
        return this.append(Float.toString(value));
    }

    default TextBuffer append(double value) {
        return this.append(Double.toString(value));
    }

    default TextBuffer appendNumber(Number number) {
        return this.append(number.toString());
    }

    default TextBuffer appendCodePoint(int codePoint) {
        return this.append(Character.toString(codePoint));
    }

    static TextBuffer of(StringBuilder builder) {
        return new StringBuilderBuffer(builder);
    }

    static TextBuffer of(Appendable output) {
        return new AppendableBuffer(output);
    }

    record StringBuilderBuffer(StringBuilder builder) implements TextBuffer {

        public @Override TextBuffer append(CharSequence text, int start, int end) {
            this.builder.append(text, start, end);
            return this;
        }

        public @Override TextBuffer append(CharSequence text) {
            this.builder.append(text);
            return this;
        }

        public @Override TextBuffer append(char value) {
            this.builder.append(value);
            return this;
        }

        public @Override TextBuffer append(int value) {
            this.builder.append(value);
            return this;
        }

        public @Override TextBuffer append(long value) {
            this.builder.append(value);
            return this;
        }

        public @Override TextBuffer append(float value) {
            this.builder.append(value);
            return this;
        }

        public @Override TextBuffer append(double value) {
            this.builder.append(value);
            return this;
        }

        public @Override TextBuffer appendCodePoint(int codePoint) {
            this.builder.appendCodePoint(codePoint);
            return this;
        }
    }

    final class AppendableBuffer extends ScratchBuffer {
        private final Appendable output;

        public AppendableBuffer(Appendable output) {
            this.output = output;
        }

        public Appendable output() {
            return this.output;
        }

        public @Override TextBuffer append(CharSequence text) {
            try {
                this.output.append(text);
                return this;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public @Override TextBuffer append(CharSequence text, int start, int end) {
            try {
                this.output.append(text, start, end);
                return this;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public @Override TextBuffer append(char value) {
            try {
                this.output.append(value);
                return this;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    abstract class ScratchBuffer implements TextBuffer {
        private @Nullable StringBuilder scratch;

        public StringBuilder scratch() {
            if (this.scratch == null) {
                this.scratch = new StringBuilder();
            }
            return this.scratch;
        }

        public TextBuffer flushScratch() {
            if (this.scratch != null) {
                this.append(this.scratch);
                this.scratch.setLength(0);
            }
            return this;
        }

        public @Override TextBuffer append(int value) {
            this.scratch().append(value);
            return this.flushScratch();
        }

        public @Override TextBuffer append(long value) {
            this.scratch().append(value);
            return this.flushScratch();
        }

        public @Override TextBuffer append(float value) {
            this.scratch().append(value);
            return this.flushScratch();
        }

        public @Override TextBuffer append(double value) {
            this.scratch().append(value);
            return this.flushScratch();
        }

        public @Override TextBuffer appendCodePoint(int codePoint) {
            this.scratch().appendCodePoint(codePoint);
            return this.flushScratch();
        }
    }
}
