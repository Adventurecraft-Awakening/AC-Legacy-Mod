package dev.adventurecraft.awakening.mixin.client.gui.screen.container;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.adventurecraft.awakening.extension.container.ExPlayerContainer;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class MixinPlayerInventoryScreen extends AbstractContainerScreen {

    public MixinPlayerInventoryScreen(AbstractContainerMenu arg) {
        super(arg);
    }

    @WrapWithCondition(
        method = "renderForeground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/TextRenderer;drawText(Ljava/lang/String;III)V"))
    private boolean renderCraftingLabel(Font renderer, String string, int i, int j, int k) {
        if (menu instanceof ExPlayerContainer exContainer) {
            return exContainer.getAllowsCrafting();
        }
        return true;
    }

    @Inject(
        method = "renderContainerBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/container/PlayerInventoryScreen;blit(IIIIII)V",
            shift = At.Shift.AFTER))
    private void renderHeartPieces(float var1, CallbackInfo ci) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int texId = this.minecraft.textures.loadTexture("/gui/heartPiece.png");
        this.minecraft.textures.bind(texId);
        int heartPieces = ((ExPlayerEntity) this.minecraft.player).getHeartPiecesCount();
        this.blit(x + 89, y + 6, heartPieces * 32, 0, 32, 32);
    }
}
