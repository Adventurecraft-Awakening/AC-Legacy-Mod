package dev.adventurecraft.awakening.mixin.entity.block;

import dev.adventurecraft.awakening.common.MusicPlayer;
import dev.adventurecraft.awakening.common.instruments.IInstrumentConfig;
import dev.adventurecraft.awakening.common.instruments.Note;
import dev.adventurecraft.awakening.common.instruments.Song;
import dev.adventurecraft.awakening.extension.entity.block.ExSongContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;

@Mixin(SignTileEntity.class)
public abstract class MixinSignBlockEntity extends TileEntity implements ExSongContainer {

    @Shadow
    public String[] messages;
    
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
                    MusicPlayer.playNote(this.level, this.x, this.y, this.z, this.instrument, currentNote, 1.0F);
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
        return this.messages[0] + this.messages[1] + this.messages[2] + this.messages[3];
    }
}
