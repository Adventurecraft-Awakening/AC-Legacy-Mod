package dev.adventurecraft.awakening.mixin.client.gui.screen.menu;

import dev.adventurecraft.awakening.common.gui.AC_GuiMapSelect;
import dev.adventurecraft.awakening.common.AC_Version;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.script.ScriptModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SelectWorldScreen;
import net.minecraft.client.gui.screens.TexturePackSelectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.locale.I18n;
import net.minecraft.util.Mth;
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
    private String splash;

    @Shadow
    private Button multiplayerButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        ScriptModel.clearAll();
        ((ExSoundHelper) Minecraft.instance.soundEngine).stopMusic();
    }

    @Overwrite
    public void init() {
        this.splash = "A Minecraft Total Conversion!";
        I18n ts = I18n.getInstance();
        int y = this.height / 4 + 48;
        this.buttons.add(new Button(6, this.width / 2 - 100, y, "New Save"));
        this.buttons.add(new Button(1, this.width / 2 - 100, y + 22, "Load Save"));
        this.buttons.add(new Button(7, this.width / 2 - 100, y + 44, "Craft a Map"));
        this.buttons.add(this.multiplayerButton = new Button(2, this.width / 2 - 100, y + 66, ts.get("menu.multiplayer")));

        if (this.minecraft.appletMode) {
            this.buttons.add(new Button(0, this.width / 2 - 100, y + 88, ts.get("menu.options")));
        } else {
            this.buttons.add(new Button(0, this.width / 2 - 100, y + 88 + 11, 98, 20, ts.get("menu.options")));
            this.buttons.add(new Button(4, this.width / 2 + 2, y + 88 + 11, 98, 20, ts.get("menu.quit")));
        }

        if (this.minecraft.user == null) {
            this.multiplayerButton.active = false;
        }
    }

    @Overwrite
    public void buttonClicked(Button button) {
        if (button.id == 0) {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        } else if (button.id == 1) {
            this.minecraft.setScreen(new SelectWorldScreen(this));
        } else if (button.id == 2) {
            this.minecraft.setScreen(new JoinMultiplayerScreen(this));
        } else if (button.id == 3) {
            this.minecraft.setScreen(new TexturePackSelectScreen(this));
        } else if (button.id == 4) {
            this.minecraft.stop();
        } else if (button.id == 6) {
            this.minecraft.setScreen(new AC_GuiMapSelect(this, ""));
        } else if (button.id == 7) {
            this.minecraft.setScreen(new AC_GuiMapSelect(this, null));
        }
    }

    @Overwrite
    public void render(int var1, int var2, float var3) {
        this.renderBackground();

        int x = this.width / 2 - 320 / 2;
        int y = 30;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textures.loadTexture("/acLogo.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.blit(x, y , 0, 0, 256, 31);
        this.blit(x + 256, y, 0, 128, 64, 31);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2 + 90), 70.0F, 0.0F);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        float scale = 1.8F - Mth.abs(Mth.sin((float) (System.currentTimeMillis() % 1000L) / 1000.0F * 3.141593F * 2.0F) * 0.1F);
        scale = scale * 100.0F / (float) (this.font.width(this.splash) + 32);
        GL11.glScalef(scale, scale, scale);
        this.drawCenteredString(this.font, this.splash, 0, -8, 16776960);
        GL11.glPopMatrix();

        this.drawString(this.font, AC_Version.version, 2, 2, 5263440);

        String copyrightText = "Copyright Mojang AB. Do not distribute.";
        this.drawString(this.font, copyrightText, this.width - this.font.width(copyrightText) - 2, this.height - 10, 16777215);

        super.render(var1, var2, var3);
    }
}
