package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.gl.GLTexture;
import dev.adventurecraft.awakening.client.gl.GLTextureTarget;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.ImageMipmapper;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.*;
import dev.adventurecraft.awakening.util.DrawUtil;
import dev.adventurecraft.awakening.client.gl.GLTextureInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

public class AC_GuiTextureAtlas extends Screen {

    private int scrollY;

    @Override
    public void mouseEvent() {
        super.mouseEvent();

        int mouseScrollYDelta = Mouse.getEventDWheel();
        this.scrollY += (int) ((double) mouseScrollYDelta * 10);
    }

    @Override
    public void render(int mouseX, int mouseY, float tick) {
        this.fill(0, 0, this.width, this.height, Rgba.fromRgba8(0, 0, 0, 128));

        super.render(mouseX, mouseY, tick);

        var textures = this.minecraft.textures;
        var ts = Tesselator.instance;
        var fnt = ((ExTextRenderer) this.font).createState();

        final GLTextureTarget target = GLTextureTarget.TEXTURE_2D;

        int padding = 4;
        int yOffset = padding + this.scrollY;

        var color = new IntCorner(0xffffffff);
        var uv = new Rect(0, 0, 1, 1);

        var infoList = new IdentityHashMap<GLTextureInfo, ArrayList<MipLevel>>();

        for (var entry : ((Map<String, Integer>) textures.idMap).entrySet()) {
            int id = entry.getValue();
            GL11.glBindTexture(target.symbol, id);

            var info = new GLTextureInfo(target, entry.getKey(), id);
            int x = info.size.w - padding;
            int y = info.size.h - yOffset;

            GL11.glTexParameteri(target.symbol, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(target.symbol, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            var totalRect = new IntRect(info.size.w - x, info.size.h - y, 0, 0);
            var mipLevels = new ArrayList<MipLevel>();

            int maxLevel = ImageMipmapper.clampLevel(info.size, info.maxLevel);
            for (int level = 0; level <= maxLevel; level++) {
                Size size = GLTexture.getSize(target, level);
                if (size.isEmpty()) {
                    break;
                }

                var rect = new IntRect(info.size.w - x, info.size.h - y, size.w, size.h);
                if (level % 2 == 0) {
                    x -= size.w + padding;
                }
                else {
                    y -= size.h + padding;
                }

                // Force this level to be drawn.
                GL11.glTexParameteri(target.symbol, GL12.GL_TEXTURE_BASE_LEVEL, level);
                GL11.glTexParameteri(target.symbol, GL12.GL_TEXTURE_MAX_LEVEL, level);

                ts.begin();
                DrawUtil.fillRect(ts, rect.asFloat(), color, uv);
                ts.end();

                totalRect = totalRect.union(rect);
                mipLevels.add(new MipLevel(level, rect));
            }

            info.restoreParameters(target);

            mipLevels.add(new MipLevel(-1, totalRect));
            infoList.put(info, mipLevels);

            yOffset += info.size.h + 4;
        }

        GL11.glBindTexture(target.symbol, 0);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        var mousePos = new IntPoint(mouseX, mouseY);

        for (Map.Entry<GLTextureInfo, ArrayList<MipLevel>> entry : infoList.entrySet()) {
            var info = entry.getKey();
            var mipLevels = entry.getValue();

            var metaMip = mipLevels.getLast();
            if (metaMip.level() != -1) {
                throw new IllegalStateException("expected last mip to store bounding box");
            }

            if (!metaMip.rect().contains(mousePos)) {
                continue;
            }

            for (MipLevel mip : mipLevels) {
                if (!mip.rect().contains(mousePos)) {
                    continue;
                }

                int x = mousePos.x;
                int y = mousePos.y;
                var mouseRect = new Rect(x, y, 150 + 150, 84 + 10);

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                ts.begin();
                DrawUtil.drawRect(ts, mip.rect().asFloat(), new Border(-1), new IntBorder(color.topLeft()));

                DrawUtil.fillRect(ts, mouseRect, new IntCorner(0xe0000000), null);
                ts.end();

                int textX = x + 5;
                int textY = y + 5;
                int textColor = 0xffdddddd;

                fnt.setColor(textColor);
                fnt.setShadowToColor();
                fnt.setShadowOffset(1f, 1f);

                fnt.begin(ts);
                fnt.drawText(info.name, textX, textY);
                textY += 11;

                fnt.drawText("Mip Level: " + mip.level(), textX, textY);
                fnt.end();
                break;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    record MipLevel(int level, IntRect rect) {
    }
}
