package dev.adventurecraft.awakening.mixin.client.renderer.tileentity;

import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.Rgba;
import net.minecraft.client.model.SignModel;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.tileentity.SignRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SignRenderer.class)
public abstract class MixinSignRenderer extends TileEntityRenderer {

    @Shadow private SignModel signModel;

    @Overwrite
    public void render(SignTileEntity signTileEntity, double x, double y, double z, float partialTick) {
        Tile tile = signTileEntity.getTile();
        float scale = 0.6666667f;

        LegacyGlAdapter.pushMatrix();
        LegacyGlAdapter.translatef((float) x + 0.5f, (float) y + 0.75f * scale, (float) z + 0.5f);
        if (tile == Tile.SIGN) {
            float angle = (float) (signTileEntity.getData() * 360) / 16.0f;
            LegacyGlAdapter.rotatef(-angle, 0.0f, 1.0f, 0.0f);
            this.signModel.stick.visible = true;
        }
        else {
            int data = signTileEntity.getData();
            float angle = switch (data) {
                case 2 -> 180.0f;
                case 4 -> 90.0f;
                case 5 -> -90.0f;
                default -> 0.0f;
            };
            LegacyGlAdapter.rotatef(-angle, 0.0f, 1.0f, 0.0f);
            LegacyGlAdapter.translatef(0.0f, -0.3125f, -0.4375f);
            this.signModel.stick.visible = false;
        }
        this.bindTexture("/item/sign.png");
        LegacyGlAdapter.pushMatrix();
        LegacyGlAdapter.scalef(scale, -scale, -scale);
        this.signModel.render();
        LegacyGlAdapter.popMatrix();

        String[] messages = signTileEntity.messages;
        if (!hasMessage(signTileEntity)) {
            LegacyGlAdapter.popMatrix();
            return;
        }

        float textScale = 0.016666668f * scale;
        LegacyGlAdapter.translatef(0.0f, 0.5f * scale, 0.07f * scale);
        LegacyGlAdapter.scalef(textScale, -textScale, textScale);
        LegacyGlAdapter.normal3f(0.0f, 0.0f, -textScale);
        LegacyGlAdapter.depthMask(false);

        var state = ((ExTextRenderer) this.getFont()).createState();
        state.setColor(Rgba.BLACK);
        state.begin(Tesselator.instance);
        for (int i = 0; i < messages.length; ++i) {
            String string = messages[i];
            if (i == signTileEntity.selectedLine) {
                string = "> " + string + " <";
            }
            int tx = -state.measureText(string).width() / 2;
            state.drawText(string, tx, i * 10 - messages.length * 5);
        }
        state.end();
        LegacyGlAdapter.depthMask(true);
        LegacyGlAdapter.popMatrix();
    }

    @Unique
    private static boolean hasMessage(SignTileEntity sign) {
        if (sign.selectedLine != -1) {
            return true;
        }
        for (String message : sign.messages) {
            if (!message.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
