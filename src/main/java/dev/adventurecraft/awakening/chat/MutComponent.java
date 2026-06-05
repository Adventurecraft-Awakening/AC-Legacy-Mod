package dev.adventurecraft.awakening.chat;

import dev.adventurecraft.awakening.dom.Node;
import dev.adventurecraft.awakening.dom.Style;
import dev.adventurecraft.awakening.util.StringUtil;

import javax.annotation.Nullable;
import java.util.List;

public class MutComponent implements Component {

    private final Node node;
    private final List<Component> siblings;
    private Style style;

    public MutComponent(Node node, List<Component> siblings, Style style) {
        this.node = node;
        this.siblings = siblings;
        this.style = style;
    }

    public @Override Node contents() {
        return this.node;
    }

    public @Override List<Component> siblings() {
        return this.siblings;
    }

    public @Override Style style() {
        return this.style;
    }

    public MutComponent setStyle(Style style) {
        this.style = style;
        return this;
    }

    public MutComponent withStyle(Style style) {
        this.setStyle(style.applyTo(this.style()));
        return this;
    }

    public MutComponent withStyle(ChatFormat format) {
        this.setStyle(format.apply(this.style()));
        return this;
    }

    public MutComponent append(@Nullable Component component) {
        if (component != null) {
            this.siblings.add(component);
        }
        return this;
    }

    public MutComponent append(@Nullable String value) {
        if (!StringUtil.isNullOrEmpty(value)) {
            this.append(Component.literal(value));
        }
        return this;
    }

    public @Override String toString() {
        String contentStr = this.node.toString();
        boolean styled = !this.style.isEmpty();
        boolean siblings = !this.siblings().isEmpty();
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
