package dev.adventurecraft.awakening.common;

import java.util.*;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
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
        this.scriptInfos.sort(Comparator.naturalOrder()); // TODO: sort every few ticks?
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
        this.drawString(this.font, "Script", 4, 1, 14737632);
        this.drawString(this.font, "Avg", this.maxSize, 1, 14737632);
        this.drawString(this.font, "Max", this.maxSize + 50, 1, 14737632);
        this.drawString(this.font, "Total", this.maxSize + 100, 1, 14737632);
        this.drawString(this.font, "Count", this.maxSize + 150, 1, 14737632);

        int y = 10;
        for (AC_JScriptInfo info : this.scriptInfos) {
            double totalTime = (double) info.totalTime / 1000000.0D;
            double avgTime = totalTime / (double) info.count;
            double maxTime = (double) info.maxTime / 1000000.0D;
            this.drawString(this.font, info.name, 4, y, 14737632);
            this.drawString(this.font, String.format("%.2f", avgTime), this.maxSize, y, 14737632);
            this.drawString(this.font, String.format("%.2f", maxTime), this.maxSize + 50, y, 14737632);
            this.drawString(this.font, String.format("%.2f", totalTime), this.maxSize + 100, y, 14737632);
            this.drawString(this.font, String.format("%d", info.count), this.maxSize + 150, y, 14737632);
            y += 10;
        }

        super.render(mouseX, mouseY, deltaTime);
    }
}
