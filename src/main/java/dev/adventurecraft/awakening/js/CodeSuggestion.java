package dev.adventurecraft.awakening.js;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import dev.adventurecraft.awakening.dom.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CodeSuggestion extends Suggestion {

    private final double score;
    private final Node value;

    public CodeSuggestion(StringRange range, String text, Node value, double score) {
        super(range, text);
        this.value = value;
        this.score = score;
    }

    public CodeSuggestion(StringRange range, String text, Node value) {
        this(range, text, value, 1.0);
    }

    public double score() {
        return this.score;
    }

    public Node value() {
        return this.value;
    }

    public @Override int compareTo(@NotNull Suggestion other) {
        if (other instanceof CodeSuggestion o) {
            return Double.compare(this.score, o.score);
        }
        return super.compareTo(other);
    }

    public @Override boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof CodeSuggestion o) {
            return this.score == o.score && super.equals(o);
        }
        return false;
    }

    public @Override int hashCode() {
        return Objects.hash(super.hashCode(), this.score);
    }

    public @Override String toString() {
        return "CodeSuggestion{range=%s, text=%s, score=%s, tooltip=%s}".formatted(
            this.getRange(),
            this.getText(),
            this.score,
            this.getTooltip()
        );
    }
}
