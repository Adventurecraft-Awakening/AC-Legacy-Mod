package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.state.SamplerState;
import dev.adventurecraft.awakening.client.render.state.BlendState;
import dev.adventurecraft.awakening.client.render.state.CompareOp;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.ImageMipmapper;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.*;
import dev.adventurecraft.awakening.util.DrawUtil;
import dev.adventurecraft.awakening.client.render.resource.TextureInfo;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.input.Mouse;

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
        try (var ui = LegacyPassBridge.uiContext(this.font, this.minecraft.textures)) {
            ui.fill(new Rect(0, 0, this.width, this.height), Rgba.withAlpha(0, 128));
            LegacyPassBridge.legacyUi(ui, () -> super.render(mouseX, mouseY, tick), false);

            var textures = this.minecraft.textures;
            var fnt = ((ExTextRenderer) this.font).createState();
            int padding = 4;
            int yOffset = padding + this.scrollY;
            var color = new IntCorner(Rgba.WHITE);
            var uv = new Rect(0, 0, 1, 1);
            var images = ui.withParameters(ui.parameters()
                .withMaterial(ui.parameters().material().withAlpha(CompareOp.GREATER, .1F))
                .withRaster(ui.parameters().raster().withBlend(BlendState.OPAQUE)));
            var infoList = new IdentityHashMap<TextureInfo, ArrayList<MipLevel>>();

            for (var entry : ((dev.adventurecraft.awakening.extension.client.ExTextureManager) textures).textureCatalog().assets()) {
                RenderTexture texture = entry.texture();
                var info = new TextureInfo(entry.asset().path(), texture);
                int x = info.size.w - padding;
                int y = info.size.h - yOffset;
                var totalRect = new IntRect(info.size.w - x, info.size.h - y, 0, 0);
                var mipLevels = new ArrayList<MipLevel>();
                var oldSampler = texture.sampler();
                var sampler = new SamplerState(SamplerState.Filter.LINEAR, SamplerState.Filter.NEAREST,
                    SamplerState.MipmapMode.NONE, oldSampler.wrapU(), oldSampler.wrapV());

                int maxLevel = ImageMipmapper.clampLevel(info.size, info.maxLevel);
                for (int level = 0; level <= maxLevel; level++) {
                    Size size = texture.size(level);
                    if (size.isEmpty()) break;
                    var rect = new IntRect(info.size.w - x, info.size.h - y, size.w, size.h);
                    if (level % 2 == 0) x -= size.w + padding;
                    else y -= size.h + padding;

                    // Sampling is local to this command; the source atlas is never mutated.
                    try (var mip = texture.sampled(level, level, sampler)) {
                        images.image(mip, rect.asFloat(), uv, color);
                        images.flush();
                    }
                    totalRect = totalRect.union(rect);
                    mipLevels.add(new MipLevel(level, rect));
                }
                mipLevels.add(new MipLevel(-1, totalRect));
                infoList.put(info, mipLevels);
                yOffset += info.size.h + 4;
            }

            var mousePos = new IntPoint(mouseX, mouseY);
            for (Map.Entry<TextureInfo, ArrayList<MipLevel>> entry : infoList.entrySet()) {
                var info = entry.getKey();
                var mipLevels = entry.getValue();
                var metaMip = mipLevels.getLast();
                if (metaMip.level() != -1) throw new IllegalStateException("expected last mip to store bounding box");
                if (!metaMip.rect().contains(mousePos)) continue;
                for (MipLevel mip : mipLevels) {
                    if (!mip.rect().contains(mousePos)) continue;
                    int x = mousePos.x;
                    int y = mousePos.y;
                    var mouseRect = new Rect(x, y, 300, 94);
                    DrawUtil.drawRect(ui.vertexWriter(null), mip.rect().asFloat(), new Border(-1),
                        new IntBorder(color.topLeft()));
                    ui.fill(mouseRect, 0xe0000000);
                    int textX = x + 5;
                    int textY = y + 5;
                    fnt.setColor(0xffdddddd);
                    fnt.setShadowToColor();
                    fnt.setShadowOffset(1, 1);
                    fnt.begin(ui);
                    fnt.drawText(info.name, textX, textY);
                    fnt.drawText("Mip Level: " + mip.level(), textX, textY + 11);
                    fnt.end();
                    break;
                }
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
