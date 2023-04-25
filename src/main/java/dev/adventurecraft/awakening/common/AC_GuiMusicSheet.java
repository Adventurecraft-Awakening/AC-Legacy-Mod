package dev.adventurecraft.awakening.common;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class AC_GuiMusicSheet extends Screen {
    private String instrument;
    private IntArrayList notesPlayed;
    private String notesPlayedString;
    private int spaceTaken;
    private String songPlayed;
    private long timeToFade;

    public AC_GuiMusicSheet(String var1) {
        this.instrument = var1;
        this.notesPlayed = new IntArrayList();
        this.notesPlayedString = "";
        this.songPlayed = null;
    }

    public void tick() {
    }

    public void initVanillaScreen() {
    }

    protected void keyPressed(char var1, int var2) {
        super.keyPressed(var1, var2);
        if (this.songPlayed == null && var2 >= 2 && var2 <= 11) {
            boolean var3 = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if (var3 && (var2 == 2 || var2 == 4 || var2 == 5 || var2 == 6 || var2 == 8 || var2 == 9 || var2 == 11)) {
                if (this.spaceTaken + 25 >= 168) {
                    this.notesPlayed.clear();
                    this.notesPlayedString = "";
                    this.spaceTaken = 0;
                }

                this.notesPlayed.add(-var2);
                this.spaceTaken += 14;
            }

            if (this.spaceTaken + 11 >= 168) {
                this.notesPlayed.clear();
                this.notesPlayedString = "";
                this.spaceTaken = 0;
            }

            this.spaceTaken += 11;
            this.notesPlayed.add(var2);

            /* TODO
            if (var2 == 2) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'D', var3, 0.5F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + var1;
            } else if (var2 == 3) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'E', false, 0.5F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + "2";
            } else if (var2 == 4) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'F', var3, 0.5F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + var1;
            } else if (var2 == 5) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'G', var3, 0.5F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + var1;
            } else if (var2 == 6) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'A', var3, 1.0F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + var1;
            } else if (var2 == 7) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'B', false, 1.0F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + "6";
            } else if (var2 == 8) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'C', var3, 1.0F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + var1;
            } else if (var2 == 9) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'D', var3, 1.0F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + var1;
            } else if (var2 == 10) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'E', false, 1.0F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + "9";
            } else if (var2 == 11) {
                MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'F', var3, 1.0F, 1.0F);
                this.notesPlayedString = this.notesPlayedString + var1;
            }

            String var4 = this.client.world.musicScripts.executeMusic(this.notesPlayedString);
            if (var4 != null) {
                this.songPlayed = var4;
                this.timeToFade = 2500L + System.currentTimeMillis();
                this.client.hasFocus = true;
                this.disableInputGrabbing = true;
                this.client.mouseHelper.grabCursor();
            }
            */
        }
    }

    public void render(int var1, int var2, float var3) {
        float var4 = 1.0F;
        int var5 = Integer.MIN_VALUE;
        int var6 = -16777216;
        if (this.songPlayed != null) {
            float var7 = (float) (this.timeToFade - System.currentTimeMillis()) / 1000.0F;
            if (var7 < 1.0F) {
                var4 = var7;
                var5 = (int) (128.0F * var7) << 24;
                var6 = (int) (255.0F * var7) << 24;
                if ((double) var7 < 0.004D) {
                    this.client.openScreen((Screen) null);
                    return;
                }
            }
        }

        this.fill((this.width - 215) / 2, this.height - 59 - 4 - 48, (this.width + 215) / 2, this.height - 48, var5);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        if (this.songPlayed != null) {
            this.drawTextWithShadowCentred(this.textRenderer, this.songPlayed, this.width / 2, this.height - 59 - 48, 14737632 + var6);
        }

        int var11 = this.client.textureManager.getTextureId("/gui/musicSheet.png");
        GL11.glColor4f(0.9F, 0.1F, 0.1F, var4);
        this.client.textureManager.bindTexture(var11);
        this.blit((this.width - 205) / 2, this.height - 59 - 2 - 48, 0, 0, 205, 59);
        int var8 = 0;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, var4);

        for (int var10 : this.notesPlayed) {
            if (var10 > 0) {
                this.drawNote(var8, var10);
                var8 += 11;
            } else {
                this.drawSharp(var8, -var10);
                var8 += 14;
            }
        }

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        super.render(var1, var2, var3);
    }

    private void drawNote(int var1, int var2) {
        this.blit((this.width - 205) / 2 + 36 + var1, this.height - 59 - 2 - 48 + 46 - (var2 - 2) * 4, 0, 64, 9, 7);
    }

    private void drawSharp(int var1, int var2) {
        this.blit((this.width - 205) / 2 + 36 + var1, this.height - 59 - 2 - 48 + 46 - (var2 - 2) * 4 - 5, 16, 64, 12, 17);
    }

    public static void showUI(String var0) {
        Minecraft.instance.openScreen(new AC_GuiMusicSheet(var0));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
