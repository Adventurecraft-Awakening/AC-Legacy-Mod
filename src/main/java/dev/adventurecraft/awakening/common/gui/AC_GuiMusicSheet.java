package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.UiDrawContext;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.CompareOp;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.Rect;
import dev.adventurecraft.awakening.layout.IntCorner;
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

public class AC_GuiMusicSheet extends Screen {

    private final IInstrumentConfig instrument;
    private final IntArrayList notesPlayed;
    private StringBuilder notesPlayedString;
    private int spaceTaken;
    private AC_MusicScriptEntry songPlayed;
    private long timeToFade;

    public AC_GuiMusicSheet(IInstrumentConfig instrument) {
        this.instrument = instrument;
        this.notesPlayed = new IntArrayList();
        this.notesPlayedString = new StringBuilder();
        this.songPlayed = null;
        this.passEvents = true;
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

        final int NOTE_SIZE = 11;
        final int MAX_NOTE_SPACE = 168;

        if (noteIsSharp) {
            int NOTE_MODIFIER_SIZE = 14;

            int totalSpaceTaken = this.spaceTaken + NOTE_MODIFIER_SIZE + NOTE_SIZE;

            if (totalSpaceTaken >= MAX_NOTE_SPACE) {
                this.notesPlayed.clear();
                this.notesPlayedString.setLength(0);
                this.spaceTaken = 0;
            }

            // Adding the key as negative adds a '#' instead of a note icon
            this.notesPlayed.add(-key);
            this.spaceTaken += NOTE_MODIFIER_SIZE;
        }

        int totalSpaceTaken = this.spaceTaken + NOTE_SIZE;

        if (totalSpaceTaken >= MAX_NOTE_SPACE) {
            this.notesPlayed.clear();
            this.notesPlayedString.setLength(0);
            this.spaceTaken = 0;
        }

        this.spaceTaken += NOTE_SIZE;
        this.notesPlayed.add(key);

        Note noteToPlay = keyboardNotes[key - Keyboard.KEY_1];
        this.notesPlayedString.append(Keyboard.getKeyName(key));


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
                if (fade < 0.004f) {
                    this.minecraft.setScreen(null);
                    return;
                }
            }
        }

        try (var root = LegacyPassBridge.uiContext(this.font, this.minecraft.textures)) {
            var ui = root.withParameters(root.parameters().withMaterial(root.parameters().material().withAlpha(CompareOp.ALWAYS, 0)));
            int sheetX = (this.width - 205) / 2;
            int sheetY = this.height - 59 - 2 - 48;
            ui.fill(new Rect((this.width - 215) / 2, this.height - 59 - 4 - 48, (this.width + 215) / 2 - (this.width - 215) / 2, 63), Rgba.fromBgra(backColor));
            if (this.songPlayed != null) {
                String song = this.songPlayed.songName;
                ui.text(song, (this.width - this.font.width(song)) / 2f, this.height - 59 - 48,
                    Rgba.fromBgra(14737632 + foreColor), true);
            }
            RenderTexture texture = ui.texture("/gui/musicSheet.png");
            int fadeAlpha = Rgba.saturateToByte(alpha);
            ui.image(texture, new Rect(sheetX, sheetY, 205, 59), new Rect(0, 0, 205 / 256f, 59 / 256f),
                new IntCorner(Rgba.fromRgba8(Rgba.saturateToByte(.9F), Rgba.saturateToByte(.1F), Rgba.saturateToByte(.1F), fadeAlpha)));
            int noteX = 0;
            int noteColor = Rgba.withAlpha(Rgba.WHITE, fadeAlpha);
            for (int note : this.notesPlayed) {
                if (note > 0) { this.drawNote(ui, texture, noteX, note, noteColor); noteX += 11; }
                else { this.drawSharp(ui, texture, noteX, -note, noteColor); noteX += 14; }
            }
        }
        super.render(var1, var2, var3);
    }

    private void drawNote(UiDrawContext ui, RenderTexture texture, int x, int note, int color) {
        ui.image(texture, new Rect((this.width - 205) / 2 + 36 + x,
                this.height - 59 - 2 - 48 + 46 - (note - 2) * 4, 9, 7),
            new Rect(0, 64 / 256f, 9 / 256f, 7 / 256f), new IntCorner(color));
    }

    private void drawSharp(UiDrawContext ui, RenderTexture texture, int x, int note, int color) {
        ui.image(texture, new Rect((this.width - 205) / 2 + 36 + x,
                this.height - 59 - 2 - 48 + 46 - (note - 2) * 4 - 5, 12, 17),
            new Rect(16 / 256f, 64 / 256f, 12 / 256f, 17 / 256f), new IntCorner(color));
    }

    public static void showUI(IInstrumentConfig instrumentConfig) {
        Minecraft.instance.setScreen(new AC_GuiMusicSheet(instrumentConfig));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
