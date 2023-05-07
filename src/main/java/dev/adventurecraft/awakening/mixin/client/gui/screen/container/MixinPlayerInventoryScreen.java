package dev.adventurecraft.awakening.mixin.client.gui.screen.container;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.client.gui.screen.container.PlayerInventoryScreen;
import net.minecraft.container.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventoryScreen.class)
public abstract class MixinPlayerInventoryScreen extends ContainerScreen {

    public MixinPlayerInventoryScreen(Container arg) {
        super(arg);
    }

    @Overwrite
    protected void renderForeground() {
    }

    @Inject(
        method = "renderContainerBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/container/PlayerInventoryScreen;blit(IIIIII)V",
            shift = At.Shift.AFTER))
    private void renderHeartPieces(float var1, CallbackInfo ci) {
        int x = (this.width - this.containerWidth) / 2;
        int y = (this.height - this.containerHeight) / 2;
        int texId = this.client.textureManager.getTextureId("/gui/heartPiece.png");
        this.client.textureManager.bindTexture(texId);
        int heartPieces = ((ExPlayerEntity) this.client.player).getHeartPiecesCount();
        this.blit(x + 89, y + 6, heartPieces * 32, 0, 32, 32);
    }
}
