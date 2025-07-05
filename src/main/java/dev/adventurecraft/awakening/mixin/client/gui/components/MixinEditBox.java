package dev.adventurecraft.awakening.mixin.client.gui.components;

import dev.adventurecraft.awakening.extension.client.gui.components.ExEditBox;
import dev.adventurecraft.awakening.layout.IntRect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EditBox.class)
public abstract class MixinEditBox implements ExEditBox {

    @Shadow @Final private Font font;
    @Shadow @Final private int x;
    @Shadow @Final private int y;
    @Shadow @Final private int width;
    @Shadow @Final private int height;

    @Unique private int activeTextColor = 0xe0e0e0;
    @Unique private int inactiveTextColor = 0x707070;

    @ModifyConstant(method = "render", constant = @Constant(intValue = 0xe0e0e0))
    private int useActiveTextColor(int constant) {
        return this.activeTextColor;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 0x707070))
    private int useInactiveTextColor(int constant) {
        return this.inactiveTextColor;
    }

    public @Override IntRect getRect() {
        return new IntRect(this.x, this.y, this.width, this.height);
    }

    public @Override Font getFont() {
        return this.font;
    }

    public @Override int getActiveTextColor() {
        return activeTextColor;
    }

    public @Override void setActiveTextColor(int color) {
        this.activeTextColor = color;
    }

    public @Override int getInactiveTextColor() {
        return inactiveTextColor;
    }

    public @Override void setInactiveTextColor(int color) {
        this.inactiveTextColor = color;
    }
}
