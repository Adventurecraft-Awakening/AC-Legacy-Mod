package dev.adventurecraft.awakening.common;

import java.io.File;
import java.util.List;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.resource.language.TranslationStorage;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class AC_GuiMapSelect extends Screen {
    protected Screen parent;
    private int field_6460_h = 0;
    private int field_6459_i = 32;
    private int field_6458_j = this.height - 55 + 4;
    private int field_6457_l = 0;
    private int field_6456_m = this.width;
    private int field_6455_n = -2;
    private int field_6454_o = -1;
    private String fileLocation = "";
    private String saveName;
    private AC_MapInfo selectedMap;

    public AC_GuiMapSelect(Screen var1, String var2) {
        this.parent = var1;
        this.saveName = var2;
    }

    public void initVanillaScreen() {
        ((ExMinecraft) this.client).getMapList().findMaps();
        TranslationStorage var1 = TranslationStorage.getInstance();
        if (this.saveName == null) {
            this.buttons.add(new OptionButtonWidget(6, this.width / 2 + 5, this.height - 48, var1.translate("gui.done")));
            this.buttons.add(new ButtonWidget(7, this.width / 2 - 155, this.height - 48, 150, 20, "New Map"));
        } else {
            this.buttons.add(new OptionButtonWidget(6, this.width / 2 - 75, this.height - 48, var1.translate("gui.done")));
        }

        this.client.texturePackManager.findTexturePacks();
        Minecraft var10003 = this.client;
        this.fileLocation = (new File(Minecraft.getGameDirectory(), "texturepacks")).getAbsolutePath();
        this.field_6459_i = 32;
        this.field_6458_j = this.height - 58 + 4;
        this.field_6457_l = 0;
        this.field_6456_m = this.width;
    }

    protected void buttonClicked(ButtonWidget var1) {
        if (var1.active) {
            if (var1.id == 6) {
                if (this.selectedMap == null) {
                    this.client.openScreen(this.parent);
                } else {
                    if (this.saveName != null) {
                        if (this.saveName.equals("")) {
                            File var2 = Minecraft.getGameDirectory();
                            File var3 = new File(var2, "saves");
                            int var4 = 1;

                            File var5;
                            do {
                                this.saveName = String.format("%s - Save %d", this.selectedMap.name, var4);
                                var5 = new File(var3, this.saveName);
                                ++var4;
                            } while (var5.exists());
                        }

                        ((ExMinecraft) this.client).saveMapUsed(this.saveName, this.selectedMap.name);
                    }

                    this.client.interactionManager = new SingleplayerInteractionManager(this.client);
                    ((ExMinecraft) this.client).startWorld(this.saveName, this.saveName, 0L, this.selectedMap.name);
                }
            } else if (var1.id == 7) {
                this.client.openScreen(new GuiCreateNewMap(this));
            }

        }
    }

    protected void mouseClicked(int var1, int var2, int var3) {
        super.mouseClicked(var1, var2, var3);
    }

    protected void mouseReleased(int var1, int var2, int var3) {
        super.mouseReleased(var1, var2, var3);
    }

    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        if (this.field_6454_o <= 0) {
            this.client.texturePackManager.findTexturePacks();
            this.field_6454_o += 20;
        }

        List var4 = ((ExMinecraft) this.client).getMapList().availableMaps();
        int var5;
        if (Mouse.isButtonDown(0)) {
            if (this.field_6455_n == -1) {
                if (var2 >= this.field_6459_i && var2 <= this.field_6458_j) {
                    var5 = this.width / 2 - 110;
                    int var6 = this.width / 2 + 110;
                    int var7 = (var2 - this.field_6459_i + this.field_6460_h - 2) / 36;
                    if (var1 >= var5 && var1 <= var6 && var7 >= 0 && var7 < var4.size()) {
                        this.selectedMap = (AC_MapInfo) var4.get(var7);
                    }

                    this.field_6455_n = var2;
                } else {
                    this.field_6455_n = -2;
                }
            } else if (this.field_6455_n >= 0) {
                this.field_6460_h -= var2 - this.field_6455_n;
                this.field_6455_n = var2;
            }
        } else {
            if (this.field_6455_n >= 0 && this.field_6455_n != var2) {
            }

            this.field_6455_n = -1;
        }

        var5 = var4.size() * 36 - (this.field_6458_j - this.field_6459_i - 4);
        if (var5 < 0) {
            var5 /= 2;
        }

        if (this.field_6460_h < 0) {
            this.field_6460_h = 0;
        }

        if (this.field_6460_h > var5) {
            this.field_6460_h = var5;
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator var16 = Tessellator.INSTANCE;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var17 = 32.0F;
        var16.start();
        var16.color(2105376);
        var16.vertex(this.field_6457_l, this.field_6458_j, 0.0D, (float) this.field_6457_l / var17, (float) (this.field_6458_j + this.field_6460_h) / var17);
        var16.vertex(this.field_6456_m, this.field_6458_j, 0.0D, (float) this.field_6456_m / var17, (float) (this.field_6458_j + this.field_6460_h) / var17);
        var16.vertex(this.field_6456_m, this.field_6459_i, 0.0D, (float) this.field_6456_m / var17, (float) (this.field_6459_i + this.field_6460_h) / var17);
        var16.vertex(this.field_6457_l, this.field_6459_i, 0.0D, (float) this.field_6457_l / var17, (float) (this.field_6459_i + this.field_6460_h) / var17);
        var16.tessellate();

        for (int var8 = 0; var8 < var4.size(); ++var8) {
            AC_MapInfo var9 = (AC_MapInfo) var4.get(var8);
            int var10 = this.width / 2 - 92 - 16;
            int var11 = 36 + var8 * 36 - this.field_6460_h;
            byte var12 = 32;
            byte var13 = 32;
            if (var9 == this.selectedMap) {
                int var14 = this.width / 2 - 110;
                int var15 = this.width / 2 + 110;
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                var16.start();
                var16.color(8421504);
                var16.vertex(var14, var11 + var12 + 2, 0.0D, 0.0D, 1.0D);
                var16.vertex(var15, var11 + var12 + 2, 0.0D, 1.0D, 1.0D);
                var16.vertex(var15, var11 - 2, 0.0D, 1.0D, 0.0D);
                var16.vertex(var14, var11 - 2, 0.0D, 0.0D, 0.0D);
                var16.color(0);
                var16.vertex(var14 + 1, var11 + var12 + 1, 0.0D, 0.0D, 1.0D);
                var16.vertex(var15 - 1, var11 + var12 + 1, 0.0D, 1.0D, 1.0D);
                var16.vertex(var15 - 1, var11 - 1, 0.0D, 1.0D, 0.0D);
                var16.vertex(var14 + 1, var11 - 1, 0.0D, 0.0D, 0.0D);
                var16.tessellate();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }

            var9.bindTexture(this.client);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            var16.start();
            var16.color(16777215);
            var16.vertex(var10, var11 + var12, 0.0D, 0.0D, 1.0D);
            var16.vertex(var10 + var13, var11 + var12, 0.0D, 1.0D, 1.0D);
            var16.vertex(var10 + var13, var11, 0.0D, 1.0D, 0.0D);
            var16.vertex(var10, var11, 0.0D, 0.0D, 0.0D);
            var16.tessellate();
            this.drawTextWithShadow(this.textRenderer, var9.name, var10 + var13 + 2, var11 + 1, 16777215);
            this.drawTextWithShadow(this.textRenderer, var9.description1, var10 + var13 + 2, var11 + 12, 8421504);
            this.drawTextWithShadow(this.textRenderer, var9.description2, var10 + var13 + 2, var11 + 12 + 10, 8421504);
        }

        byte var18 = 4;
        this.drawBackground(0, this.field_6459_i, 255, 255);
        this.drawBackground(this.field_6458_j, this.height, 255, 255);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        var16.start();
        var16.color(0, 0);
        var16.vertex(this.field_6457_l, this.field_6459_i + var18, 0.0D, 0.0D, 1.0D);
        var16.vertex(this.field_6456_m, this.field_6459_i + var18, 0.0D, 1.0D, 1.0D);
        var16.color(0, 255);
        var16.vertex(this.field_6456_m, this.field_6459_i, 0.0D, 1.0D, 0.0D);
        var16.vertex(this.field_6457_l, this.field_6459_i, 0.0D, 0.0D, 0.0D);
        var16.tessellate();
        var16.start();
        var16.color(0, 255);
        var16.vertex(this.field_6457_l, this.field_6458_j, 0.0D, 0.0D, 1.0D);
        var16.vertex(this.field_6456_m, this.field_6458_j, 0.0D, 1.0D, 1.0D);
        var16.color(0, 0);
        var16.vertex(this.field_6456_m, this.field_6458_j - var18, 0.0D, 1.0D, 0.0D);
        var16.vertex(this.field_6457_l, this.field_6458_j - var18, 0.0D, 0.0D, 0.0D);
        var16.tessellate();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        TranslationStorage var19 = TranslationStorage.getInstance();
        this.drawTextWithShadowCentred(this.textRenderer, var19.translate("mapList.title"), this.width / 2, 16, 16777215);
        super.render(var1, var2, var3);
    }

    public void tick() {
        super.tick();
        --this.field_6454_o;
    }

    public void drawBackground(int var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var6 = 32.0F;
        var5.start();
        var5.color(4210752, var4);
        var5.vertex(0.0D, var2, 0.0D, 0.0D, (float) var2 / var6);
        var5.vertex(this.width, var2, 0.0D, (float) this.width / var6, (float) var2 / var6);
        var5.color(4210752, var3);
        var5.vertex(this.width, var1, 0.0D, (float) this.width / var6, (float) var1 / var6);
        var5.vertex(0.0D, var1, 0.0D, 0.0D, (float) var1 / var6);
        var5.tessellate();
    }
}
