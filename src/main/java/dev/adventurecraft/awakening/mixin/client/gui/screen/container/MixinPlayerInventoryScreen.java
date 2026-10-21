package dev.adventurecraft.awakening.mixin.client.gui.screen.container;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.extension.container.ExPlayerContainer;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
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
        method = "renderLabels",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;draw(Ljava/lang/String;III)V"))
    private boolean renderCraftingLabel(Font renderer, String string, int i, int j, int k) {
        if (menu instanceof ExPlayerContainer exContainer) {
            return exContainer.getAllowsCrafting();
        }
        return true;
    }

    @WrapOperation(
        method = "render(F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFF)V"))
    private void renderPlayerPreview(EntityRenderDispatcher dispatcher, Entity entity, double x, double y, double z,
                                     float yaw, float partialTick, Operation<Void> original) {
        var captured = LegacyPassBridge.capture();
        // The surrounding UI does not write depth. The model needs its own
        // depth state so cube faces and limbs occlude one another in 3D.
        var parameters = dev.adventurecraft.awakening.client.render.state.MaterialRecipe.MODEL_PREVIEW.apply(captured);
        LegacyPassBridge.drawLegacy(parameters, LegacyPassBridge.currentTexture(),
            () -> original.call(dispatcher, entity, x, y, z, yaw, partialTick));
    }

    @Inject(
        method = "render(F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;blit(IIIIII)V",
            shift = At.Shift.AFTER))
    private void renderHeartPieces(float var1, CallbackInfo ci) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int heartPieces = ((ExPlayerEntity) this.minecraft.player).getHeartPiecesCount();
        try (var ui = dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge.uiContext(this.font, this.minecraft.textures)) {
            ui.image(ui.texture("/gui/heartPiece.png"), new dev.adventurecraft.awakening.layout.Rect(x + 89, y + 6, 32, 32),
                new dev.adventurecraft.awakening.layout.Rect(heartPieces * 32 / 256f, 0, 32 / 256f, 32 / 256f),
                new dev.adventurecraft.awakening.layout.IntCorner(-1));
        }
    }
}
