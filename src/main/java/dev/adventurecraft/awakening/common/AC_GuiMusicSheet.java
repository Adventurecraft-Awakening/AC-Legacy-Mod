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

    public AC_GuiMusicSheet(String instrument) {
        this.instrument = instrument;
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
        boolean keyCorrespondsToANote = key < 2 || key > 11; // Numeric top row on the keyboard
        if (this.songPlayed != null || keyCorrespondsToANote) {
            return;
        }

        boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean canBeSharp = (key == Keyboard.KEY_1 || key == Keyboard.KEY_3 || key == Keyboard.KEY_4 || key == Keyboard.KEY_5 || key == Keyboard.KEY_7 || key == Keyboard.KEY_8 || key == Keyboard.KEY_0);
        boolean noteIsSharp = shiftDown && canBeSharp;

        int NOTE_SIZE = 11;
        int MAX_NOTE_SPACE = 168;

        if (noteIsSharp) {
            int NOTE_MODIFIER_SIZE = 14;

            int totalSpaceTaken = this.spaceTaken + NOTE_MODIFIER_SIZE + NOTE_SIZE;

            if (totalSpaceTaken >= MAX_NOTE_SPACE) {
                this.notesPlayed.clear();
                this.notesPlayedString = "";
                this.spaceTaken = 0;
            }

            // Adding the key as negative adds a '#' instead of a note icon
            this.notesPlayed.add(-key);
            this.spaceTaken += NOTE_MODIFIER_SIZE;
        }

        int totalSpaceTaken = this.spaceTaken + NOTE_SIZE;


        if (totalSpaceTaken >= MAX_NOTE_SPACE) {
            this.notesPlayed.clear();
            this.notesPlayedString = "";
            this.spaceTaken = 0;
        }

        this.spaceTaken += NOTE_SIZE;
        this.notesPlayed.add(key);

        if (key == Keyboard.KEY_1) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'D', shiftDown, 0.5F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "1";
        } else if (key == Keyboard.KEY_2) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'E', false, 0.5F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "2";
        } else if (key == Keyboard.KEY_3) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'F', shiftDown, 0.5F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "3";
        } else if (key == Keyboard.KEY_4) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'G', shiftDown, 0.5F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "4";
        } else if (key == Keyboard.KEY_5) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'A', shiftDown, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "5";
        } else if (key == Keyboard.KEY_6) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'B', false, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "6";
        } else if (key == Keyboard.KEY_7) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'C', shiftDown, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "7";
        } else if (key == Keyboard.KEY_8) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'D', shiftDown, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "8";
        } else if (key == Keyboard.KEY_9) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'E', false, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "9";
        } else if (key == Keyboard.KEY_0) {
            MusicPlayer.playNoteFromEntity(this.client.world, this.client.player, this.instrument, 'F', shiftDown, 1.0F, 1.0F);
            this.notesPlayedString = this.notesPlayedString + "0";
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
        float alpha = 1.0F;
        int backColor = Integer.MIN_VALUE;
        int foreColor = -16777216;
        if (this.songPlayed != null) {
            float fade = (float) (this.timeToFade - System.currentTimeMillis()) / 1000.0F;
            if (fade < 1.0F) {
                alpha = fade;
                backColor = (int) (128.0F * fade) << 24;
                foreColor = (int) (255.0F * fade) << 24;
                if ((double) fade < 0.004D) {
                    this.client.openScreen(null);
                    return;
                }
            }
        }

        this.fill((this.width - 215) / 2, this.height - 59 - 4 - 48, (this.width + 215) / 2, this.height - 48, backColor);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        if (this.songPlayed != null) {
            this.drawTextWithShadowCentred(this.textRenderer, this.songPlayed.songName, this.width / 2, this.height - 59 - 48, 14737632 + foreColor);
        }

        int backTexId = this.client.textureManager.getTextureId("/gui/musicSheet.png");
        GL11.glColor4f(0.9F, 0.1F, 0.1F, alpha);
        this.client.textureManager.bindTexture(backTexId);
        this.blit((this.width - 205) / 2, this.height - 59 - 2 - 48, 0, 0, 205, 59);
        int noteX = 0;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

        for (int note : this.notesPlayed) {
            if (note > 0) {
                this.drawNote(noteX, note);
                noteX += 11;
            } else {
                this.drawSharp(noteX, -note);
                noteX += 14;
            }
        }

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        super.render(var1, var2, var3);
    }

    private void drawNote(int x, int note) {
        this.blit((this.width - 205) / 2 + 36 + x, this.height - 59 - 2 - 48 + 46 - (note - 2) * 4, 0, 64, 9, 7);
    }

    private void drawSharp(int x, int note) {
        this.blit((this.width - 205) / 2 + 36 + x, this.height - 59 - 2 - 48 + 46 - (note - 2) * 4 - 5, 16, 64, 12, 17);
    }

    public static void showUI(String var0) {
        Minecraft.instance.openScreen(new AC_GuiMusicSheet(var0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
