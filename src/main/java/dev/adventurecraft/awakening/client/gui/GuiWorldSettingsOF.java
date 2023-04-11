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

public class GuiWorldSettingsOF extends Screen {
	private Screen prevScreen;
	protected String title = "World Settings";
	private GameOptions settings;
	private static Option[] enumOptions = new Option[]{OptionOF.LOAD_FAR, OptionOF.PRELOADED_CHUNKS, OptionOF.CHUNK_UPDATES, OptionOF.CHUNK_UPDATES_DYNAMIC, OptionOF.WEATHER, OptionOF.TIME, OptionOF.FAR_VIEW};
	private int lastMouseX = 0;
	private int lastMouseY = 0;
	private long mouseStillTime = 0L;

	public GuiWorldSettingsOF(Screen var1, GameOptions var2) {
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
		return var1.equals("Load Far") ? new String[]{"Loads the world chunks at distance Far.", "Switching the render distance does not cause all chunks ", "to be loaded again.", "  OFF - world chunks loaded up to render distance", "  ON - world chunks loaded at distance Far, allows", "       fast render distance switching"} : (var1.equals("Preloaded Chunks") ? new String[]{"Defines an area in which no chunks will be loaded", "  OFF - after 5m new chunks will be loaded", "  2 - after 32m  new chunks will be loaded", "  8 - after 128m new chunks will be loaded", "Higher values need more time to load all the chunks"} : (var1.equals("Chunk Updates") ? new String[]{"Chunk updates per frame", " 1 - (default) slower world loading, higher FPS", " 3 - faster world loading, lower FPS", " 5 - fastest world loading, lowest FPS"} : (var1.equals("Dynamic Updates") ? new String[]{"Chunk updates per frame", " OFF - (default) standard chunk updates per frame", " ON - more updates while the player is standing still", "Dynamic updates force more chunk updates while", "the player is standing still to load the world faster."} : (var1.equals("Far View") ? new String[]{"Far View", " OFF - (default) standard view distance", " ON - 3x view distance", "Far View is very resource demanding!", "3x view distance => 9x chunks to be loaded => FPS / 9", "Standard view distances: 32, 64, 128, 256", "Far view distances: 96, 192, 384, 512"} : (var1.equals("Time") ? new String[]{"Time", " Default - normal day/night cycles", " Day Only - day only", " Night Only - night only"} : (var1.equals("Weather") ? new String[]{"Weather", "  ON - weather is active, slower", "  OFF  - weather is not active, faster", "The weather controls rain, snow and thunderstorms."} : null))))));
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
