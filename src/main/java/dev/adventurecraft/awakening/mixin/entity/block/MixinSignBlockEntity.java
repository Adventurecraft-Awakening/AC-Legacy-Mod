package dev.adventurecraft.awakening.mixin.entity.block;

import dev.adventurecraft.awakening.common.MusicPlayer;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.block.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SignBlockEntity.class)
public abstract class MixinSignBlockEntity extends BlockEntity {

    @Shadow
    public String[] text;
    public boolean playSong;
    public String instrument;
    public int onNote;
    public int tickSinceStart;

    public void tick() {
        if (this.playSong) {
            if (this.tickSinceStart % 10 == 0) {
                String var1 = this.text[0] + this.text[1] + this.text[2] + this.text[3];
                if (this.onNote < MusicPlayer.countNotes(var1)) {
                    MusicPlayer.playNoteFromSong(this.world, this.x, this.y, this.z, this.instrument, var1, this.onNote, 1.0F);
                    ++this.onNote;
                } else {
                    this.playSong = false;
                }
            }

            ++this.tickSinceStart;
        }

    }

    public void playSong(String var1) {
        this.playSong = true;
        this.instrument = var1;
        this.tickSinceStart = 0;
        this.onNote = 0;
    }
}
