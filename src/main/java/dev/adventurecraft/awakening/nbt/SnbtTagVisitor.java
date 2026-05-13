package dev.adventurecraft.awakening.nbt;

import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import net.minecraft.nbt.*;

import java.util.Collection;
import java.util.Iterator;

import static dev.adventurecraft.awakening.nbt.SnbtGrammar.*;

public class SnbtTagVisitor extends BuilderTagVisitor {

    private final String indent;
    private StringBuilder result = new StringBuilder();

    public SnbtTagVisitor(String indent) {
        super();
        this.indent = indent;
    }

    public SnbtTagVisitor() {
        this(DEFAULT_INDENT);
    }

    public void reset() {
        this.result.setLength(0);
    }

    public String build() {
        return this.result.toString();
    }

    public String resetAndBuild(Tag tag) {
        this.reset();
        this.visit(tag);
        return this.build();
    }

    public @Override void visit(StringTag stringTag) {
        this.result.append(QUOTE);
        escapeString(stringTag.contents, this.result);
        this.result.append(QUOTE);
    }

    public @Override void visit(ByteTag byteTag) {
        this.result.append(byteTag.data).append(TYPE_BYTE);
    }

    public @Override void visit(ShortTag shortTag) {
        this.result.append(shortTag.data).append(TYPE_SHORT);
    }

    public @Override void visit(IntTag intTag) {
        this.result.append(intTag.data);
    }

    public @Override void visit(LongTag longTag) {
        this.result.append(longTag.data).append(TYPE_LONG);
    }

    public @Override void visit(FloatTag floatTag) {
        this.result.append(floatTag.data).append(TYPE_FLOAT);
    }

    public @Override void visit(DoubleTag doubleTag) {
        this.result.append(doubleTag.data).append(TYPE_DOUBLE);
    }

    public @Override void visit(ByteArrayTag byteArrayTag) {
        this.result.append(LIST_OPEN).append(TYPE_BYTE).append(LIST_TYPE_SEPARATOR);
        byte[] bs = byteArrayTag.data;

        for (int i = 0; i < bs.length; i++) {
            this.result.append(ITEM_SPACE).append(bs[i]).append(TYPE_BYTE);
            if (i != bs.length - 1) {
                this.result.append(ITEM_SEPARATOR);
            }
        }
        this.result.append(LIST_CLOSE);
    }

    public @Override void visit(IntArrayTag intArrayTag) {
        this.result.append(LIST_OPEN).append(TYPE_INT).append(LIST_TYPE_SEPARATOR);
        int[] is = intArrayTag.data;

        for (int i = 0; i < is.length; i++) {
            this.result.append(ITEM_SPACE).append(is[i]);
            if (i != is.length - 1) {
                this.result.append(ITEM_SEPARATOR);
            }
        }
        this.result.append(LIST_CLOSE);
    }

    public @Override void visit(LongArrayTag longArrayTag) {
        this.result.append(LIST_OPEN).append(TYPE_LONG).append(LIST_TYPE_SEPARATOR);
        long[] ls = longArrayTag.data;

        for (int i = 0; i < ls.length; i++) {
            this.result.append(ITEM_SPACE).append(ls[i]).append(TYPE_LONG);
            if (i != ls.length - 1) {
                this.result.append(ITEM_SEPARATOR);
            }
        }
        this.result.append(LIST_CLOSE);
    }

    public @Override void visit(ListTag listTag) {
        this.result.append(LIST_OPEN);
        if (listTag.size() == 0) {
        }
        else {
            if (!this.indent.isEmpty()) {
                this.result.append(NEWLINE);
            }

            for (int i = 0; i < listTag.size(); i++) {
                this.appendIndent(this.indentDepth() + 1);
                this.appendSubTag(listTag.get(i), true);

                if (i != listTag.size() - 1) {
                    this.result.append(ITEM_SEPARATOR);
                    this.result.append(this.indent.isEmpty() ? ITEM_SPACE : NEWLINE);
                }
            }

            if (!this.indent.isEmpty()) {
                this.result.append(NEWLINE);
                this.appendIndent(this.indentDepth());
            }
        }
        this.result.append(LIST_CLOSE);
    }

    public @Override void visit(CompoundTag compoundTag) {
        this.result.append(STRUCT_OPEN);
        if (((ExCompoundTag) compoundTag).isEmpty()) {
        }
        else {
            if (!this.indent.isEmpty()) {
                this.result.append(NEWLINE);
            }

            Collection<String> keySet = ((ExCompoundTag) compoundTag).keySet();
            Iterator<String> keys = keySet.iterator();

            while (keys.hasNext()) {
                String key = keys.next();
                this.appendIndent(this.indentDepth() + 1);
                this.escapeKey(key, this.result);
                this.result.append(KEY_VALUE_SEPARATOR).append(ITEM_SPACE);

                Tag tag = ((ExCompoundTag) compoundTag).getTag(key);
                this.appendSubTag(tag, true);

                if (keys.hasNext()) {
                    this.result.append(ITEM_SEPARATOR);
                    this.result.append(this.indent.isEmpty() ? ITEM_SPACE : NEWLINE);
                }
            }

            if (!this.indent.isEmpty()) {
                this.result.append(NEWLINE);
                this.appendIndent(this.indentDepth());
            }
        }
        this.result.append(STRUCT_CLOSE);
    }

    protected void appendIndent(int depth) {
        //noinspection StringRepeatCanBeUsed: avoid the crazy alloc
        for (int i = 0; i < depth; i++) {
            this.result.append(this.indent);
        }
    }

    protected void escapeKey(String key, StringBuilder output) {
        this.simpleValueMatcher.reset(key);
        if (this.simpleValueMatcher.matches()) {
            output.append(key);
        }
        else {
            escapeString(key, output);
        }
    }

    public @Override void visit(EndTag endTag) {
    }
}
