package dev.adventurecraft.awakening.client.gui;


import dev.adventurecraft.awakening.common.ScrollableWidget;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.GL11;

import java.nio.file.Path;
import java.util.ArrayList;

public class FilePickerWidget extends ScrollableWidget {

    public ArrayList<Path> files = new ArrayList<>();
    private int selectedIndex = -1;
    private int hoveredIndex = -1;

    public FilePickerWidget(
        Minecraft minecraft,
        int x, int y, int width, int height,
        int contentStartY, int contentEndY, int entryHeight) {
        super(minecraft, x, y, width, height, contentStartY, contentEndY, entryHeight);
        this.setContentTopPadding(4);
    }

    @Override
    protected int getEntryCount() {
        return this.files.size();
    }

    @Override
    protected void entryClicked(int entryIndex, boolean doubleClick) {
        this.selectedIndex = entryIndex;
    }

    @Override
    protected void renderContentBackground(
        double left, double right, double top, double bot, double scroll, Tessellator ts) {
        this.fillGradient(
            this.widgetX, this.widgetY,
            this.widgetX + this.width, this.widgetY + this.height,
            0x0f101010, 0xd0101010);
    }

    @Override
    protected void beforeEntryRender(int mouseX, int mouseY, double entryX, double entryY, Tessellator ts) {
        super.beforeEntryRender(mouseX, mouseY, entryX, entryY, ts);

        this.hoveredIndex = this.getEntryUnderPoint(mouseX, mouseY);
    }

    @Override
    protected void renderEntry(int entryIndex, double entryX, double entryY, int entryHeight, Tessellator ts) {
        var exText = (ExTextRenderer) this.client.textRenderer;
        String file = this.files.get(entryIndex).getFileName().toString();

        entryX -= 110;

        if (this.selectedIndex == entryIndex || this.hoveredIndex == entryIndex) {
            int width = exText.getTextWidth(file, 0).width() + 6;
            boolean isHover = this.selectedIndex != entryIndex && this.hoveredIndex == entryIndex;
            int borderColor = isHover ? 0x80808080 : 0xff808080;
            int backColor = isHover ? 0x80000000 : 0xff000000;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            ts.start();
            this.renderContentSelection(
                entryX - 2, entryY - 2, width, entryHeight, 1, borderColor, backColor, ts);
            ts.tessellate();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }

        float x = (float) entryX + 2;
        float y = (float) entryY + 2;
        exText.drawString(file, x, y, 0xffffff, true);
    }

    public Path getSelectedItem() {
        int index = getSelectedIndex();
        if (index != -1) {
            return this.files.get(index);
        }
        return null;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public int getHoveredIndex() {
        return this.hoveredIndex;
    }
}