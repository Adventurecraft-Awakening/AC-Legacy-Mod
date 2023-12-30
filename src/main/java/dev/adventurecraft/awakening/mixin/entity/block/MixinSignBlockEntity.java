package dev.adventurecraft.awakening.mixin.entity.block;

import dev.adventurecraft.awakening.common.MusicPlayer;
import dev.adventurecraft.awakening.common.instruments.IInstrumentConfig;
import dev.adventurecraft.awakening.common.instruments.Note;
import dev.adventurecraft.awakening.common.instruments.Song;
import dev.adventurecraft.awakening.extension.entity.block.ExSongContainer;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.block.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;

@Mixin(SignBlockEntity.class)
public abstract class MixinSignBlockEntity extends BlockEntity implements ExSongContainer {

    @Shadow
    public String[] text;
    public boolean playSong;
    public IInstrumentConfig instrument;
    public int tickSinceStart;

    public Iterator<Note> songIterator;

    @Override
    public void tick() {
        if (!this.playSong) {
            return;
        }

        if (this.tickSinceStart % 10 == 0) {
            if (!songIterator.hasNext()) this.playSong = false;
            else {
                Note currentNote = songIterator.next();
                if (currentNote != null)
                    MusicPlayer.playNote(this.world, this.x, this.y, this.z, this.instrument, currentNote, 1.0F);
            }
        }

        ++this.tickSinceStart;
    }

    @Override
    public void playSong(IInstrumentConfig instrumentConfig) {
        this.playSong = true;
        this.instrument = instrumentConfig;
        this.tickSinceStart = 0;

        Song songToPlay = new Song(getSong(), instrumentConfig.getTuning());
        this.songIterator = songToPlay.iterator();
    }

    public String getSong() {
        return this.text[0] + this.text[1] + this.text[2] + this.text[3];
    }
}
