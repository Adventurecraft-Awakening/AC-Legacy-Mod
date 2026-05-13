package dev.adventurecraft.awakening.nbt;

import dev.adventurecraft.awakening.extension.nbt.ExTag;
import net.minecraft.nbt.Tag;

import java.util.regex.Matcher;

public abstract class BuilderTagVisitor implements TagVisitor {

    protected Matcher simpleValueMatcher;
    private int depth;
    private int indentDepth;

    public BuilderTagVisitor() {
        this.simpleValueMatcher = SnbtGrammar.SIMPLE_VALUE.matcher("");
    }

    // TODO: stack instead of recursion?
    protected void appendSubTag(Tag tag, boolean indent) {
        if (indent) {
            this.indentDepth++;
        }
        this.depth++;

        try {
            ((ExTag) tag).accept(this);
        }
        finally {
            if (indent) {
                this.indentDepth--;
            }
            this.depth--;
        }
    }

    public int depth() {
        return this.depth;
    }

    public int indentDepth() {
        return this.indentDepth;
    }
}
