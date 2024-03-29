package dev.adventurecraft.awakening.mixin.client.gui.screen.menu;

import dev.adventurecraft.awakening.common.AC_GuiMapSelect;
import dev.adventurecraft.awakening.common.AC_Version;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.script.ScriptModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SelectWorldScreen;
import net.minecraft.client.gui.screen.menu.MultiplayerScreen;
import net.minecraft.client.gui.screen.menu.OptionsScreen;
import net.minecraft.client.gui.screen.menu.TexturePacksScreen;
import net.minecraft.client.gui.screen.menu.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
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
        ScriptModel.clearAll();
        ((ExSoundHelper) Minecraft.instance.soundHelper).stopMusic();
    }

    @Overwrite
    public void initVanillaScreen() {
        this.splashMessage = "A Minecraft Total Conversion!";
        TranslationStorage ts = TranslationStorage.getInstance();
        int y = this.height / 4 + 48;
        this.buttons.add(new ButtonWidget(6, this.width / 2 - 100, y, "New Save"));
        this.buttons.add(new ButtonWidget(1, this.width / 2 - 100, y + 22, "Load Save"));
        this.buttons.add(new ButtonWidget(7, this.width / 2 - 100, y + 44, "Craft a Map"));

        if (this.client.isApplet) {
            this.buttons.add(new ButtonWidget(0, this.width / 2 - 100, y + 88, ts.translate("menu.options")));
        } else {
            this.buttons.add(new ButtonWidget(0, this.width / 2 - 100, y + 88 + 11, 98, 20, ts.translate("menu.options")));
            this.buttons.add(new ButtonWidget(4, this.width / 2 + 2, y + 88 + 11, 98, 20, ts.translate("menu.quit")));
        }

        if (this.client.session == null) {
            this.multiplayerButton.active = false;
        }
    }

    @Overwrite
    public void buttonClicked(ButtonWidget button) {
        if (button.id == 0) {
            this.client.openScreen(new OptionsScreen(this, this.client.options));
        } else if (button.id == 1) {
            this.client.openScreen(new SelectWorldScreen(this));
        } else if (button.id == 2) {
            this.client.openScreen(new MultiplayerScreen(this));
        } else if (button.id == 3) {
            this.client.openScreen(new TexturePacksScreen(this));
        } else if (button.id == 4) {
            this.client.scheduleStop();
        } else if (button.id == 6) {
            this.client.openScreen(new AC_GuiMapSelect(this, ""));
        } else if (button.id == 7) {
            this.client.openScreen(new AC_GuiMapSelect(this, null));
        }
    }

    @Overwrite
    public void render(int var1, int var2, float var3) {
        this.renderBackground();

        int x = this.width / 2 - 320 / 2;
        int y = 30;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/acLogo.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.blit(x, y , 0, 0, 256, 31);
        this.blit(x + 256, y, 0, 128, 64, 31);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2 + 90), 70.0F, 0.0F);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        float scale = 1.8F - MathHelper.abs(MathHelper.sin((float) (System.currentTimeMillis() % 1000L) / 1000.0F * 3.141593F * 2.0F) * 0.1F);
        scale = scale * 100.0F / (float) (this.textRenderer.getTextWidth(this.splashMessage) + 32);
        GL11.glScalef(scale, scale, scale);
        this.drawTextWithShadowCentred(this.textRenderer, this.splashMessage, 0, -8, 16776960);
        GL11.glPopMatrix();

        this.drawTextWithShadow(this.textRenderer, AC_Version.version, 2, 2, 5263440);

        String copyrightText = "Copyright Mojang AB. Do not distribute.";
        this.drawTextWithShadow(this.textRenderer, copyrightText, this.width - this.textRenderer.getTextWidth(copyrightText) - 2, this.height - 10, 16777215);

        super.render(var1, var2, var3);
    }
}
