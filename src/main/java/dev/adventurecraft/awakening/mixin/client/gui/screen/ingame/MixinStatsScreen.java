package dev.adventurecraft.awakening.mixin.client.gui.screen.ingame;

import dev.adventurecraft.awakening.extension.client.gui.widget.ExScrollableBaseWidget;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.StatsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StatsScreen.class)
public abstract class MixinStatsScreen extends Screen {

    @Shadow
    private AbstractSelectionList generalTabBase;

    @Override
    public void mouseEvent() {
        super.mouseEvent();
        if (this.generalTabBase instanceof ExScrollableBaseWidget scrollable) {
            scrollable.onMouseEvent();
        }
    }
}
