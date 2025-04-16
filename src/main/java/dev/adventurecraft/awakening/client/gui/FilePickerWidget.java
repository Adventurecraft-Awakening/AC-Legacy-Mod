package dev.adventurecraft.awakening.client.gui;


import dev.adventurecraft.awakening.common.ScrollableWidget;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

public class FilePickerWidget extends ScrollableWidget {

    public final List<Path> files = new ArrayList<>();
    private int selectedIndex = -1;
    private int hoveredIndex = -1;

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTime = 0L;

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
    protected void entryClicked(int entryIndex, int buttonIndex, boolean doubleClick) {
        if (buttonIndex == 0) {
            this.selectedIndex = entryIndex;
        }
    }

    @Override
    protected void renderContentBackground(
        double left, double right, double top, double bot, double scroll, Tesselator ts) {
        this.fillGradient(
            this.widgetX, this.widgetY,
            this.widgetX + this.width, this.widgetY + this.height,
            0x0f101010, 0xd0101010);
    }

    @Override
    protected void beforeEntryRender(int mouseX, int mouseY, double entryX, double entryY, Tesselator ts) {
        super.beforeEntryRender(mouseX, mouseY, entryX, entryY, ts);

        this.hoveredIndex = this.getEntryUnderPoint(mouseX, mouseY);
    }

    @Override
    protected void renderEntry(int entryIndex, double entryX, double entryY, int entryHeight, Tesselator ts) {
        var exText = (ExTextRenderer) this.client.font;
        String file = this.files.get(entryIndex).getFileName().toString();

        entryX -= 110;

        if (this.selectedIndex == entryIndex || this.hoveredIndex == entryIndex) {
            int width = exText.getTextWidth(file, 0).width() + 6;
            boolean isHover = this.selectedIndex != entryIndex && this.hoveredIndex == entryIndex;
            int borderColor = isHover ? 0x80808080 : 0xff808080;
            int backColor = isHover ? 0x80000000 : 0xff000000;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            ts.begin();
            this.renderContentSelection(
                entryX - 2, entryY - 2, width, entryHeight, 1, borderColor, backColor, ts);
            ts.end();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }

        float x = (float) entryX + 2;
        float y = (float) entryY + 2;
        exText.drawString(file, x, y, 0xffffff, true);
    }

    @Override
    public void render(int mouseX, int mouseY, float tickTime) {
        super.render(mouseX, mouseY, tickTime);

        if (Math.abs(mouseX - this.lastMouseX) > 5 ||
            Math.abs(mouseY - this.lastMouseY) > 5) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            this.mouseStillTime = System.currentTimeMillis();
            return;
        }

        int contentTop = this.contentTop + this.widgetY;
        int contentBot = this.contentBot + this.widgetY;
        if (mouseY > contentBot || mouseY + this.entryHeight < contentTop) {
            return;
        }

        int hoverDelay = 500;
        if (System.currentTimeMillis() >= this.mouseStillTime + hoverDelay) {
            int hoverIndex = getHoveredIndex();
            if (hoverIndex != -1) {
                this.lastMouseX = mouseX;
                this.lastMouseY = mouseY;
                renderHoverTooltip(hoverIndex, mouseX, mouseY);
            }
        }
    }

    public void renderHoverTooltip(int entryIndex, int mouseX, int mouseY) {
        Path path = this.files.get(entryIndex);
        File file = path.toFile();

        List<String> lines = new ArrayList<>();
        lines.add(file.getName());
        lines.add("§7Size: " + file.length() + " bytes");
        lines.add("§7Last modified: " + DateFormat.getInstance().format(new Date(file.lastModified())));

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            lines.add("§7Dir: " + path.getParent().normalize());
        }

        int maxWidth = lines.stream()
            .mapToInt(line -> this.client.font.width(line))
            .max()
            .orElse(0);
        if (maxWidth == 0) {
            return;
        }

        int x = this.widgetX + this.width / 2 - maxWidth / 2;
        int y = mouseY + 4;

        int xEnd = x + maxWidth + 10;
        int yEnd = y + 11 * lines.size() + 6;

        fillGradient(x, y, xEnd, yEnd, 0xe0000000, 0xe0000000);

        int lineIndex = 0;
        for (String line : lines) {
            this.client.font.drawShadow(line, x + 5, y + 5 + lineIndex++ * 11, 14540253);
        }
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