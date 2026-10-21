package dev.adventurecraft.awakening.client.options;

import net.minecraft.client.Option;
import net.minecraft.locale.I18n;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Typed access to the existing public fields, without a second copy of option state. */
public final class OptionBindings {

    private final OptionEffects effects;
    private final Map<String, Binding<?>> bindings = new LinkedHashMap<>();

    public OptionBindings(OptionEffects effects) {
        this.effects = effects;
    }

    public <T> OptionBindings bind(OptionDefinition<T> definition, Supplier<T> read, Consumer<T> write) {
        if (this.bindings.putIfAbsent(definition.key(), new Binding<>(definition, read, write)) != null) {
            throw new IllegalArgumentException("Duplicate option binding: " + definition.key());
        }
        return this;
    }

    public OptionBindings complete() {
        if (this.bindings.size() != GameOptionRegistry.VALUES.size()) {
            throw new IllegalStateException("Every game option must have exactly one field binding");
        }
        for (OptionDefinition<?> definition : GameOptionRegistry.VALUES) {
            Binding<?> binding = this.bindings.get(definition.key());
            if (binding == null || binding.definition != definition) {
                throw new IllegalStateException("Missing field binding: " + definition.key());
            }
        }
        return this;
    }

    public void load(String key, String value) {
        Binding<?> binding = this.bindings.get(key);
        if (binding != null) {
            binding.load(value, this.effects);
        }
    }

    public void save(PrintWriter writer) {
        for (OptionDefinition<?> definition : GameOptionRegistry.VALUES) {
            this.bindings.get(definition.key()).save(writer);
        }
    }

    public void cycle(Option option, int direction) {
        Binding<?> binding = this.find(option);
        if (binding != null) {
            binding.cycle(direction, this.effects);
        }
    }

    public void setProgress(Option option, float progress) {
        Binding<?> binding = this.find(option);
        if (binding != null) {
            binding.setProgress(progress, this.effects);
        }
    }

    public Float progress(Option option) {
        Binding<?> binding = this.find(option);
        return binding == null ? null : binding.progress();
    }

    public Boolean booleanValue(Option option) {
        Binding<?> binding = this.find(option);
        return binding == null ? null : binding.booleanValue();
    }

    public String label(Option option, I18n translations) {
        Binding<?> binding = this.find(option);
        return binding == null ? null : binding.label(translations);
    }

    private Binding<?> find(Option option) {
        for (Binding<?> binding : this.bindings.values()) {
            if (binding.definition.matches(option)) {
                return binding;
            }
        }
        return null;
    }

    private record Binding<T>(OptionDefinition<T> definition, Supplier<T> read, Consumer<T> write) {

        private void update(T value, OptionEffects effects, OptionDefinition.ChangeSource source) {
            this.write.accept(value);
            this.definition.apply(effects, value, source);
        }

        private void load(String value, OptionEffects effects) {
            this.update(this.definition.decode(value), effects, OptionDefinition.ChangeSource.LOAD);
        }

        private void save(PrintWriter writer) {
            writer.println(this.definition.key() + ":" + this.definition.encode(this.read.get()));
        }

        private void cycle(int direction, OptionEffects effects) {
            if (this.definition.canCycle()) {
                this.update(this.definition.next(this.read.get(), direction), effects, OptionDefinition.ChangeSource.INTERACTION);
            }
        }

        private void setProgress(float progress, OptionEffects effects) {
            if (this.definition.slider() != null) {
                this.update(this.definition.slider().value(progress), effects, OptionDefinition.ChangeSource.INTERACTION);
            }
        }

        private Float progress() {
            return this.definition.slider() == null ? null : this.definition.slider().progress(this.read.get());
        }

        private Boolean booleanValue() {
            return this.definition.booleanValue(this.read.get());
        }

        private String label(I18n translations) {
            return this.definition.label(this.read.get(), translations);
        }
    }
}
