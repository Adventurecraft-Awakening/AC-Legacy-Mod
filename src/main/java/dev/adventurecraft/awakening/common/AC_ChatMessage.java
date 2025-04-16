package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;

import java.util.ArrayList;

public class AC_ChatMessage {

    public final String text;

    public int age;

    public ArrayList<Line> lines;
    public int width;
    public int height;

    public AC_ChatMessage(String text) {
        this.text = text;
        this.lines = new ArrayList<>();
    }

    public void rebuild(ExTextRenderer renderer, long maxWidth) {
        final int textLength = text.length();
        int offset = 0;
        int width = 0;
        int height = 0;

        lines.clear();

        do {
            TextRect rect = renderer.getTextWidth(text, offset, textLength, maxWidth, true);
            if (rect.charCount() == 0) {
                break;
            }

            int lineEnd = offset + rect.charCount();
            int last = lineEnd - 1;
            if (last > 0 && last < text.length() && text.charAt(last) == '\n') {
                lineEnd -= 1;
            }

            lines.add(new Line(offset, lineEnd, rect.width()));

            offset += rect.charCount();
            width = Math.max(rect.width(), width);
            height += 9;
        }
        while (true);

        this.width = width;
        this.height = height;
    }

    public record Line(int start, int end, int width) {
    }
}
