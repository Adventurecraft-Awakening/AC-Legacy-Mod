package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;
import net.minecraft.client.gui.widgets.SliderWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.util.ScreenScaler;

public class GuiDetailSettingsOF extends Screen {
	private Screen prevScreen;
	protected String title = "Detail Settings";
	private GameOptions settings;
	private static Option[] enumOptions = new Option[]{OptionOF.CLOUDS, OptionOF.CLOUD_HEIGHT, OptionOF.TREES, OptionOF.GRASS, OptionOF.WATER, OptionOF.RAIN, OptionOF.SKY, OptionOF.STARS, OptionOF.AF_LEVEL, OptionOF.AA_LEVEL, OptionOF.CLEAR_WATER};
	private int lastMouseX = 0;
	private int lastMouseY = 0;
	private long mouseStillTime = 0L;

	public GuiDetailSettingsOF(Screen var1, GameOptions var2) {
		this.prevScreen = var1;
		this.settings = var2;
	}

	public void initVanillaScreen() {
		TranslationStorage var1 = TranslationStorage.getInstance();
		int var2 = 0;
		Option[] var3 = enumOptions;
		int var4 = var3.length;

		for(int var5 = 0; var5 < var4; ++var5) {
			Option var6 = var3[var5];
			int var7 = this.width / 2 - 155 + var2 % 2 * 160;
			int var8 = this.height / 6 + 21 * (var2 / 2) - 10;
			if(!var6.isSlider()) {
				this.buttons.add(new OptionButtonWidget(var6.getId(), var7, var8, var6, this.settings.getTranslatedValue(var6)));
			} else {
				this.buttons.add(new SliderWidget(var6.getId(), var7, var8, var6, this.settings.getTranslatedValue(var6), this.settings.getFloatValue(var6)));
			}

			++var2;
		}

		this.buttons.add(new ButtonWidget(200, this.width / 2 - 100, this.height / 6 + 168 + 11, var1.translate("gui.done")));
	}

	protected void buttonClicked(ButtonWidget var1) {
		if(var1.active) {
			if(var1.id < 100 && var1 instanceof OptionButtonWidget) {
				this.settings.setIntOption(((OptionButtonWidget)var1).getOption(), 1);
				var1.text = this.settings.getTranslatedValue(Option.getById(var1.id));
			}

			if(var1.id == 200) {
				this.client.options.saveOptions();
				this.client.openScreen(this.prevScreen);
			}

			if(var1.id != OptionOF.CLOUD_HEIGHT.ordinal()) {
				ScreenScaler var2 = new ScreenScaler(this.client.options, this.client.actualWidth, this.client.actualHeight);
				int var3 = var2.getScaledWidth();
				int var4 = var2.getScaledHeight();
				this.init(this.client, var3, var4);
			}

		}
	}

	public void render(int var1, int var2, float var3) {
		this.renderBackground();
		this.drawTextWithShadowCentred(this.textRenderer, this.title, this.width / 2, 20, 16777215);
		super.render(var1, var2, var3);
		if(Math.abs(var1 - this.lastMouseX) <= 5 && Math.abs(var2 - this.lastMouseY) <= 5) {
			short var4 = 700;
			if(System.currentTimeMillis() >= this.mouseStillTime + (long)var4) {
				int var5 = this.width / 2 - 150;
				int var6 = this.height / 6 - 5;
				if(var2 <= var6 + 98) {
					var6 += 105;
				}

				int var7 = var5 + 150 + 150;
				int var8 = var6 + 84 + 10;
				ButtonWidget var9 = this.getSelectedButton(var1, var2);
				if(var9 != null) {
					String var10 = this.getButtonName(var9.text);
					String[] var11 = this.getTooltipLines(var10);
					if(var11 == null) {
						return;
					}

					this.fillGradient(var5, var6, var7, var8, -536870912, -536870912);

					for(int var12 = 0; var12 < var11.length; ++var12) {
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
		return var1.equals("Clouds") ? new String[]{"Clouds", "  Default - as set by setting Graphics", "  Fast - lower quality, faster", "  Fancy - higher quality, slower", "  OFF - no clouds, fastest", "Fast clouds are rendered 2D.", "Fancy clouds are rendered 3D."} : (var1.equals("Cloud Height") ? new String[]{"Cloud Height", "  OFF - default height", "  100% - above world height limit"} : (var1.equals("Trees") ? new String[]{"Trees", "  Default - as set by setting Graphics", "  Fast - lower quality, faster", "  Fancy - higher quality, slower", "Fast trees have opaque leaves.", "Fancy trees have transparent leaves."} : (var1.equals("Grass") ? new String[]{"Grass", "  Default - as set by setting Graphics", "  Fast - lower quality, faster", "  Fancy - higher quality, slower", "Fast grass uses default side texture.", "Fancy grass uses biome side texture."} : (var1.equals("Water") ? new String[]{"Water", "  Default - as set by setting Graphics", "  Fast  - lower quality, faster", "  Fancy - higher quality, slower", "Fast water (1 pass) has some visual artifacts", "Fancy water (2 pass) has no visual artifacts"} : (var1.equals("Rain & Snow") ? new String[]{"Rain & Snow", "  Default - as set by setting Graphics", "  Fast  - light rain/snow, faster", "  Fancy - heavy rain/snow, slower", "  OFF - no rain/snow, fastest", "When rain is OFF the splashes and rain sounds", "are still active."} : (var1.equals("Sky") ? new String[]{"Sky", "  ON - sky is visible, slower", "  OFF  - sky is not visible, faster", "When sky is OFF the moon and sun are still visible."} : (var1.equals("Stars") ? new String[]{"Stars", "  ON - stars are visible, slower", "  OFF  - stars are not visible, faster"} : (var1.equals("Autosave") ? new String[]{"Autosave interval", "Default autosave interval (2s) is NOT RECOMMENDED.", "Autosave causes the famous Lag Spike of Death."} : (var1.equals("Fast Debug Info") ? new String[]{"Fast Debug Info", " OFF - default debug info screen, slower", " ON - debug info screen without lagometer, faster", "Removes the lagometer from the debug screen (F3)."} : (var1.equals("Chunk Updates") ? new String[]{"Chunk updates per frame", " 1 - (default) slower world loading, higher FPS", " 3 - faster world loading, lower FPS", " 5 - fastest world loading, lowest FPS"} : (var1.equals("Dynamic Updates") ? new String[]{"Chunk updates per frame", " OFF - (default) standard chunk updates per frame", " ON - more updates while the player is standing still", "Dynamic updates force more chunk updates while", "the player is standing still to load the world faster."} : (var1.equals("Anisotropic Filtering") ? new String[]{"Anisotropic Filtering", " OFF - (default) standard texture detail (faster)", " 2-16 - finer details in mipmapped textures (slower)", "The Anisotropic Filtering restores details in mipmapped", "textures. Higher values may decrease the FPS."} : (var1.equals("Antialiasing") ? new String[]{"Antialiasing (effective after a RESTART)", " OFF - (default) no antialiasing (faster)", " 2-16 - antialiased lines and edges (slower)", "The Antialiasing smooths jagged lines and ", "sharp color transitions.", "Higher values may substantially decrease the FPS.", "Effective after a RESTART."} : null)))))))))))));
	}

	private String getButtonName(String var1) {
		int var2 = var1.indexOf(58);
		return var2 < 0 ? var1 : var1.substring(0, var2);
	}

	private ButtonWidget getSelectedButton(int var1, int var2) {
		for(int var3 = 0; var3 < this.buttons.size(); ++var3) {
			ButtonWidget var4 = (ButtonWidget)this.buttons.get(var3);
			boolean var5 = var1 >= var4.x && var2 >= var4.y && var1 < var4.x + var4.width && var2 < var4.y + var4.height;
			if(var5) {
				return var4;
			}
		}

		return null;
	}
}
