package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.chat.Component;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;

public class AC_ChatMessage {

    public final Component content;
    public final long timestamp;

    public final ArrayList<Line> lines;
    public int width;
    public int height;
    public int maxWidth;

    public AC_ChatMessage(Component content, long timestamp) {
        this.content = content;
        this.timestamp = timestamp;
        this.lines = new ArrayList<>();
    }

    public long getAgeInMillis() {
        return System.currentTimeMillis() - this.timestamp;
    }

    public void rebuild(Font font, int maxWidth) {
        final String text = this.content.getString();
        final int textLength = text.length();
        final var exFont = (ExTextRenderer) font;

        int offset = 0;
        int width = 0;
        int height = 0;

        this.lines.clear();
        do {
            TextRect rect = exFont.measureText(text, offset, textLength, maxWidth, true);
            if (rect.charCount() == 0) {
                break;
            }

            int lineEnd = offset + rect.charCount();
            int last = lineEnd - 1;
            if (last > 0 && last < text.length() && text.charAt(last) == '\n') {
                lineEnd -= 1;
            }

            this.lines.add(new Line(text.substring(offset, lineEnd), rect.width()));

            offset += rect.charCount();
            width = Math.max(rect.width(), width);
            height += 9;
        }
        while (true);

        this.width = width;
        this.height = height;
        this.maxWidth = maxWidth;
    }

    public record Line(CharSequence content, int width) {
    }
}
