package dev.adventurecraft.awakening.client.gui;

import ch.bailu.gtk.gdk.Display;
import ch.bailu.gtk.gtk.*;
import ch.bailu.gtk.type.exception.AllocationError;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.ScrollableWidget;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.filesystem.FileIconOptions;
import dev.adventurecraft.awakening.filesystem.FileIconRenderer;
import dev.adventurecraft.awakening.filesystem.GtkFileIconRenderer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.layout.*;
import dev.adventurecraft.awakening.layout.Border;
import dev.adventurecraft.awakening.util.*;
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

    private IntPoint lastMousePoint = IntPoint.zero;
    private long mouseStillTime = 0L;

    public FilePickerWidget(Minecraft minecraft, IntRect layoutRect, int entryHeight) {
        super(minecraft, layoutRect, entryHeight);
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
        else if (buttonIndex == 1) {
            Path path = this.files.get(entryIndex);
            try {
                DesktopUtil.browseFileDirectory(path);
            }
            catch (Exception ex) {
                ACMod.LOGGER.warn("Failed to browse file dir: ", ex);
            }
        }
    }

    @Override
    protected void renderContentBackground(Tesselator ts, Rect rect, Point scroll) {
        DrawUtil.fillRect(ts, rect, IntCorner.vertical(0x0f101010, 0xd0101010), null);
    }

    @Override
    protected void beforeEntryRender(Tesselator ts, IntPoint mouseLocation, Point entryLocation) {
        super.beforeEntryRender(ts, mouseLocation, entryLocation);

        this.hoveredIndex = this.getEntryUnderPoint(mouseLocation.asFloat());
    }

    @Override
    protected void renderEntry(Tesselator ts, int entryIndex, Point entryLocation, int entryHeight) {
        var exText = (ExTextRenderer) this.client.font;
        String file = FileDisplayUtil.colorizePath(this.files.get(entryIndex).getFileName());

        if (this.selectedIndex == entryIndex || this.hoveredIndex == entryIndex) {
            int width = exText.getTextWidth(file, 0).width() + 6;
            var selectRect = new Rect(entryLocation.x, entryLocation.y, width, entryHeight);

            boolean isHover = this.selectedIndex != entryIndex && this.hoveredIndex == entryIndex;
            var borderColor = new IntCorner(isHover ? 0x80808080 : 0xff808080);
            var backColor = new IntCorner(isHover ? 0x80000000 : 0xff000000);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            ts.begin();
            this.renderContentSelection(ts, selectRect, new Border(1), borderColor, backColor, null, null);
            ts.end();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }

        float x = (float) entryLocation.x + 3;
        float y = (float) entryLocation.y + 3;
        exText.drawString(file, x, y, 0xffffff, true);
    }

    @Override
    public void render(IntPoint mousePoint, float tickTime) {
        super.render(mousePoint, tickTime);

        IntPoint mouseDelta = mousePoint.sub(lastMousePoint).abs();
        if (mouseDelta.x > 5 || mouseDelta.y > 5) {
            this.lastMousePoint = mousePoint;
            this.mouseStillTime = System.currentTimeMillis();
            return;
        }

        IntRect contentRect = this.getBorderRect();
        if (mousePoint.y > contentRect.bot() || mousePoint.y + this.entryHeight < contentRect.top()) {
            return;
        }

        int hoverDelay = 500;
        if (System.currentTimeMillis() >= this.mouseStillTime + hoverDelay) {
            int hoverIndex = getHoveredIndex();
            if (hoverIndex != -1) {
                this.lastMousePoint = mousePoint;
                this.renderHoverTooltip(hoverIndex, mousePoint);
            }
        }
    }

    public void renderHoverTooltip(int entryIndex, IntPoint mousePoint) {
        Path path = this.files.get(entryIndex);
        File file = path.toFile();

        List<String> lines = new ArrayList<>();
        lines.add(FileDisplayUtil.colorizePath(path.getFileName()));
        lines.add("§7Size: §r" + FileDisplayUtil.readableLength(file.length()));
        lines.add("§7Last modified: §r" + DateFormat.getInstance().format(new Date(file.lastModified())));

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            lines.add("§7Dir: §r" + FileDisplayUtil.colorizePath(path.getParent().normalize()));
        }

        int maxWidth = lines.stream().mapToInt(line -> this.client.font.width(line)).max().orElse(0);
        if (maxWidth == 0) {
            return;
        }

        IntRect layoutRect = this.getLayoutRect();
        int x = layoutRect.left() + layoutRect.width() / 2 - maxWidth / 2;
        int y = mousePoint.y + 4;

        int xEnd = x + maxWidth + 10;
        int yEnd = y + 11 * lines.size() + 6;

        fillGradient(x, y, xEnd, yEnd, 0xe0000000, 0xe0000000);

        try (var renderer = FileIconRenderer.create()) {
            if (renderer != null) {
                double size = (Math.sin(System.currentTimeMillis() / 4000.0) + 1.0) * 31 + 1;
                this.renderFileIcon(renderer, path, new Rect(x - 32, y, size, size));
            }
        }

        int lineIndex = 0;
        for (String line : lines) {
            this.client.font.drawShadow(line, x + 5, y + 5 + lineIndex++ * 11, 14540253);
        }
    }

    private void renderFileIcon(FileIconRenderer renderer, Path path, Rect rect) {
        Rect realRect = GLUtil.projectModelViewProj(rect);
        double fRealSize = Math.max(realRect.width(), realRect.height());
        int realSize = (int) Math.round(fRealSize);

        var image = renderer.getIcon(path, new FileIconOptions(ImageFormat.RGBA_U8, realSize, 1, false));

        var ts = Tesselator.instance;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);

        var texMan = (ExTextureManager) this.client.textures;
        int id = texMan.loadTexture(image);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        ts.begin();
        DrawUtil.fillRect(ts, rect, new IntCorner(0xffffffff), new Rect(0, 0, 1, 1));
        ts.end();

        texMan.releaseTexture(id);
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