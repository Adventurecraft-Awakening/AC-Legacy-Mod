package dev.adventurecraft.awakening.mixin.entity.block;

import dev.adventurecraft.awakening.common.MusicPlayer;
import dev.adventurecraft.awakening.extension.entity.block.ExSongContainer;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.block.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SignBlockEntity.class)
public abstract class MixinSignBlockEntity extends BlockEntity implements ExSongContainer {

    @Shadow
    public String[] text;
    public boolean playSong;
    public String instrument;
    public int onNote;
    public int tickSinceStart;

    @Override
    public void tick() {
        if (!this.playSong) {
            return;
        }

        if (this.tickSinceStart % 10 == 0) {
            String signContents = GetSong();
            if (this.onNote < MusicPlayer.countNotes(signContents)) {
                MusicPlayer.playNoteFromSong(this.world, this.x, this.y, this.z, this.instrument, signContents, this.onNote, 1.0F);
                ++this.onNote;
            } else {
                this.playSong = false;
            }
        }

        ++this.tickSinceStart;
    }

    @Override
    public void PlaySong(String instrumentUri) {
        this.playSong = true;
        this.instrument = instrumentUri;
        this.tickSinceStart = 0;
        this.onNote = 0;
    }

    public String GetSong() {
        return this.text[0] + this.text[1] + this.text[2] + this.text[3];
    }
}
