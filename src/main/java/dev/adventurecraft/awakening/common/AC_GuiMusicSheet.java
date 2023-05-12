package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.world.ExWorld;
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
    private AC_MusicScriptEntry songPlayed;
    private long timeToFade;

    public AC_GuiMusicSheet(String var1) {
        this.instrument = var1;
        this.notesPlayed = new IntArrayList();
        this.notesPlayedString = "";
        this.songPlayed = null;
    }

    @Override
    public void tick() {
    }

    @Override
    public void initVanillaScreen() {
    }

    @Override
    protected void keyPressed(char character, int key) {
        super.keyPressed(character, key);
        if (this.songPlayed != null || key < 2 || key > 11) {
            return;
        }

        boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (shiftDown && (key == 2 || key == 4 || key == 5 || key == 6 || key == 8 || key == 9 || key == 11)) {
            if (this.spaceTaken + 25 >= 168) {
                this.notesPlayed.clear();
                this.notesPlayedString = "";
                this.spaceTaken = 0;
            }

            this.notesPlayed.add(-key);
            this.spaceTaken += 14;
        }

        if (this.spaceTaken + 11 >= 168) {
            this.notesPlayed.clear();
            this.notesPlayedString = "";
            this.spaceTaken = 0;
        }

        this.spaceTaken += 11;
        this.notesPlayed.add(key);

        if (key == 2) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'D', shiftDown, 0.5F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + character;
        } else if (key == 3) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'E', false, 0.5F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "2";
        } else if (key == 4) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'F', shiftDown, 0.5F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + character;
        } else if (key == 5) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'G', shiftDown, 0.5F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + character;
        } else if (key == 6) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'A', shiftDown, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + character;
        } else if (key == 7) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'B', false, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "6";
        } else if (key == 8) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'C', shiftDown, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + character;
        } else if (key == 9) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'D', shiftDown, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + character;
        } else if (key == 10) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'E', false, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "9";
        } else if (key == 11) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'F', shiftDown, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + character;
        }

        AC_MusicScriptEntry entry = ((ExWorld) this.client.world).getMusicScripts().executeMusic(this.notesPlayedString);
        if (entry != null) {
            this.songPlayed = entry;
            this.timeToFade = 2500L + System.currentTimeMillis();
            this.client.hasFocus = true;
            ((ExScreen) this).setDisabledInputGrabbing(true);
            this.client.mouseHelper.grabCursor();
        }
    }

    @Override
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
                    this.client.openScreen(null);
                    return;
                }
            }
        }

        this.fill((this.width - 215) / 2, this.height - 59 - 4 - 48, (this.width + 215) / 2, this.height - 48, var5);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        if (this.songPlayed != null) {
            this.drawTextWithShadowCentred(this.textRenderer, this.songPlayed.songName, this.width / 2, this.height - 59 - 48, 14737632 + var6);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
