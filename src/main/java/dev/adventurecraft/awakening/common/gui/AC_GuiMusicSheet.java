package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.AC_MusicScriptEntry;
import dev.adventurecraft.awakening.common.MusicPlayer;
import dev.adventurecraft.awakening.common.instruments.IInstrumentConfig;
import dev.adventurecraft.awakening.common.instruments.Note;
import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class AC_GuiMusicSheet extends Screen {

    private final IInstrumentConfig instrument;
    private final IntArrayList notesPlayed;
    private String notesPlayedString;
    private int spaceTaken;
    private AC_MusicScriptEntry songPlayed;
    private long timeToFade;

    public AC_GuiMusicSheet(IInstrumentConfig instrument) {
        this.instrument = instrument;
        this.notesPlayed = new IntArrayList();
        this.notesPlayedString = "";
        this.songPlayed = null;
    }

    public static final Note[] keyboardNotes = {
        new Note('D', -1), // Keyboard 1
        new Note('E', -1),
        new Note('F', -1),
        new Note('G', -1),
        new Note('A', 0),
        new Note('B', 0),
        new Note('C', 0),
        new Note('D', 0),
        new Note('E', 0),
        new Note('F', 0), // Keyboard 0
    };

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

        Note noteToPlay = keyboardNotes[key - Keyboard.KEY_1];
        this.notesPlayedString += Keyboard.getKeyName(key);


        if (noteToPlay != null) {
            int totalShiftValue = this.instrument.getTuning();
            if (noteIsSharp)
                totalShiftValue += 1;
            noteToPlay = noteToPlay.withShiftedValue(totalShiftValue);

            MusicPlayer.playNoteFromEntity(this.minecraft.player, this.instrument, noteToPlay, 1F);
        }

        AC_MusicScriptEntry entry = ((ExWorld) this.minecraft.level).getMusicScripts().executeMusic(this.notesPlayedString);
        if (entry != null) {
            this.songPlayed = entry;
            this.timeToFade = 2500L + System.currentTimeMillis();
            this.minecraft.mouseGrabbed = true;
            ((ExScreen) this).setDisabledInputGrabbing(true);
            this.minecraft.mouseHandler.grab();
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
                    this.minecraft.setScreen(null);
                    return;
                }
            }
        }

        this.fill((this.width - 215) / 2, this.height - 59 - 4 - 48, (this.width + 215) / 2, this.height - 48, backColor);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        if (this.songPlayed != null) {
            this.drawCenteredString(this.font, this.songPlayed.songName, this.width / 2, this.height - 59 - 48, 14737632 + foreColor);
        }

        int backTexId = this.minecraft.textures.loadTexture("/gui/musicSheet.png");
        GL11.glColor4f(0.9F, 0.1F, 0.1F, alpha);
        this.minecraft.textures.bind(backTexId);
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

    public static void showUI(IInstrumentConfig instrumentConfig) {
        Minecraft.instance.setScreen(new AC_GuiMusicSheet(instrumentConfig));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
