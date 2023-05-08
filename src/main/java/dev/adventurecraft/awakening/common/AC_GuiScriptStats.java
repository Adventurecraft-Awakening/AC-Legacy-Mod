package dev.adventurecraft.awakening.common;

import java.util.*;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class AC_GuiScriptStats extends Screen {

    int maxSize = 90;
    ArrayList<AC_JScriptInfo> scriptInfo;

    public AC_GuiScriptStats() {
        this.scriptInfo = new ArrayList<>();

        Collection<AC_JScriptInfo> infos = ((ExWorld) Minecraft.instance.world).getScriptHandler().scripts.values();
        for (AC_JScriptInfo info : infos) {
            if (info.count > 0) {
                this.scriptInfo.add(info);
            }
        }

        for (AC_JScriptInfo info : this.scriptInfo) {
            int width = Minecraft.instance.textRenderer.getTextWidth(info.name);
            if (width > this.maxSize) {
                this.maxSize = width;
            }
        }

        this.maxSize += 10;
        this.scriptInfo.sort(Comparator.naturalOrder());
    }

    @Override
    public void initVanillaScreen() {
    }

    @Override
    public void tick() {
    }

    @Override
    protected void buttonClicked(ButtonWidget var1) {
    }

    @Override
    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        this.drawTextWithShadow(this.textRenderer, "Script", 4, 1, 14737632);
        this.drawTextWithShadow(this.textRenderer, "Avg", this.maxSize, 1, 14737632);
        this.drawTextWithShadow(this.textRenderer, "Max", this.maxSize + 50, 1, 14737632);
        this.drawTextWithShadow(this.textRenderer, "Total", this.maxSize + 100, 1, 14737632);
        this.drawTextWithShadow(this.textRenderer, "Count", this.maxSize + 150, 1, 14737632);
        int var4 = 10;

        for (AC_JScriptInfo info : this.scriptInfo) {
            double totalTime = (double) info.totalTime / 1000000.0D;
            double avgTime = totalTime / (double) info.count;
            double maxTime = (double) info.maxTime / 1000000.0D;
            this.drawTextWithShadow(this.textRenderer, info.name, 4, var4, 14737632);
            this.drawTextWithShadow(this.textRenderer, String.format("%.2f", avgTime), this.maxSize, var4, 14737632);
            this.drawTextWithShadow(this.textRenderer, String.format("%.2f", maxTime), this.maxSize + 50, var4, 14737632);
            this.drawTextWithShadow(this.textRenderer, String.format("%.2f", totalTime), this.maxSize + 100, var4, 14737632);
            this.drawTextWithShadow(this.textRenderer, String.format("%d", info.count), this.maxSize + 150, var4, 14737632);
            var4 += 10;
        }

        super.render(var1, var2, var3);
    }

    public static void showUI() {
        Minecraft.instance.openScreen(new AC_GuiScriptStats());
    }
}
