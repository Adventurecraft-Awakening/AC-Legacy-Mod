package dev.adventurecraft.awakening.client.gui.components;

import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.primitives.Property;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class AC_ValueBox<T> extends AC_EditBox {

    private Property<T> property;
    private Format format;

    private int prevVersion;
    private int version;
    private boolean resupply;

    public AC_ValueBox(IntRect rect, Property<T> property, Format format) {
        super(rect, "");
        this.property = Objects.requireNonNull(property);
        this.format = Objects.requireNonNull(format);

        this.supply();
    }

    protected @Override void onValueChanged() {
        this.version++;
    }

    protected @Override void onSubmit() {
        this.resupply = true;
        super.onSubmit();
    }

    private void resetError() {
        this.resetTextColor();
    }

    private void rebind() {
        this.resetError();

        String value = this.getValue();
        var pos = new ParsePosition(0);
        Object result = this.format.parseObject(value, pos);

        // If the position is at the end, we parsed everything.
        // Care has to be taken to avoid nulls on parse failure. TODO: this.allowNull field?
        if (pos.getErrorIndex() == -1 && pos.getIndex() == value.length()) {
            // TODO: type check with Class<T>
            this.property.set((T) result);
            return;
        }

        this.setActiveTextColor(Rgba.fromRgb8(0xff, 0, 0));
        this.setInactiveTextColor(Rgba.fromRgb8(0x8f, 0, 0));
        // TODO: show error range
    }

    private void supply() {
        this.resetError();
        this.setValue(this.format.format(this.property.get()));
    }

    @Override
    public void render(Font font) {
        if (this.prevVersion != this.version) {
            this.prevVersion = this.version;
            this.rebind();
        }

        if (this.resupply) {
            this.resupply = false;
            this.supply();
        }

        super.render(font);
    }
}
