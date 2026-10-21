package dev.adventurecraft.awakening.client.gui.components;

import dev.adventurecraft.awakening.common.GuiSlider2;
import dev.adventurecraft.awakening.primitives.Property;

import java.util.Objects;
import java.util.function.Function;

/** A live property editor whose model changes happen only from pointer input. */
public final class BoundSlider<T> extends GuiSlider2 {
    private final Property<T> property;
    private final Function<Float, T> decode;
    private final Function<T, Float> encode;
    private final Function<T, String> label;

    public BoundSlider(int id, int x, int y, Property<T> property, Function<T, Float> encode,
                       Function<Float, T> decode, Function<T, String> label) {
        super(id, x, y, 0, label.apply(property.get()), encode.apply(property.get()));
        this.property = Objects.requireNonNull(property);
        this.encode = Objects.requireNonNull(encode);
        this.decode = Objects.requireNonNull(decode);
        this.label = Objects.requireNonNull(label);
    }

    @Override protected boolean updateWhileRendering() { return false; }

    @Override protected void valueChanged(float position) {
        T value = this.decode.apply(position);
        if (!Objects.equals(value, this.property.get())) this.property.set(value);
        this.message = this.label.apply(this.property.get());
    }

    /** Optional tick-time refresh for values changed externally by a non-paused world. */
    public void refresh() {
        if (this.dragging) return;
        T value = this.property.get();
        this.sliderValue = this.encode.apply(value);
        this.message = this.label.apply(value);
    }

    /** Beta's Screen sends pointer motion to mouseReleased with button -1. */
    public static void drag(Iterable<?> controls, int mouseX) {
        for (Object control : controls) {
            if (control instanceof BoundSlider<?> slider) slider.dragTo(mouseX);
        }
    }
}
