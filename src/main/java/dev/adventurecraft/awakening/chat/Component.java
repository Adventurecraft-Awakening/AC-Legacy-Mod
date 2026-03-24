package dev.adventurecraft.awakening.chat;

import com.mojang.brigadier.Message;
import dev.adventurecraft.awakening.chat.contents.LocaleContents;
import dev.adventurecraft.awakening.chat.contents.TextContents;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Component extends StyledText, Message {

    Contents getContents();

    List<Component> getSiblings();

    Style getStyle();

    default MutComponent copy() {
        return new MutComponent(this.getContents(), new ArrayList<>(this.getSiblings()), this.getStyle());
    }

    default @Override String getString() {
        return StyledText.super.getString();
    }

    default String getString(int i) {
        var builder = new StringBuilder();
        this.visit(content -> {
            int j = i - builder.length();
            if (j <= 0) {
                return STOP_ITERATION;
            }
            else {
                builder.append(content.length() <= j ? content : content.substring(0, j));
                return Optional.empty();
            }
        });
        return builder.toString();
    }

    default <T> Optional<T> visit(Consumer<T> consumer) {
        Optional<T> o1 = this.getContents().visit(consumer);
        if (o1.isPresent()) {
            return o1;
        }
        for (Component sibling : this.getSiblings()) {
            Optional<T> o2 = sibling.visit(consumer);
            if (o2.isPresent()) {
                return o2;
            }
        }
        return Optional.empty();
    }

    default <T> Optional<T> visit(StyledConsumer<T> consumer, @Nullable Style style) {
        Style appliedStyle = this.getStyle().applyTo(style);
        Optional<T> o1 = this.getContents().visit(consumer, appliedStyle);
        if (o1.isPresent()) {
            return o1;
        }
        for (Component sibling : this.getSiblings()) {
            Optional<T> o2 = sibling.visit(consumer, appliedStyle);
            if (o2.isPresent()) {
                return o2;
            }
        }
        return Optional.empty();
    }

    static MutComponent empty() {
        return MutComponent.create(TextContents.EMPTY);
    }

    static MutComponent locale(String key) {
        return MutComponent.create(new LocaleContents(key, null, LocaleContents.NO_ARGS));
    }

    static MutComponent locale(String key, Object... args) {
        return MutComponent.create(new LocaleContents(key, null, args));
    }

    static MutComponent localeEscaped(String key, Object... args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (!LocaleContents.isAllowedPrimitiveArgument(arg) && !(arg instanceof Component)) {
                args[i] = String.valueOf(arg);
            }
        }
        return locale(key, args);
    }

    static MutComponent literal(String value) {
        return MutComponent.create(TextContents.create(value));
    }

    static MutComponent literal(char value) {
        return MutComponent.create(TextContents.create(String.valueOf(value)));
    }
}
