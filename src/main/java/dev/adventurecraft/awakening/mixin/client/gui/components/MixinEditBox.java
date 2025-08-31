package dev.adventurecraft.awakening.mixin.client.gui.components;

import dev.adventurecraft.awakening.client.gui.components.AC_EditBox;
import dev.adventurecraft.awakening.layout.IntRect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(EditBox.class)
public class MixinEditBox {

    @Shadow private Screen parent;
    @Shadow @Final private Font font;

    @Shadow public boolean active;
    @Shadow public boolean visible;
    @Unique private AC_EditBox editBox;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(Screen screen, Font font, int x, int y, int width, int height, String value, CallbackInfo ci) {
        this.editBox = new AC_EditBox(new IntRect(x, y, width, height), value);
    }

    public @Overwrite void setValue(String msg) {
        if (this.editBox != null) {
            this.editBox.setValue(msg);
        }
    }

    public @Overwrite String getValue() {
        return this.editBox.getValue();
    }

    public @Overwrite void tick() {
        this.editBox.setActive(this.active);
        this.editBox.setVisible(this.visible);
        this.editBox.tick();
    }

    public @Overwrite void charTyped(char codePoint, int key) {
        if (codePoint == '\t') {
            if (this.editBox.isVisible() && this.editBox.isActive()) {
                this.parent.tab();
            }
        }
        this.editBox.charTyped(codePoint, key);
    }

    public @Overwrite void clicked(int mouseX, int mouseY, int button) {
        this.editBox.clicked(mouseX, mouseY, button);
        this.active = this.editBox.isActive();
    }

    public @Overwrite void setActive(boolean active) {
        this.editBox.setActive(active);
    }

    public @Overwrite void render() {
        this.editBox.render(this.font);
    }

    public @Overwrite void setMaxLength(int length) {
        this.editBox.setMaxLength(length);
    }
}
