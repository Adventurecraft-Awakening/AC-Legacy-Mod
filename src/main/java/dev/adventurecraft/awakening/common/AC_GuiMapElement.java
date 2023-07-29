package dev.adventurecraft.awakening.common;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.texture.TextureManager;
import dev.adventurecraft.awakening.script.ScriptUIContainer;
import dev.adventurecraft.awakening.script.ScriptUILabel;
import dev.adventurecraft.awakening.script.ScriptUIRect;
import dev.adventurecraft.awakening.script.ScriptUISprite;

public class AC_GuiMapElement extends ScriptUIContainer {

    ScriptUIRect topBack;
    ScriptUIRect ratingBack;
    ScriptUISprite ratingBar;
    ScriptUIRect botFadeBack;
    ScriptUISprite background;
    ScriptUILabel[] descriptions;
    ScriptUIRect topFadeBack;
    ScriptUILabel[] topFadeText;
    long fadeTimePrev;
    boolean fadeIn = false;
    boolean fadeOut = false;
    public String mapName;
    public String mapURL;
    int mapID;
    int totalRating = 0;
    int numRatings = 0;
    int voted = 0;
    boolean hoveringOverRating = false;
    private boolean downloaded;

    public AC_GuiMapElement(
        int var1, int var2, ScriptUIContainer var3,
        String var4, String var5, String var6, String var7, String var8,
        int var9, int var10, int var11) {
        super((float) var1, (float) var2, var3);

        ArrayList<String> var12 = new ArrayList<>();
        this.background = new ScriptUISprite(var7, 0.0F, 0.0F, 100.0F, 100.0F, 0.0D, 0.0D, this);
        this.topBack = new ScriptUIRect(0.0F, 0.0F, 100.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.5F, this);
        this.mapID = var9;
        this.ratingBack = new ScriptUIRect(0.0F, 85.0F, 100.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.5F, this);
        this.ratingBar = new ScriptUISprite("/misc/adventureCraftMisc.png", 0.0F, 86.0F, 64.0F, 13.0F, 0.0D, 0.0D, this);
        this.totalRating = var10;
        this.numRatings = var11;
        this.updateRatingBar();
        int var13 = 0;
        String[] var14 = var4.split("\n");
        String[] var15 = var14;
        int var16 = var14.length;

        int var17;
        String var18;
        for (var17 = 0; var17 < var16; ++var17) {
            var18 = var15[var17];
            var12.clear();
            this.getLines(var18, var12);

            for (String var20 : var12) {
                new ScriptUILabel(var20, 2.0F, (float) (2 + var13 * 10), this);
                ++var13;
            }
        }

        this.topBack.height = (float) (2 + 10 * var13);
        var13 = 0;
        var14 = var6.split("\n");
        this.botFadeBack = new ScriptUIRect(0.0F, 0.0F, 100.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F, this);
        this.botFadeBack.height = (float) (2 + 10 * var14.length);
        this.botFadeBack.setY((float) (98 - (var14.length - var13) * 10 - 13));
        this.descriptions = new ScriptUILabel[var14.length];
        var15 = var14;
        var16 = var14.length;

        for (var17 = 0; var17 < var16; ++var17) {
            var18 = var15[var17];
            this.descriptions[var13] = new ScriptUILabel(var18, 2.0F, (float) (100 - (var14.length - var13) * 10 - 13), this);
            this.descriptions[var13].alpha = 0.0F;
            ++var13;
        }

        if (var6.equals("")) {
            this.botFadeBack.removeFromScreen();
        }

        var13 = 0;
        var14 = var5.split("\n");
        this.topFadeBack = new ScriptUIRect(0.0F, this.topBack.height, 100.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F, this);
        this.topFadeBack.height = (float) (10 * var14.length);
        this.topFadeText = new ScriptUILabel[var14.length];
        var15 = var14;
        var16 = var14.length;

        for (var17 = 0; var17 < var16; ++var17) {
            var18 = var15[var17];
            this.topFadeText[var13] = new ScriptUILabel(var18, 2.0F, (float) ((int) ((float) (var13 * 10) + this.topFadeBack.curY)), this);
            this.topFadeText[var13].alpha = 0.0F;
            ++var13;
        }

        if (var5.equals("")) {
            this.topFadeBack.removeFromScreen();
        }

        this.background.imageHeight = 100.0F;
        this.background.imageWidth = 100.0F;
        this.mapName = var4.replace("\n", " ");
        this.mapURL = var8;
    }

    void updateRatingBar() {
        float var1;
        if (this.voted != 0) {
            var1 = 12.8F * (float) this.voted;
            this.ratingBar.width = (float) Math.round(var1);
        } else if (this.numRatings == 0) {
            this.ratingBar.width = 0.0F;
        } else {
            var1 = 12.8F * (float) this.totalRating / (float) this.numRatings;
            this.ratingBar.width = (float) Math.round(var1);
        }
    }

    void mouseMoved(int var1, int var2) {
        if (0 <= var1 && var1 <= 64 && 85 < var2 && var2 < 100) {
            this.ratingBar.width = (float) Math.min((var1 / 13 + 1) * 13, 64);
            this.hoveringOverRating = true;
        } else if (this.hoveringOverRating) {
            this.updateRatingBar();
            this.hoveringOverRating = false;
        }
    }

    void ratingClicked(int var1, int var2) {
        if (this.voted != 0) {
            this.totalRating -= this.voted;
            --this.numRatings;
        }

        this.voted = var1 / 13 + 1;
        SwingUtilities.invokeLater(() -> {
            try {
                URL var11 = new URL(String.format("http://www.adventurecraft.org/cgi-bin/vote.py?mapID=%d&rating=%d", AC_GuiMapElement.this.mapID, AC_GuiMapElement.this.voted));
                URLConnection var21 = var11.openConnection();
                var21.connect();
                var21.getInputStream();
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        });
    }

    void getLines(String text, List<String> output) {
        String[] var3 = text.split(" ");
        String var4 = "";
        String[] var5 = var3;
        int var6 = var3.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            String var8 = var5[var7];
            if (var4.equals("")) {
                var4 = var8;
            } else {
                String var9 = var4 + " " + var8;
                if (Minecraft.instance.textRenderer.getTextWidth(var9) > 100) {
                    output.add(var4);
                    var4 = var8;
                } else {
                    var4 = var9;
                }
            }
        }

        if (!var4.equals("")) {
            output.add(var4);
        }
    }

    void fadeDescriptionIn() {
        this.fadeIn = true;
        this.fadeOut = false;
        this.fadeTimePrev = System.nanoTime();
    }

    void fadeDescriptionOut() {
        this.fadeIn = false;
        this.fadeOut = true;
        this.fadeTimePrev = System.nanoTime();
    }

    @Override
    public void render(TextRenderer textRenderer, TextureManager texManager, float deltaTime) {
        if (this.fadeIn || this.fadeOut) {
            long var4 = System.nanoTime();
            long var6 = var4 - this.fadeTimePrev;
            float var8 = (float) (2L * var6) / 1.0E9F;
            if (var8 != 0.0F) {
                this.fadeTimePrev = var4;
                ScriptUILabel[] var9 = this.descriptions;
                int var10 = var9.length;

                int var11;
                ScriptUILabel var12;
                for (var11 = 0; var11 < var10; ++var11) {
                    var12 = var9[var11];
                    if (this.fadeIn) {
                        var12.alpha += var8;
                    } else {
                        var12.alpha -= var8;
                    }

                    var12.alpha = Math.max(Math.min(var12.alpha, 1.0F), 0.0F);
                }

                var9 = this.topFadeText;
                var10 = var9.length;

                for (var11 = 0; var11 < var10; ++var11) {
                    var12 = var9[var11];
                    if (this.fadeIn) {
                        var12.alpha += var8;
                    } else {
                        var12.alpha -= var8;
                    }

                    var12.alpha = Math.max(Math.min(var12.alpha, 1.0F), 0.0F);
                }

                if (this.fadeIn) {
                    this.botFadeBack.alpha += var8 / 2.0F;
                    this.topFadeBack.alpha += var8 / 2.0F;
                } else {
                    this.botFadeBack.alpha -= var8 / 2.0F;
                    this.topFadeBack.alpha -= var8 / 2.0F;
                }

                this.botFadeBack.alpha = Math.max(Math.min(this.botFadeBack.alpha, 0.5F), 0.0F);
                this.topFadeBack.alpha = Math.max(Math.min(this.topFadeBack.alpha, 0.5F), 0.0F);
                if (this.botFadeBack.alpha <= 0.0F || this.botFadeBack.alpha >= 0.5F) {
                    this.fadeIn = false;
                    this.fadeOut = false;
                }
            }
        }

        super.render(textRenderer, texManager, deltaTime);
    }

    public void setAsDownloaded() {
        if (!this.downloaded) {
            int var1 = Minecraft.instance.textRenderer.getTextWidth("Downloaded");
            new ScriptUIRect(0.0F, 0.0F, 100.0F, 100.0F, 0.0F, 0.0F, 0.0F, 0.5F, this);
            new ScriptUILabel("Downloaded", (float) (50 - var1 / 2), 46.0F, this);
            this.downloaded = true;
        }
    }
}
