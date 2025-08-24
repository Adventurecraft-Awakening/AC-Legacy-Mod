package dev.adventurecraft.awakening.common.gui;

import java.util.*;

import dev.adventurecraft.awakening.common.AC_JScriptInfo;
import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;

public class AC_GuiScriptStats extends Screen {

    int maxSize;
    ArrayList<AC_JScriptInfo> scriptInfos;

    public AC_GuiScriptStats(Level world) {
        this.scriptInfos = new ArrayList<>();

        var infos = ((ExWorld) world).getScriptHandler().getScripts();
        for (AC_JScriptInfo info : infos) {
            if (info.count > 0) {
                this.scriptInfos.add(info);
            }
        }
        this.passEvents = true;
        ((ExScreen) this).setDisabledInputGrabbing(true);
    }

    @Override
    public void init() {
        this.maxSize = 90;
        for (AC_JScriptInfo info : this.scriptInfos) {
            int width = Minecraft.instance.font.width(info.name);
            if (width > this.maxSize) {
                this.maxSize = width;
            }
        }
        this.maxSize += 10;

        // TODO: sort every few ticks? if so, also add hotkey that stops sorting when held
        this.scriptInfos.sort(Comparator.comparing(o -> o.totalTime));
    }

    @Override
    public void tick() {
    }

    @Override
    protected void buttonClicked(Button button) {
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();

        var state = ((ExTextRenderer) this.font).createState();
        state.setColor(0xffa0ffa0);
        state.setShadowToColor();
        state.begin(Tesselator.instance);

        state.drawText("Script", 4, 1);
        state.drawText("Avg", this.maxSize, 1);
        state.drawText("Max", this.maxSize + 50, 1);
        state.drawText("Total", this.maxSize + 100, 1);
        state.drawText("Count", this.maxSize + 150, 1);

        state.setColor(0xffe0e0e0);
        state.setShadowToColor();
        state.resetFormat();

        int y = 10;
        for (AC_JScriptInfo info : this.scriptInfos) {
            double totalTime = (double) info.totalTime / 1000000.0D;
            double avgTime = totalTime / (double) info.count;
            double maxTime = (double) info.maxTime / 1000000.0D;
            state.drawText(info.name, 4, y);
            state.drawText(String.format("%.2f", avgTime), this.maxSize, y);
            state.drawText(String.format("%.2f", maxTime), this.maxSize + 50, y);
            state.drawText(String.format("%.2f", totalTime), this.maxSize + 100, y);
            state.drawText(String.format("%d", info.count), this.maxSize + 150, y);
            y += 10;
        }
        state.end();

        super.render(mouseX, mouseY, deltaTime);
    }
}
