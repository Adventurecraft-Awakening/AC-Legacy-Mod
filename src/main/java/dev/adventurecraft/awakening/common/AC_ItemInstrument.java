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
    public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
        if (var3.getBlockId(var4, var5, var6) == Block.STANDING_SIGN.id) {
            var var8 = (SignBlockEntity) var3.getBlockEntity(var4, var5, var6);
            ((ExSignBlockEntity) var8).playSong(this.instrument);
        }

        return false;
    }

    @Override
    public ItemStack use(ItemStack var1, World var2, PlayerEntity var3) {
        AC_GuiMusicSheet.showUI(this.instrument);
        return var1;
    }
}
