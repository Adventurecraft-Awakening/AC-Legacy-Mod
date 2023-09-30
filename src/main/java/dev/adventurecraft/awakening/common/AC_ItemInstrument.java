package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.block.ExSignBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.block.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemInstrument extends Item {

    String instrument;

    protected AC_ItemInstrument(int var1, String var2) {
        super(var1);
        this.instrument = var2;
    }

    @Override
    public boolean useOnBlock(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        if (world.getBlockId(x, y, z) == Block.STANDING_SIGN.id) {
            var targetSign = (SignBlockEntity) world.getBlockEntity(x, y, z);
            ((ExSignBlockEntity) targetSign).playSong(this.instrument);
        }

        return false;
    }

    @Override
    public ItemStack use(ItemStack stack, World world, PlayerEntity player) {
        AC_GuiMusicSheet.showUI(this.instrument);
        return stack;
    }
}
