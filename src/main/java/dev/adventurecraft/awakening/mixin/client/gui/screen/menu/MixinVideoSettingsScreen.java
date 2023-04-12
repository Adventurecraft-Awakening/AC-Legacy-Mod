package dev.adventurecraft.awakening.mixin.client.gui.screen.menu;

import dev.adventurecraft.awakening.client.gui.GuiAnimationSettingsOF;
import dev.adventurecraft.awakening.client.gui.GuiDetailSettingsOF;
import dev.adventurecraft.awakening.client.gui.GuiOtherSettingsOF;
import dev.adventurecraft.awakening.client.gui.GuiWorldSettingsOF;
import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.VideoSettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;
import net.minecraft.client.gui.widgets.SliderWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.util.ScreenScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class MixinVideoSettingsScreen extends Screen {

    @Shadow
    private Screen parent;
    @Shadow
    protected String title;
    @Shadow
    private GameOptions options;
    @Shadow
    private static Option[] OPTIONS;

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTime = 0L;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void init_rewriteOptions(CallbackInfo ci) {
        OPTIONS = new Option[]{Option.GRAPHICS_QUALITY, Option.RENDER_DISTANCE, OptionOF.AO_LEVEL, Option.LIMIT_FRAMERATE, Option.ANAGLYPH, Option.VIEW_BOBBING, Option.GUI_SCALE, Option.ADVANCED_OPENGL, OptionOF.FOG_FANCY, OptionOF.FOG_START, OptionOF.MIPMAP_LEVEL, OptionOF.MIPMAP_TYPE, OptionOF.BETTER_GRASS, OptionOF.BRIGHTNESS};
    }

    public void initVanillaScreen() {
        TranslationStorage var1 = TranslationStorage.getInstance();
        this.title = var1.translate("options.videoTitle");
        int var2 = 0;
        Option[] var3 = OPTIONS;
        int var4 = var3.length;

        int var5;
        for (var5 = 0; var5 < var4; ++var5) {
            Option var6 = var3[var5];
            int var7 = this.width / 2 - 155 + var2 % 2 * 160;
            int var8 = this.height / 6 + 21 * (var2 / 2) - 10;
            if (!var6.isSlider()) {
                this.buttons.add(new OptionButtonWidget(var6.getId(), var7, var8, var6, this.options.getTranslatedValue(var6)));
            } else {
                this.buttons.add(new SliderWidget(var6.getId(), var7, var8, var6, this.options.getTranslatedValue(var6), this.options.getFloatValue(var6)));
            }

            ++var2;
        }

        var5 = this.height / 6 + 21 * (var2 / 2) - 10;
        boolean var9 = false;
        int var10 = this.width / 2 - 155 + 0;
        this.buttons.add(new OptionButtonWidget(100, var10, var5, "Animations..."));
        var10 = this.width / 2 - 155 + 160;
        this.buttons.add(new OptionButtonWidget(101, var10, var5, "Details..."));
        var5 += 21;
        var10 = this.width / 2 - 155 + 0;
        this.buttons.add(new OptionButtonWidget(102, var10, var5, "World..."));
        var10 = this.width / 2 - 155 + 160;
        this.buttons.add(new OptionButtonWidget(103, var10, var5, "Other..."));
        this.buttons.add(new ButtonWidget(200, this.width / 2 - 100, this.height / 6 + 168 + 11, var1.translate("gui.done")));
    }

    public void buttonClicked(ButtonWidget var1) {
        if (var1.active) {
            if (var1.id < 100 && var1 instanceof OptionButtonWidget) {
                this.options.setIntOption(((OptionButtonWidget) var1).getOption(), 1);
                var1.text = this.options.getTranslatedValue(Option.getById(var1.id));
            }

            if (var1.id == 200) {
                this.client.options.saveOptions();
                this.client.openScreen(this.parent);
            }

            if (var1.id == 100) {
                this.client.options.saveOptions();
                GuiAnimationSettingsOF var2 = new GuiAnimationSettingsOF(this, this.options);
                this.client.openScreen(var2);
            }

            if (var1.id == 101) {
                this.client.options.saveOptions();
                GuiDetailSettingsOF var5 = new GuiDetailSettingsOF(this, this.options);
                this.client.openScreen(var5);
            }

            if (var1.id == 102) {
                this.client.options.saveOptions();
                GuiWorldSettingsOF var6 = new GuiWorldSettingsOF(this, this.options);
                this.client.openScreen(var6);
            }

            if (var1.id == 103) {
                this.client.options.saveOptions();
                GuiOtherSettingsOF var7 = new GuiOtherSettingsOF(this, this.options);
                this.client.openScreen(var7);
            }

            if (var1.id != OptionOF.BRIGHTNESS.ordinal() && var1.id != OptionOF.AO_LEVEL.ordinal()) {
                ScreenScaler var8 = new ScreenScaler(this.client.options, this.client.actualWidth, this.client.actualHeight);
                int var3 = var8.getScaledWidth();
                int var4 = var8.getScaledHeight();
                this.init(this.client, var3, var4);
            }
        }
    }

    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        this.drawTextWithShadowCentred(this.textRenderer, this.title, this.width / 2, 20, 16777215);
        super.render(var1, var2, var3);
        if (Math.abs(var1 - this.lastMouseX) <= 5 && Math.abs(var2 - this.lastMouseY) <= 5) {
            short var4 = 700;
            if (System.currentTimeMillis() >= this.mouseStillTime + (long) var4) {
                int var5 = this.width / 2 - 150;
                int var6 = this.height / 6 - 5;
                if (var2 <= var6 + 98) {
                    var6 += 105;
                }

                int var7 = var5 + 150 + 150;
                int var8 = var6 + 84 + 10;
                ButtonWidget var9 = this.getSelectedButton(var1, var2);
                if (var9 != null) {
                    String var10 = this.getButtonName(var9.text);
                    String[] var11 = this.getTooltipLines(var10);
                    if (var11 == null) {
                        return;
                    }

                    this.fillGradient(var5, var6, var7, var8, -536870912, -536870912);

                    for (int var12 = 0; var12 < var11.length; ++var12) {
                        String var13 = var11[var12];
                        this.textRenderer.drawTextWithShadow(var13, var5 + 5, var6 + 5 + var12 * 11, 14540253);
                    }
                }

            }
        } else {
            this.lastMouseX = var1;
            this.lastMouseY = var2;
            this.mouseStillTime = System.currentTimeMillis();
        }
    }

    private String[] getTooltipLines(String var1) {
        return var1.equals("Graphics") ? new String[]{"Visual quality", "  Fast  - lower quality, faster", "  Fancy - higher quality, slower", "Changes the appearance of clouds, leaves, water,", "shadows and grass sides."} : (var1.equals("Render Distance") ? new String[]{"Visible distance", "  Far - 256m (slower)", "  Normal - 128m", "  Short - 64m (faster)", "  Tiny - 32m (fastest)"} : (var1.equals("Smooth Lighting") ? new String[]{"Smooth lighting", "  OFF - no smooth lighting (faster)", "  1% - light smooth lighting (slower)", "  100% - dark smooth lighting (slower)"} : (var1.equals("Performance") ? new String[]{"FPS Limit", "  Max FPS - no limit (fastest)", "  Balanced - limit 120 FPS (slower)", "  Power saver - limit 40 FPS (slowest)", "  VSync - limit to monitor framerate (60, 30, 20)", "Balanced and Power saver decrease the FPS even if", "the limit value is not reached."} : (var1.equals("3D Anaglyph") ? new String[]{"3D mode used with red-cyan 3D glasses."} : (var1.equals("View Bobbing") ? new String[]{"More realistic movement.", "When using mipmaps set it to OFF for best results."} : (var1.equals("GUI Scale") ? new String[]{"GUI Scale", "Smaller GUI might be faster"} : (var1.equals("Advanced OpenGL") ? new String[]{"Detect and render only visible geometry", "  OFF - all geometry is rendered (slower)", "  Fast - ony visible geometry is rendered (fastest)", "  Fancy - conservative, avoids visual artifacts (faster)", "The option is available only if it is supported by the ", "graphic card."} : (var1.equals("Fog") ? new String[]{"Fog type", "  Fast - faster fog", "  Fancy - slower fog, looks better", "The fancy fog is available only if it is supported by the ", "graphic card."} : (var1.equals("Fog Start") ? new String[]{"Fog start", "  0.2 - the fog starts near the player", "  0.8 - the fog starts far from the player", "This option usually does not affect the performance."} : (var1.equals("Mipmap Level") ? new String[]{"Visual effect which makes distant objects look better", "by smoothing the texture details", "  OFF - no smoothing", "  1 - minimum smoothing", "  4 - maximum smoothing", "This option usually does not affect the performance."} : (var1.equals("Mipmap Type") ? new String[]{"Visual effect which makes distant objects look better", "by smoothing the texture details", "  Nearest - rough smoothing", "  Linear - fine smoothing", "This option usually does not affect the performance."} : (var1.equals("Better Grass") ? new String[]{"Better Grass", "  OFF - default side grass texture, fastest", "  Fast - full side grass texture, slower", "  Fancy - dynamic side grass texture, slowest"} : (var1.equals("Brightness") ? new String[]{"Increases the brightness of darker objects", "  OFF - standard brightness", "  100% - maximum brightness for darker objects", "This options does not change the brightness of ", "fully black objects"} : null)))))))))))));
    }

    private String getButtonName(String var1) {
        int var2 = var1.indexOf(58);
        return var2 < 0 ? var1 : var1.substring(0, var2);
    }

    private ButtonWidget getSelectedButton(int var1, int var2) {
        for (int var3 = 0; var3 < this.buttons.size(); ++var3) {
            ButtonWidget var4 = (ButtonWidget) this.buttons.get(var3);
            boolean var5 = var1 >= var4.x && var2 >= var4.y && var1 < var4.x + var4.width && var2 < var4.y + var4.height;
            if (var5) {
                return var4;
            }
        }

        return null;
    }
}

