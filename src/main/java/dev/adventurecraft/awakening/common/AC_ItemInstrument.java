package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.common.instruments.InstrumentConfig;
import dev.adventurecraft.awakening.extension.entity.block.ExSongContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.block.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemInstrument extends Item {


    /**
     * The sound's URI.
     * To play, for example, resources/newsound/note/harp.ogg, the instrument would be <code>"note.harp"</code>.
     */
    InstrumentConfig instrument;

    /**
     * Creates a new instrument item.
     *
     * @param itemId        The ID of the item.
     * @param instrumentUri The instrument's sound URI. Default tuning is +3
     */
    protected AC_ItemInstrument(int itemId, String instrumentUri) {
        super(itemId);
        this.instrument = new InstrumentConfig(instrumentUri);
    }

    /**
     * Creates a new instrument item.
     *
     * @param itemId        The ID of the item.
     * @param instrumentUri The instrument's sound URI.
     * @param noteOffset    The offset of the instrument to be tuned so its first note is A
     */
    protected AC_ItemInstrument(int itemId, String instrumentUri, int noteOffset) {
        super(itemId);
        this.instrument = new InstrumentConfig(instrumentUri, noteOffset);
    }

    @Override
    public boolean useOnBlock(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        if (world.getBlockId(x, y, z) == Block.STANDING_SIGN.id) {
            var targetSign = (SignBlockEntity) world.getBlockEntity(x, y, z);
            ((ExSongContainer) targetSign).playSong(this.instrument);
        }

        return false;
    }

    @Override
    public ItemStack use(ItemStack stack, World world, PlayerEntity player) {
        AC_GuiMusicSheet.showUI(this.instrument);
        return stack;
    }
}
