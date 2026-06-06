package dev.adventurecraft.awakening.dom;

import com.google.common.collect.ImmutableList;
import dev.adventurecraft.awakening.extension.client.resource.language.ExTranslationStorage;
import net.minecraft.locale.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleNode implements Node {

    public static final Object[] NO_ARGS = new Object[0];

    private static final Node TEXT_PERCENT = Node.text("%");
    private static final Node TEXT_NULL = Node.text("null");

    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private final String key;
    private final @Nullable String fallback;
    private final Object[] args;

    private @Nullable I18n decomposedWith;
    private List<Node> decomposedNodes = ImmutableList.of();

    private LocaleNode(String key, @Nullable String fallback, Object[] args) {
        this.key = key;
        this.fallback = fallback;
        this.args = args;
    }

    public @Override <T> Optional<T> visit(NodeConsumer<T> consumer) {
        this.decompose();
        for (Node node : this.decomposedNodes) {
            Optional<T> o = node.visit(consumer);
            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    public @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style style) {
        this.decompose();
        for (Node node : this.decomposedNodes) {
            Optional<T> o = node.visit(consumer, style);
            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    public @Override boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof LocaleNode contents) {
            return Objects.equals(this.key, contents.key) && Objects.equals(this.fallback, contents.fallback) &&
                Arrays.equals(this.args, contents.args);
        }
        return false;
    }

    public @Override int hashCode() {
        int i = Objects.hashCode(this.key);
        i = 31 * i + Objects.hashCode(this.fallback);
        return 31 * i + Arrays.hashCode(this.args);
    }

    public String getKey() {
        return this.key;
    }

    public @Nullable String getFallback() {
        return this.fallback;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public final Node getArgument(int i) {
        if (i >= 0 && i < this.args.length) {
            Object object = this.args[i];
            if (object instanceof Node node) {
                return node;
            }
            return object == null ? TEXT_NULL : TextNode.of(object.toString());
        }
        throw new LocaleFormatException(this, i);
    }

    public static boolean isAllowedPrimitiveArgument(@Nullable Object object) {
        return object instanceof Number || object instanceof Boolean || object instanceof String;
    }

    public static LocaleNode key(String key) {
        return new LocaleNode(key, null, LocaleNode.NO_ARGS);
    }

    public static LocaleNode format(String key, Object... args) {
        return new LocaleNode(key, null, args);
    }

    public static LocaleNode escaped(String key, Object... args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (!LocaleNode.isAllowedPrimitiveArgument(arg) && !(arg instanceof Node)) {
                args[i] = String.valueOf(arg);
            }
        }
        return format(key, args);
    }

    private void decompose() {
        I18n lang = I18n.getInstance();
        if (lang == this.decomposedWith) {
            return;
        }
        this.decomposedWith = lang;
        var exLang = ((ExTranslationStorage) lang);
        String value = this.fallback != null ? exLang.getOr(this.key, this.fallback) : lang.get(this.key);
        try {
            var builder = ImmutableList.<Node>builder();
            this.decomposeTemplate(value, builder::add);
            this.decomposedNodes = builder.build();
        }
        catch (LocaleFormatException ex) {
            this.decomposedNodes = ImmutableList.of(Node.text(value));
        }
    }

    private void decomposeTemplate(String template, Consumer<Node> consumer) {
        Matcher matcher = FORMAT_PATTERN.matcher(template);
        try {
            int argOffset = 0;
            int i = 0;

            while (matcher.find(i)) {
                int start = matcher.start();
                int end = matcher.end();
                if (start > i) {
                    String text = template.substring(i, start);
                    if (text.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }
                    consumer.accept(Node.text(text));
                }

                String specifier = matcher.group(2);
                String format = template.substring(start, end);
                if ("%".equals(specifier) && "%%".equals(format)) {
                    consumer.accept(TEXT_PERCENT);
                }
                else {
                    if (!"s".equals(specifier)) {
                        throw new LocaleFormatException(this, "Unsupported format: '" + format + "'");
                    }

                    String argName = matcher.group(1);
                    int argIndex = argName != null ? Integer.parseInt(argName) - 1 : argOffset++;
                    consumer.accept(this.getArgument(argIndex));
                }

                i = end;
            }

            if (i < template.length()) {
                String text = template.substring(i);
                if (text.indexOf('%') != -1) {
                    throw new IllegalArgumentException();
                }
                consumer.accept(Node.text(text));
            }
        }
        catch (IllegalArgumentException ex) {
            throw new LocaleFormatException(this, ex);
        }
    }
}
