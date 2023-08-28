package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.resource.language.TranslationStorage;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.List;

public class AC_GuiMapSelect extends Screen {

    protected Screen parent;
    private String saveName;

    private MapList mapList;

    public AC_GuiMapSelect(Screen parent, String saveName) {
        this.parent = parent;
        this.saveName = saveName;
    }

    @Override
    public void initVanillaScreen() {
        ((ExMinecraft) this.client).getMapList().findMaps();
        var ts = TranslationStorage.getInstance();
        if (this.saveName == null) {
            this.buttons.add(new OptionButtonWidget(6, this.width / 2 + 5, this.height - 48, ts.translate("gui.done")));
            this.buttons.add(new ButtonWidget(7, this.width / 2 - 155, this.height - 48, 150, 20, ts.translate("mapList.newMap")));
        } else {
            this.buttons.add(new OptionButtonWidget(6, this.width / 2 - 75, this.height - 48, ts.translate("gui.done")));
        }

        this.mapList = new MapList();
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (!button.active) {
            return;
        }

        if (button.id == 6) {
            AC_MapInfo selectedMap = this.mapList.selectedMap;
            if (selectedMap == null) {
                this.client.openScreen(this.parent);
            } else {
                if (this.saveName != null) {
                    if (this.saveName.equals("")) {
                        File gameDir = Minecraft.getGameDirectory();
                        File savesDir = new File(gameDir, "saves");
                        int saveIndex = 1;

                        File saveDir;
                        do {
                            this.saveName = String.format("%s - Save %d", selectedMap.name, saveIndex);
                            saveDir = new File(savesDir, this.saveName);
                            ++saveIndex;
                        } while (saveDir.exists());
                    }

                    ((ExMinecraft) this.client).saveMapUsed(this.saveName, selectedMap.name);
                }

                this.client.interactionManager = new SingleplayerInteractionManager(this.client);
                ((ExMinecraft) this.client).startWorld(this.saveName, this.saveName, 0L, selectedMap.name);
            }
        } else if (button.id == 7) {
            this.client.openScreen(new GuiCreateNewMap(this));
        }
    }

    @Override
    public void onMouseEvent() {
        super.onMouseEvent();
        this.mapList.onMouseEvent();
    }

    @Override
    public void render(int mouseX, int mouseY, float tickTime) {
        this.renderBackground();

        this.mapList.render(mouseX, mouseY, tickTime);

        var translation = TranslationStorage.getInstance();
        this.drawTextWithShadowCentred(this.textRenderer, translation.translate("mapList.title"), this.width / 2, 16, 16777215);
        super.render(mouseX, mouseY, tickTime);
    }

    public void tick() {
        super.tick();
    }

    @Environment(value = EnvType.CLIENT)
    public class MapList extends ScrollableWidget {

        private List<AC_MapInfo> maps;
        private int hoveredEntry = -1;
        private int selectedEntry = -1;
        private AC_MapInfo selectedMap;

        public MapList() {
            super(
                AC_GuiMapSelect.this.client,
                0, 0, AC_GuiMapSelect.this.width, AC_GuiMapSelect.this.height,
                32, AC_GuiMapSelect.this.height - 58 + 4, 36);

            this.setContentTopPadding(4);

            this.maps = ((ExMinecraft) this.client).getMapList().getMaps();
        }

        @Override
        protected int getEntryCount() {
            return this.maps.size();
        }

        @Override
        protected void entryClicked(int entryIndex, boolean doubleClick) {
            this.selectedEntry = entryIndex;
            if (entryIndex != -1) {
                this.selectedMap = maps.get(entryIndex);
            } else {
                this.selectedMap = null;
            }
        }

        @Override
        protected void beforeEntryRender(int mouseX, int mouseY, double entryX, double entryY, Tessellator ts) {
            super.beforeEntryRender(mouseX, mouseY, entryX, entryY, ts);

            this.hoveredEntry = this.getEntryUnderPoint(mouseX, mouseY);
        }

        @Override
        protected void renderEntry(int entryIndex, double entryX, double entryY, int entryHeight, Tessellator ts) {
            AC_MapInfo mapInfo = this.maps.get(entryIndex);
            var exText = (ExTextRenderer) AC_GuiMapSelect.this.textRenderer;

            int iconX = (int) entryX - 110 + 2;
            int iconY = (int) entryY + 2;
            int iconWidth = 32;
            int iconHeight = 32;

            if (this.selectedEntry == entryIndex || this.hoveredEntry == entryIndex) {
                int size = 110;
                int nameWidth = exText.getTextWidth(mapInfo.name, 0).width();
                int desc1Width = exText.getTextWidth(mapInfo.description1, 0).width();
                int desc2Width = exText.getTextWidth(mapInfo.description2, 0).width();
                int entryWidth = Math.max(size * 2, iconWidth + Math.max(nameWidth, Math.max(desc1Width, desc2Width)) + 6);
                boolean isHover = this.selectedEntry != entryIndex && this.hoveredEntry == entryIndex;
                int borderColor = isHover ? 0x80808080 : 0xff808080;
                int backColor = isHover ? 0x80000000 : 0xff000000;

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                ts.start();
                this.renderContentSelection(
                    iconX - 2, iconY - 2, entryWidth, entryHeight, 1, borderColor, backColor, ts);
                ts.tessellate();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
            }

            mapInfo.bindTexture(this.client.textureManager);
            ts.start();
            ts.color(16777215);
            ts.vertex(iconX, iconY + iconHeight, 0.0D, 0.0D, 1.0D);
            ts.vertex(iconX + iconWidth, iconY + iconHeight, 0.0D, 1.0D, 1.0D);
            ts.vertex(iconX + iconWidth, iconY, 0.0D, 1.0D, 0.0D);
            ts.vertex(iconX, iconY, 0.0D, 0.0D, 0.0D);
            ts.tessellate();

            int textY = iconY;
            int textX = iconX + iconWidth;

            exText.drawString(mapInfo.name, textX + 2, textY + 1, 0xFFFFFF, true);
            exText.drawString(mapInfo.description1, textX + 2, textY + 12, 0x808080, true);
            exText.drawString(mapInfo.description2, textX + 2, textY + 12 + 10, 0x808080, true);
        }
    }
}
