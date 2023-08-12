package dev.adventurecraft.awakening.common;

import java.util.*;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.World;

public class AC_GuiScriptStats extends Screen {

    int maxSize;
    ArrayList<AC_JScriptInfo> scriptInfos;

    public AC_GuiScriptStats(World world) {
        this.scriptInfos = new ArrayList<>();

        var infos = ((ExWorld) world).getScriptHandler().scripts.values();
        for (AC_JScriptInfo info : infos) {
            if (info.count > 0) {
                this.scriptInfos.add(info);
            }
        }
    }

    @Override
    public void initVanillaScreen() {
        this.maxSize = 90;
        for (AC_JScriptInfo info : this.scriptInfos) {
            int width = Minecraft.instance.textRenderer.getTextWidth(info.name);
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
    protected void buttonClicked(ButtonWidget button) {
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();
        this.drawTextWithShadow(this.textRenderer, "Script", 4, 1, 14737632);
        this.drawTextWithShadow(this.textRenderer, "Avg", this.maxSize, 1, 14737632);
        this.drawTextWithShadow(this.textRenderer, "Max", this.maxSize + 50, 1, 14737632);
        this.drawTextWithShadow(this.textRenderer, "Total", this.maxSize + 100, 1, 14737632);
        this.drawTextWithShadow(this.textRenderer, "Count", this.maxSize + 150, 1, 14737632);

        int y = 10;
        for (AC_JScriptInfo info : this.scriptInfos) {
            double totalTime = (double) info.totalTime / 1000000.0D;
            double avgTime = totalTime / (double) info.count;
            double maxTime = (double) info.maxTime / 1000000.0D;
            this.drawTextWithShadow(this.textRenderer, info.name, 4, y, 14737632);
            this.drawTextWithShadow(this.textRenderer, String.format("%.2f", avgTime), this.maxSize, y, 14737632);
            this.drawTextWithShadow(this.textRenderer, String.format("%.2f", maxTime), this.maxSize + 50, y, 14737632);
            this.drawTextWithShadow(this.textRenderer, String.format("%.2f", totalTime), this.maxSize + 100, y, 14737632);
            this.drawTextWithShadow(this.textRenderer, String.format("%d", info.count), this.maxSize + 150, y, 14737632);
            y += 10;
        }

        super.render(mouseX, mouseY, deltaTime);
    }
}
