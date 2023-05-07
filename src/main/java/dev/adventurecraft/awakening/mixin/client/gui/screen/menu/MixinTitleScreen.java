package dev.adventurecraft.awakening.mixin.client.gui.screen.menu;

import dev.adventurecraft.awakening.common.AC_GuiMapSelect;
import dev.adventurecraft.awakening.common.AC_Version;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SelectWorldScreen;
import net.minecraft.client.gui.screen.menu.MultiplayerScreen;
import net.minecraft.client.gui.screen.menu.OptionsScreen;
import net.minecraft.client.gui.screen.menu.TexturePacksScreen;
import net.minecraft.client.gui.screen.menu.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

    @Shadow
    private String splashMessage;

    @Shadow
    private ButtonWidget multiplayerButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        //ScriptModel.clearAll(); TODO
        //Minecraft.instance.soundHelper.stopMusic(); TODO
    }

    @Overwrite
    public void initVanillaScreen() {
        this.splashMessage = "A Minecraft Total Conversion!";
        TranslationStorage var2 = TranslationStorage.getInstance();
        int var3 = this.height / 4 + 48;
        this.buttons.add(new ButtonWidget(6, this.width / 2 - 100, var3, "New Save"));
        this.buttons.add(new ButtonWidget(1, this.width / 2 - 100, var3 + 22, "Load Save"));
        this.buttons.add(new ButtonWidget(7, this.width / 2 - 100, var3 + 44, "Craft a Map"));
        this.buttons.add(new ButtonWidget(5, this.width / 2 - 100, var3 + 66, "Download a Map"));
        if (this.client.isApplet) {
            this.buttons.add(new ButtonWidget(0, this.width / 2 - 100, var3 + 88, var2.translate("menu.options")));
        } else {
            this.buttons.add(new ButtonWidget(0, this.width / 2 - 100, var3 + 88 + 11, 98, 20, var2.translate("menu.options")));
            this.buttons.add(new ButtonWidget(4, this.width / 2 + 2, var3 + 88 + 11, 98, 20, var2.translate("menu.quit")));
        }

        if (this.client.session == null) {
            this.multiplayerButton.active = false;
        }
    }

    @Overwrite
    public void buttonClicked(ButtonWidget var1) {
        if (var1.id == 0) {
            this.client.openScreen(new OptionsScreen(this, this.client.options));
        } else if (var1.id == 1) {
            this.client.openScreen(new SelectWorldScreen(this));
        } else if (var1.id == 2) {
            this.client.openScreen(new MultiplayerScreen(this));
        } else if (var1.id == 3) {
            this.client.openScreen(new TexturePacksScreen(this));
        } else if (var1.id == 4) {
            this.client.scheduleStop();
        } else if (var1.id == 5) {
            //this.client.openScreen(new AC_GuiMapDownload(this)); TODO
        } else if (var1.id == 6) {
            this.client.openScreen(new AC_GuiMapSelect(this, ""));
        } else if (var1.id == 7) {
            this.client.openScreen(new AC_GuiMapSelect(this, null));
        }
    }

    @Overwrite
    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        Tessellator var4 = Tessellator.INSTANCE;
        short var5 = 320;
        int var6 = this.width / 2 - var5 / 2;
        byte var7 = 30;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/acLogo.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.blit(var6 + 0, var7 + 0, 0, 0, 256, 31);
        this.blit(var6 + 256, var7 + 0, 0, 128, 64, 31);
        var4.color(16777215);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2 + 90), 70.0F, 0.0F);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        float var8 = 1.8F - MathHelper.abs(MathHelper.sin((float) (System.currentTimeMillis() % 1000L) / 1000.0F * 3.141593F * 2.0F) * 0.1F);
        var8 = var8 * 100.0F / (float) (this.textRenderer.getTextWidth(this.splashMessage) + 32);
        GL11.glScalef(var8, var8, var8);
        this.drawTextWithShadowCentred(this.textRenderer, this.splashMessage, 0, -8, 16776960);
        GL11.glPopMatrix();
        this.drawTextWithShadow(this.textRenderer, AC_Version.version, 2, 2, 5263440);
        String var9 = "Copyright Mojang AB. Do not distribute.";
        this.drawTextWithShadow(this.textRenderer, var9, this.width - this.textRenderer.getTextWidth(var9) - 2, this.height - 10, 16777215);
        super.render(var1, var2, var3);
    }
}
