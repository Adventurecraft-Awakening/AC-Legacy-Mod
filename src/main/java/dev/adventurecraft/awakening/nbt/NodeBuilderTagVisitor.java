package dev.adventurecraft.awakening.nbt;

import dev.adventurecraft.awakening.chat.ChatFormat;
import dev.adventurecraft.awakening.dom.*;
import dev.adventurecraft.awakening.extension.nbt.ExCompoundTag;
import dev.adventurecraft.awakening.extension.nbt.ExListTag;
import dev.adventurecraft.awakening.util.TagUtil;
import net.minecraft.nbt.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;

public class NodeBuilderTagVisitor extends BuilderTagVisitor {

    private static final int INLINE_LIST_THRESHOLD = 8;
    private static final int MAX_DEPTH = 64;
    private static final int MAX_LENGTH = 128;

    private static final Style HIGHLIGHT_KEY = ChatFormat.AQUA.toStyle();
    private static final Style HIGHLIGHT_STRING = ChatFormat.GREEN.toStyle();
    private static final Style HIGHLIGHT_NUMBER = ChatFormat.GOLD.toStyle(); // TODO: use NumberFormat?
    private static final Style HIGHLIGHT_NUMBER_TYPE = ChatFormat.RED.toStyle();

    private static final Node ITEM_SPACE = Node.text(SnbtGrammar.ITEM_SPACE);
    private static final Node NEWLINE = Node.text(SnbtGrammar.NEWLINE);
    private static final Node QUOTE = Node.text(SnbtGrammar.QUOTE);
    private static final Node ITEM_SEPARATOR = Node.text(SnbtGrammar.ITEM_SEPARATOR);

    private static final Node LIST_OPEN = Node.text(SnbtGrammar.LIST_OPEN);
    private static final Node LIST_CLOSE = Node.text(SnbtGrammar.LIST_CLOSE);
    private static final Node LIST_TYPE_SEPARATOR = Node.text(SnbtGrammar.LIST_TYPE_SEPARATOR);
    private static final Node STRUCT_OPEN = Node.text(SnbtGrammar.STRUCT_OPEN);
    private static final Node STRUCT_CLOSE = Node.text(SnbtGrammar.STRUCT_CLOSE);
    private static final Node KEY_VALUE_SEPARATOR = Node.text(SnbtGrammar.KEY_VALUE_SEPARATOR);

    private static final Node FOLDED = Node.text("<...>", ChatFormat.GRAY.toStyle());
    private static final Node TYPE_BYTE = Node.text(SnbtGrammar.TYPE_BYTE, HIGHLIGHT_NUMBER_TYPE);
    private static final Node TYPE_SHORT = Node.text(SnbtGrammar.TYPE_SHORT, HIGHLIGHT_NUMBER_TYPE);
    private static final Node TYPE_INT = Node.text(SnbtGrammar.TYPE_INT, HIGHLIGHT_NUMBER_TYPE);
    private static final Node TYPE_LONG = Node.text(SnbtGrammar.TYPE_LONG, HIGHLIGHT_NUMBER_TYPE);
    private static final Node TYPE_FLOAT = Node.text(SnbtGrammar.TYPE_FLOAT, HIGHLIGHT_NUMBER_TYPE);
    private static final Node TYPE_DOUBLE = Node.text(SnbtGrammar.TYPE_DOUBLE, HIGHLIGHT_NUMBER_TYPE);

    private final @Nullable Node indent;
    private final @Nullable Node itemSpace;
    private final @Nullable Node newline;

    private boolean sortKeys;

    private ArrayListNode result = new ArrayListNode();

    public NodeBuilderTagVisitor(@Nullable Node indent, @Nullable Node itemSpace, @Nullable Node newline) {
        super();
        this.indent = indent;
        this.itemSpace = itemSpace;
        this.newline = newline;
    }

    public NodeBuilderTagVisitor(boolean pretty) {
        this(
            TextNode.ofOrNull(pretty ? SnbtGrammar.DEFAULT_INDENT : null),
            pretty ? ITEM_SPACE : null,
            pretty ? NEWLINE : null
        );
        this.sortKeys = pretty;
    }

    public void reset() {
        this.result.clear();
    }

    public Node build() {
        return this.result;
    }

    public Node resetAndBuild(Tag tag) {
        this.reset();
        this.visit(tag);
        return this.build();
    }

    protected void appendNumber(NumberNode value, Node type) {
        this.result.append(value.withStyle(HIGHLIGHT_NUMBER)).append(type);
    }

    public @Override void visit(StringTag stringTag) {
        this.escapeString(stringTag.contents, HIGHLIGHT_STRING, this.result);
    }

    public @Override void visit(ByteTag byteTag) {
        this.appendNumber(NumberNode.of(byteTag.data), TYPE_BYTE);
    }

    public @Override void visit(ShortTag shortTag) {
        this.appendNumber(NumberNode.of(shortTag.data), TYPE_SHORT);
    }

    public @Override void visit(IntTag intTag) {
        this.appendNumber(NumberNode.of(intTag.data), TYPE_INT);
    }

    public @Override void visit(LongTag longTag) {
        this.appendNumber(NumberNode.of(longTag.data), TYPE_LONG);
    }

    public @Override void visit(FloatTag floatTag) {
        this.appendNumber(NumberNode.of(floatTag.data), TYPE_FLOAT);
    }

    public @Override void visit(DoubleTag doubleTag) {
        this.appendNumber(NumberNode.of(doubleTag.data), TYPE_DOUBLE);
    }

    public @Override void visit(ByteArrayTag byteArrayTag) {
        this.result.append(LIST_OPEN).append(TYPE_BYTE).append(LIST_TYPE_SEPARATOR);
        byte[] bs = byteArrayTag.data;

        for (int i = 0; i < bs.length && i < MAX_LENGTH; i++) {
            this.result.append(this.itemSpace);
            this.result.append(NumberNode.of(bs[i]).withStyle(HIGHLIGHT_NUMBER));
            if (i != bs.length - 1) {
                this.result.append(ITEM_SEPARATOR);
            }
        }

        if (bs.length > MAX_LENGTH) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    public @Override void visit(IntArrayTag intArrayTag) {
        this.result.append(LIST_OPEN).append(TYPE_INT).append(LIST_TYPE_SEPARATOR);
        int[] is = intArrayTag.data;

        for (int i = 0; i < is.length && i < MAX_LENGTH; i++) {
            this.result.append(this.itemSpace);
            this.result.append(NumberNode.of(is[i]).withStyle(HIGHLIGHT_NUMBER));
            if (i != is.length - 1) {
                this.result.append(ITEM_SEPARATOR);
            }
        }

        if (is.length > MAX_LENGTH) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    public @Override void visit(LongArrayTag longArrayTag) {
        this.result.append(LIST_OPEN).append(TYPE_LONG).append(LIST_TYPE_SEPARATOR);
        long[] ls = longArrayTag.data;

        for (int i = 0; i < ls.length && i < MAX_LENGTH; i++) {
            this.result.append(this.itemSpace);
            this.result.append(NumberNode.of(ls[i]).withStyle(HIGHLIGHT_NUMBER));
            if (i != ls.length - 1) {
                this.result.append(ITEM_SEPARATOR);
            }
        }

        if (ls.length > MAX_LENGTH) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    private static boolean shouldWrapListElements(ListTag listTag) {
        if (listTag.size() >= INLINE_LIST_THRESHOLD) {
            return false;
        }
        return TagUtil.isNumericType(((ExListTag) listTag).getElementType());
    }

    public @Override void visit(ListTag listTag) {
        this.result.append(LIST_OPEN);
        if (listTag.size() == 0) {
        }
        else if (this.depth() >= MAX_DEPTH) {
            this.result.append(FOLDED);
        }
        else if (!shouldWrapListElements(listTag)) {
            for (int i = 0; i < listTag.size(); i++) {
                if (i != 0) {
                    this.result.append(ITEM_SEPARATOR).append(this.itemSpace);
                }
                this.appendSubTag(listTag.get(i), false);
            }
        }
        else {
            Node prefix = null;
            if (this.indent != null) {
                this.result.append(this.newline);
                prefix = this.makeIndent(this.indentDepth() + 1);
            }

            for (int i = 0; i < listTag.size() && i < MAX_LENGTH; i++) {
                this.result.append(prefix);
                this.appendSubTag(listTag.get(i), true);

                if (i != listTag.size() - 1) {
                    this.result.append(ITEM_SEPARATOR);
                    this.result.append(prefix == null ? this.itemSpace : this.newline);
                }
            }

            if (listTag.size() > MAX_LENGTH) {
                this.result.append(prefix).append(FOLDED);
            }

            if (this.indent != null) {
                this.result.append(this.newline);
                this.result.append(this.makeIndent(this.indentDepth()));
            }
        }
        this.result.append(LIST_CLOSE);
    }

    public @Override void visit(CompoundTag compoundTag) {
        this.result.append(STRUCT_OPEN);
        if (((ExCompoundTag) compoundTag).isEmpty()) {
        }
        else if (this.depth() >= MAX_DEPTH) {
            this.result.append(FOLDED);
        }
        else {
            Node prefix = null;
            if (this.indent != null) {
                this.result.append(this.newline);
                prefix = this.makeIndent(this.indentDepth() + 1);
            }

            Collection<String> keySet = ((ExCompoundTag) compoundTag).keySet();
            Iterator<String> keys = this.sortKeys ? keySet.stream().sorted().iterator() : keySet.iterator();

            while (keys.hasNext()) {
                String key = keys.next();
                this.result.append(prefix);
                this.escapeKey(key, this.result);
                this.result.append(KEY_VALUE_SEPARATOR).append(this.itemSpace);

                Tag tag = ((ExCompoundTag) compoundTag).getTag(key);
                this.appendSubTag(tag, true);

                if (keys.hasNext()) {
                    this.result.append(ITEM_SEPARATOR);
                    this.result.append(prefix == null ? this.itemSpace : this.newline);
                }
            }

            if (this.indent != null) {
                this.result.append(this.newline);
                this.result.append(this.makeIndent(this.indentDepth()));
            }
        }
        this.result.append(STRUCT_CLOSE);
    }

    protected @Nullable Node makeIndent(int amount) {
        if (amount == 0) {
            return null;
        }
        return Node.repeat(this.indent, amount);
    }

    protected void escapeKey(String key, ListNode target) {
        this.simpleValueMatcher.reset(key);
        if (this.simpleValueMatcher.matches()) {
            target.append(Node.text(key, HIGHLIGHT_KEY));
        }
        else {
            this.escapeString(key, HIGHLIGHT_KEY, target);
        }
    }

    protected void escapeString(String value, Style style, ListNode target) {
        Node body = Node.text(SnbtGrammar.escapeString(value), style);
        target.append(QUOTE).append(body).append(QUOTE);
    }
}
