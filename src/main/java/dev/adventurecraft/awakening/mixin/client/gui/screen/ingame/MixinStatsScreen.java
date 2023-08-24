package dev.adventurecraft.awakening.mixin.client.gui.screen.ingame;

import dev.adventurecraft.awakening.extension.client.gui.widget.ExScrollableBaseWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.StatsScreen;
import net.minecraft.client.gui.widget.ScrollableBaseWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StatsScreen.class)
public abstract class MixinStatsScreen extends Screen {

    @Shadow
    private ScrollableBaseWidget generalTabBase;

    @Override
    public void onMouseEvent() {
        super.onMouseEvent();
        if (this.generalTabBase instanceof ExScrollableBaseWidget scrollable) {
            scrollable.onMouseEvent();
        }
    }
}
