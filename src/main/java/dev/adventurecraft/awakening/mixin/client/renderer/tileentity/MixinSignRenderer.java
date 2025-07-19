package dev.adventurecraft.awakening.mixin.client.renderer.tileentity;

import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.Rgba;
import net.minecraft.client.model.SignModel;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.tileentity.SignRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import org.lwjgl.opengl.GL11;
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

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5f, (float) y + 0.75f * scale, (float) z + 0.5f);
        if (tile == Tile.SIGN) {
            float angle = (float) (signTileEntity.getData() * 360) / 16.0f;
            GL11.glRotatef(-angle, 0.0f, 1.0f, 0.0f);
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
            GL11.glRotatef(-angle, 0.0f, 1.0f, 0.0f);
            GL11.glTranslatef(0.0f, -0.3125f, -0.4375f);
            this.signModel.stick.visible = false;
        }
        this.bindTexture("/item/sign.png");
        GL11.glPushMatrix();
        GL11.glScalef(scale, -scale, -scale);
        this.signModel.render();
        GL11.glPopMatrix();

        String[] messages = signTileEntity.messages;
        if (!hasMessage(signTileEntity)) {
            GL11.glPopMatrix();
            return;
        }

        float textScale = 0.016666668f * scale;
        GL11.glTranslatef(0.0f, 0.5f * scale, 0.07f * scale);
        GL11.glScalef(textScale, -textScale, textScale);
        GL11.glNormal3f(0.0f, 0.0f, -textScale);
        GL11.glDepthMask(false);

        var state = ((ExTextRenderer) this.getFont()).createState();
        state.setColor(Rgba.BLACK);
        state.begin(Tesselator.instance);
        for (int i = 0; i < messages.length; ++i) {
            String string = messages[i];
            if (i == signTileEntity.selectedLine) {
                string = "> " + string + " <";
            }
            int tx = -state.measureText(string).width() / 2;
            state.drawText(Tesselator.instance, string, tx, i * 10 - messages.length * 5);
        }
        state.end(Tesselator.instance);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
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
