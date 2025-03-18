package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.AC_TileEntityWeather;
import dev.adventurecraft.awakening.common.GuiSlider2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiWeather extends Screen {
    private AC_TileEntityWeather weather;
    private GuiSlider2 tempOffset;
    private GuiSlider2 timeOfDay;
    private GuiSlider2 timeRate;

    public AC_GuiWeather(Level var1, AC_TileEntityWeather var2) {
        this.weather = var2;
    }

    public void tick() {
    }

    public void init() {
        OptionButton var1 = new OptionButton(0, 4, 0, "Don\'t Change Precipitation");
        this.buttons.add(var1);
        if (this.weather.changePrecipitate) {
            if (this.weather.precipitate) {
                var1.message = "Start Precipitation";
            } else {
                var1.message = "Stop Precipitation";
            }
        }

        var1 = new OptionButton(1, 4, 22, "Don\'t Change Temperature");
        this.buttons.add(var1);
        if (this.weather.changeTempOffset) {
            var1.message = "Change Temperature";
        }

        this.tempOffset = new GuiSlider2(2, 4, 44, 10, String.format("Temp Offset: %.2f", this.weather.tempOffset), (float) ((this.weather.tempOffset + 1.0D) / 2.0D));
        this.buttons.add(this.tempOffset);
        var1 = new OptionButton(3, 4, 66, "Don\'t Change Time");
        this.buttons.add(var1);
        if (this.weather.changeTimeOfDay) {
            var1.message = "Change Time";
        }

        this.timeOfDay = new GuiSlider2(4, 4, 88, 10, String.format("Time: %d", this.weather.timeOfDay), (float) this.weather.timeOfDay / 24000.0F);
        this.buttons.add(this.timeOfDay);
        var1 = new OptionButton(5, 4, 110, "Don\'t Change Time Rate");
        this.buttons.add(var1);
        if (this.weather.changeTimeRate) {
            var1.message = "Change Time Rate";
        }

        this.timeRate = new GuiSlider2(6, 4, 132, 10, String.format("Time Rate: %.2f", this.weather.timeRate), (this.weather.timeRate + 16.0F) / 32.0F);
        this.buttons.add(this.timeRate);
        var1 = new OptionButton(7, 4, 152, "Don\'t Change Thundering");
        this.buttons.add(var1);
        if (this.weather.changeThundering) {
            if (this.weather.thundering) {
                var1.message = "Start Thundering";
            } else {
                var1.message = "Stop Thundering";
            }
        }

    }

    protected void buttonClicked(Button var1) {
        if (var1.id == 0) {
            if (this.weather.changePrecipitate && this.weather.precipitate) {
                this.weather.precipitate = false;
            } else if (this.weather.changePrecipitate && !this.weather.precipitate) {
                this.weather.changePrecipitate = false;
            } else {
                this.weather.changePrecipitate = true;
                this.weather.precipitate = true;
            }

            if (this.weather.changePrecipitate) {
                if (this.weather.precipitate) {
                    var1.message = "Start Precipitation";
                } else {
                    var1.message = "Stop Precipitation";
                }
            } else {
                var1.message = "Don\'t Change Precipitation";
            }
        }

        if (var1.id == 7) {
            if (this.weather.changeThundering && this.weather.thundering) {
                this.weather.thundering = false;
            } else if (this.weather.changeThundering && !this.weather.thundering) {
                this.weather.changeThundering = false;
            } else {
                this.weather.changeThundering = true;
                this.weather.thundering = true;
            }

            if (this.weather.changeThundering) {
                if (this.weather.thundering) {
                    var1.message = "Start Thundering";
                } else {
                    var1.message = "Stop Thundering";
                }
            } else {
                var1.message = "Don\'t Change Thundering";
            }
        } else if (var1.id == 1) {
            this.weather.changeTempOffset = !this.weather.changeTempOffset;
            if (this.weather.changeTempOffset) {
                var1.message = "Change Temperature";
            } else {
                var1.message = "Don\'t Change Temperature";
            }
        } else if (var1.id == 3) {
            this.weather.changeTimeOfDay = !this.weather.changeTimeOfDay;
            if (this.weather.changeTimeOfDay) {
                var1.message = "Change Time";
            } else {
                var1.message = "Don\'t Change Time";
            }
        } else if (var1.id == 5) {
            this.weather.changeTimeRate = !this.weather.changeTimeRate;
            if (this.weather.changeTimeRate) {
                var1.message = "Change Time Rate";
            } else {
                var1.message = "Don\'t Change Time Rate";
            }
        }

    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.weather.tempOffset = (double) this.tempOffset.sliderValue * 2.0D - 1.0D;
        this.tempOffset.message = String.format("Temp Offset: %.2f", this.weather.tempOffset);
        this.weather.timeOfDay = (int) (this.timeOfDay.sliderValue * 24000.0F);
        this.timeOfDay.message = String.format("Time: %d", this.weather.timeOfDay);
        this.weather.timeRate = this.timeRate.sliderValue * 32.0F - 16.0F;
        this.timeRate.message = String.format("Time Rate: %.2f", this.weather.timeRate);
        super.render(var1, var2, var3);
    }

    public static void showUI(Level var0, AC_TileEntityWeather var1) {
        Minecraft.instance.setScreen(new AC_GuiWeather(var0, var1));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
