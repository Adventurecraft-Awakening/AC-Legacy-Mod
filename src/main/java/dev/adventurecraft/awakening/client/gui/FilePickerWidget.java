package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.ScrollableWidget;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.filesystem.FileIconFlags;
import dev.adventurecraft.awakening.filesystem.FileIconOptions;
import dev.adventurecraft.awakening.filesystem.FileIconRenderer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.layout.*;
import dev.adventurecraft.awakening.layout.Border;
import dev.adventurecraft.awakening.layout.Point;
import dev.adventurecraft.awakening.util.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilePickerWidget extends ScrollableWidget {

    private final List<Entry> storageList = new ArrayList<>();
    private final List<Entry> displayList = new ArrayList<>();

    private final SearchPatternBox searchPatternBox;
    private Pattern searchPattern;

    private int selectedIndex = -1;
    private int hoveredIndex = -1;

    private IntPoint lastMousePoint = IntPoint.zero;
    private long mouseStillTime = 0L;

    public FilePickerWidget(Screen parent, IntRect layoutRect, int entryHeight) {
        super(((ExScreen) parent).getMinecraft(), layoutRect, entryHeight);

        this.searchPatternBox = makePatternBox(parent, layoutRect);
    }

    private static SearchPatternBox makePatternBox(Screen parent, IntRect layoutRect) {
        var offset = new IntPoint(2, 0);
        int width = Math.min(layoutRect.width() / 2 - offset.x, 120);
        int x = layoutRect.right() - width - offset.x;
        return new SearchPatternBox(parent, new IntRect(x, layoutRect.y + 2, width, 16));
    }

    @Override
    protected int getEntryCount() {
        return this.getActiveList().size();
    }

    @Override
    protected void entryClicked(int entryIndex, int buttonIndex, boolean doubleClick) {
        if (buttonIndex == 0) {
            this.selectedIndex = entryIndex;
        }
        else if (buttonIndex == 1) {
            Path path = this.getActiveList().get(entryIndex).value();
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
    protected void afterRender(Tesselator ts, IntPoint mouseLocation, float tickTime) {
        super.afterRender(ts, mouseLocation, tickTime);

        this.renderMatchCount();
    }

    private void renderMatchCount() {
        int activeSize = this.getActiveList().size();
        String sizeText;
        if (this.searchPattern == null) {
            sizeText = activeSize + " files";
        }
        else {
            int storageSize = this.getStorageList().size();
            sizeText = "%d of %d match".formatted(activeSize, storageSize);
        }
        var boxRect = this.searchPatternBox.getBoxRect();
        var boxPos = boxRect.botLeft();

        var exText = (ExTextRenderer) this.client.font;
        int width = exText.getTextWidth(sizeText, 0).width();
        int x = boxRect.right() - 6 - width;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        exText.drawString(sizeText, x, boxPos.y + 6, 0xffffff, true);
    }

    @Override
    protected void renderEntry(Tesselator ts, int entryIndex, Point entryLocation, int entryHeight) {
        var exText = (ExTextRenderer) this.client.font;
        Entry entry = this.getActiveList().get(entryIndex);
        String displayName = entry.getDisplayName();

        if (this.selectedIndex == entryIndex || this.hoveredIndex == entryIndex) {
            int width = exText.getTextWidth(displayName, 0).width() + 6;
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
        exText.drawString(displayName, x, y, 0xffffff, true);
    }

    public void charTyped(char codepoint, int key) {
        this.searchPatternBox.charTyped(codepoint, key);
    }

    public void clicked(IntPoint mouseLocation, int buttonIndex) {
        this.searchPatternBox.clicked(mouseLocation, buttonIndex);
    }

    public @Override boolean buttonClicked(Button button) {
        if (this.searchPatternBox.buttonClicked(button)) {
            return true;
        }
        return super.buttonClicked(button);
    }

    public void refresh() {
        for (Entry entry : this.displayList) {
            entry.setDisplayName(null);
        }
        this.displayList.clear();

        if (this.searchPattern != null) {
            this.applySearchPattern(this.searchPattern);
        }
    }

    private void applySearchPattern(Pattern pattern) {
        final var matcher = pattern.matcher("");
        final var builder = new StringBuilder();

        this.storageList.stream().filter(entry -> {
            builder.setLength(0);
            return matchEntry(entry, matcher, builder);
        }).collect(Collectors.toCollection(() -> this.displayList));
    }

    private static boolean matchEntry(Entry entry, Matcher matcher, StringBuilder builder) {
        final String matchStyle = "§3";
        final String resetStyle = "§r";

        // TODO: colorize path before match styling?

        final String fileName = entry.value().getFileName().toString();
        matcher.reset(fileName);

        int last = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            builder.append(fileName, last, start); // append unmatched text ahead of match

            builder.append(matchStyle); // push style
            builder.append(fileName, start, end); // append matched text
            builder.append(resetStyle); // pop style
            last = end;
        }

        if (last == 0) {
            return false;
        }
        builder.append(fileName, last, fileName.length()); // append remaining unmatched text

        entry.setDisplayName(builder.toString());
        return true;
    }

    public void tick() {
        this.searchPatternBox.tick();

        Pattern nextPattern = this.searchPatternBox.getPattern();
        if (this.searchPattern != nextPattern) {
            this.searchPattern = nextPattern;

            this.refresh();
        }
    }

    public @Override void render(IntPoint mouseLocation, float tickTime) {
        super.render(mouseLocation, tickTime);

        this.searchPatternBox.render();

        this.renderHoverTooltipUnderMouse(mouseLocation);
    }

    private void renderHoverTooltipUnderMouse(IntPoint mousePoint) {
        IntPoint mouseDelta = mousePoint.sub(this.lastMousePoint).abs();
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
        Path path = this.getActiveList().get(entryIndex).value();
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
                double size = 32; // scaling test: (Math.sin(System.currentTimeMillis() / 4000.0) + 1.0) * 31 + 1;
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
        int realWidth = (int) Math.round(realRect.width());
        int realHeight = (int) Math.round(realRect.height());

        var flags = List.of(FileIconFlags.Icon, FileIconFlags.Symbolic);
        var options = new FileIconOptions(ImageFormat.RGBA_U8, realWidth, realHeight, 1, flags);
        var iconImage = renderer.getIcon(path, options);
        if (iconImage == null) {
            return;
        }

        var ts = Tesselator.instance;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);

        var texMan = (ExTextureManager) this.client.textures;
        int texId = texMan.loadTexture(iconImage);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        ts.begin();
        DrawUtil.fillRect(ts, rect, new IntCorner(0xffffffff), new Rect(0, 0, 1, 1));
        ts.end();

        texMan.releaseTexture(texId);
    }

    public Entry getSelectedEntry() {
        int index = this.getSelectedIndex();
        if (index != -1) {
            return this.getActiveList().get(index);
        }
        return null;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public int getHoveredIndex() {
        return this.hoveredIndex;
    }

    private boolean canUseStorageList() {
        return this.displayList.isEmpty() && this.searchPattern == null;
    }

    public List<Entry> getActiveList() {
        if (this.canUseStorageList()) {
            return this.storageList;
        }
        return this.displayList;
    }

    public List<Entry> getStorageList() {
        return this.storageList;
    }

    public List<Entry> getDisplayList() {
        return this.displayList;
    }

    public static class Entry {
        private final Path value;
        private String displayName;

        public Entry(Path value) {
            this.value = value;
        }

        public Path value() {
            return value;
        }

        public String getDisplayName() {
            if (this.displayName == null) {
                this.displayName = FileDisplayUtil.colorizePath(this.value.getFileName());
            }
            return this.displayName;
        }

        public void setDisplayName(String value) {
            this.displayName = value;
        }
    }
}