package dev.adventurecraft.awakening.common.gui;

import java.util.*;
import java.util.function.ToDoubleFunction;

import dev.adventurecraft.awakening.common.AC_JScriptInfo;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AC_GuiScriptStats extends Screen {

    private static final int COLUMN_SPACING = 60;

    int maxSize;
    ArrayList<AC_JScriptInfo> scriptInfos;

    private Button buttonAvg;
    private Button buttonMax;
    private Button buttonTotal;
    private Button buttonCount;

    private @Nullable ToDoubleFunction<AC_JScriptInfo> keyExtractor;
    private @Nullable Button activeButton;
    private int sortOrder;

    public AC_GuiScriptStats(Level world) {
        this.scriptInfos = new ArrayList<>();

        var infos = ((ExWorld) world).getScriptHandler().getScripts();
        for (AC_JScriptInfo info : infos) {
            if (info.count > 0) {
                this.scriptInfos.add(info);
            }
        }
        this.passEvents = true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        this.initButtons();
    }

    private void initButtons() {
        this.buttons.add(this.buttonAvg = new Button(1, 0, 1, 40, 20, "Avg"));
        this.buttons.add(this.buttonMax = new Button(2, 0, 1, 40, 20, "Max"));
        this.buttons.add(this.buttonTotal = new Button(3, 0, 1, 40, 20, "Total"));
        this.buttons.add(this.buttonCount = new Button(4, 0, 1, 40, 20, "Count"));
    }

    @Override
    public void tick() {
        this.maxSize = 90;
        for (AC_JScriptInfo info : this.scriptInfos) {
            int width = Minecraft.instance.font.width(info.name);
            this.maxSize = Math.max(width, this.maxSize);
        }
        this.maxSize += 15;

        int x = this.maxSize;
        this.buttonAvg.x = x;
        this.buttonMax.x = x += COLUMN_SPACING;
        this.buttonTotal.x = x += COLUMN_SPACING;
        this.buttonCount.x = x += COLUMN_SPACING;
    }

    @Override
    protected void buttonClicked(Button button) {
        if (button.id == 1) {
            this.keyExtractor = o -> (double) o.totalTime / (double) o.count;
        }
        else if (button.id == 2) {
            this.keyExtractor = o -> o.maxTime;
        }
        else if (button.id == 3) {
            this.keyExtractor = o -> o.totalTime;
        }
        else if (button.id == 4) {
            this.keyExtractor = o -> o.count;
        }
        else {
            return;
        }
        this.activateButton(button);
    }

    protected void activateButton(Button button) {
        if (button != this.activeButton) {
            this.activeButton = button;
            this.sortOrder = -1;
        }
        else if (this.sortOrder == -1) {
            this.sortOrder = 1;
        }
        else {
            this.activeButton = null;
            this.sortOrder = 0;
            this.keyExtractor = null;
        }

        // TODO: sort every few ticks? if so, also add hotkey that stops sorting when held
        if (this.keyExtractor != null) {
            this.scriptInfos.sort(comparingDouble(this.sortOrder, this.keyExtractor));
        }
    }

    private static Comparator<AC_JScriptInfo> comparingDouble(int order, ToDoubleFunction<AC_JScriptInfo> key) {
        if (order >= 0) {
            return Comparator.comparingDouble(key);
        }
        return (c1, c2) -> Double.compare(key.applyAsDouble(c2), key.applyAsDouble(c1));
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();

        if (this.activeButton != null) {
            this.renderSortArrow(this.activeButton);
        }

        try (var ui = dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge.uiContext(this.font, this.minecraft.textures)) {
            var state = ((ExTextRenderer) this.font).createState();
            state.setColor(0xffe0e0e0);
            state.setShadowToColor();
            state.begin(ui);

            int y = 25;
            for (AC_JScriptInfo info : this.scriptInfos) {
                double totalTime = (double) info.totalTime / 1000000.0D;
                double avgTime = totalTime / (double) info.count;
                double maxTime = (double) info.maxTime / 1000000.0D;
                state.drawText(info.name, 4, y);

                int x = this.maxSize;
                state.drawText(String.format("%.2f", avgTime), x, y);
                state.drawText(String.format("%.2f", maxTime), x += COLUMN_SPACING, y);
                state.drawText(String.format("%.2f", totalTime), x += COLUMN_SPACING, y);
                state.drawText(String.format("%d", info.count), x += COLUMN_SPACING, y);
                y += 10;
            }
            state.end();
        }

        super.render(mouseX, mouseY, deltaTime);
    }

    private void renderSortArrow(Button button) {
        int u = this.sortOrder == 1 ? 36 : 18;
        this.renderSlotIcon(button.x - 15, 1, u, 0);
    }

    private void renderSlotIcon(int x, int y, int u, int v) {
        try (var ui = dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge.uiContext(this.font, this.minecraft.textures)) {
            ui.translated(0, 0, this.blitOffset).image(ui.texture("/gui/slot.png"),
                new dev.adventurecraft.awakening.layout.Rect(x, y, 18, 18),
                new dev.adventurecraft.awakening.layout.Rect(u / 128f, v / 128f, 18 / 128f, 18 / 128f),
                new dev.adventurecraft.awakening.layout.IntCorner(-1));
        }
    }
}
