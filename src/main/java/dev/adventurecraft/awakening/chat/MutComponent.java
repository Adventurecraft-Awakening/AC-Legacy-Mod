package dev.adventurecraft.awakening.chat;

import dev.adventurecraft.awakening.util.StringUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MutComponent implements Component {

    private Contents contents;
    private List<Component> siblings;
    private Style style;

    public MutComponent(Contents contents, List<Component> siblings, Style style) {
        this.contents = contents;
        this.siblings = siblings;
        this.style = style;
    }

    public static MutComponent create(Contents contents) {
        return new MutComponent(contents, new ArrayList<>(), Style.EMPTY);
    }

    public @Override Contents getContents() {
        return this.contents;
    }

    public @Override List<Component> getSiblings() {
        return this.siblings;
    }

    public @Override Style getStyle() {
        return this.style;
    }

    public MutComponent setStyle(Style style) {
        this.style = style;
        return this;
    }

    public MutComponent withStyle(Style style) {
        this.setStyle(style.applyTo(this.getStyle()));
        return this;
    }

    public MutComponent withStyle(ChatFormat format) {
        this.setStyle(this.getStyle().applyFormat(format));
        return this;
    }

    public MutComponent append(@Nullable Component component) {
        if (component != null) {
            this.siblings.add(component);
        }
        return this;
    }

    public MutComponent append(@Nullable String string) {
        if (!StringUtil.isNullOrEmpty(string)) {
            this.append(Component.literal(string));
        }
        return this;
    }

    public @Override String toString() {
        String contentStr = this.contents.toString();
        boolean styled = !this.style.isEmpty();
        boolean siblings = !this.siblings.isEmpty();
        if (!styled && !siblings) {
            return contentStr;
        }

        var builder = new StringBuilder(contentStr);
        builder.append('[');
        if (styled) {
            builder.append("style=");
            builder.append(this.style);
        }

        if (styled && siblings) {
            builder.append(", ");
        }

        if (siblings) {
            builder.append("siblings=");
            builder.append(this.siblings);
        }

        builder.append(']');
        return builder.toString();
    }
}
