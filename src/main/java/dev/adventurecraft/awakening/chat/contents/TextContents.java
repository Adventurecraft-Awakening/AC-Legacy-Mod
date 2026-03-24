package dev.adventurecraft.awakening.chat.contents;

import dev.adventurecraft.awakening.chat.Contents;
import dev.adventurecraft.awakening.chat.StyledText;
import dev.adventurecraft.awakening.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface TextContents extends Contents {

    TextContents EMPTY = new TextContents() {
        public @Override String text() {
            return "";
        }

        public @Override String toString() {
            return "empty";
        }
    };

    String text();

    static TextContents create(String string) {
        return string.isEmpty() ? EMPTY : new LiteralContents(string);
    }

    record LiteralContents(String text) implements TextContents {
        public @Override <T> Optional<T> visit(StyledText.Consumer<T> consumer) {
            return consumer.accept(this.text);
        }

        public @Override <T> Optional<T> visit(StyledText.StyledConsumer<T> consumer, Style style) {
            return consumer.accept(this.text, style);
        }

        public @Override @NotNull String toString() {
            return "literal{" + this.text + "}";
        }
    }
}
