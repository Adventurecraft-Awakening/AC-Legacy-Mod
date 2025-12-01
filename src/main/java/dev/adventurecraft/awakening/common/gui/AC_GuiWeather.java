package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.entity.AC_TileEntityWeather;
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

    public AC_GuiWeather(AC_TileEntityWeather entity) {
        this.weather = entity;
    }

    public void tick() {
    }

    public void init() {
        OptionButton var1 = new OptionButton(0, 4, 0, "Don't Change Precipitation");
        this.buttons.add(var1);
        if (this.weather.changePrecipitate) {
            if (this.weather.precipitate) {
                var1.message = "Start Precipitation";
            } else {
                var1.message = "Stop Precipitation";
            }
        }

        var1 = new OptionButton(1, 4, 22, "Don't Change Temperature");
        this.buttons.add(var1);
        if (this.weather.changeTempOffset) {
            var1.message = "Change Temperature";
        }

        this.tempOffset = new GuiSlider2(2, 4, 44, 10, String.format("Temp Offset: %.2f", this.weather.tempOffset), (float) ((this.weather.tempOffset + 1.0D) / 2.0D));
        this.buttons.add(this.tempOffset);

        var1 = new OptionButton(3, 4, 66, "Don't Change Time");
        this.buttons.add(var1);
        if (this.weather.changeTimeOfDay) {
            var1.message = "Change Time";
        }

        this.timeOfDay = new GuiSlider2(4, 4, 88, 10, String.format("Time: %d", this.weather.timeOfDay), (float) this.weather.timeOfDay / 24000.0F);
        this.buttons.add(this.timeOfDay);

        var1 = new OptionButton(5, 4, 110, "Don't Change Time Rate");
        this.buttons.add(var1);
        if (this.weather.changeTimeRate) {
            var1.message = "Change Time Rate";
        }

        this.timeRate = new GuiSlider2(6, 4, 132, 10, String.format("Time Rate: %.2f", this.weather.timeRate), (this.weather.timeRate + 16.0F) / 32.0F);
        this.buttons.add(this.timeRate);

        var1 = new OptionButton(7, 4, 152, "Don't Change Thundering");
        this.buttons.add(var1);
        if (this.weather.changeThundering) {
            if (this.weather.thundering) {
                var1.message = "Start Thundering";
            } else {
                var1.message = "Stop Thundering";
            }
        }
    }

    protected void buttonClicked(Button btn) {
        AC_TileEntityWeather weather = this.weather;
        if (btn.id == 0) {
            if (weather.changePrecipitate && weather.precipitate) {
                weather.precipitate = false;
            } else if (weather.changePrecipitate && !weather.precipitate) {
                weather.changePrecipitate = false;
            } else {
                weather.changePrecipitate = true;
                weather.precipitate = true;
            }

            if (weather.changePrecipitate) {
                if (weather.precipitate) {
                    btn.message = "Start Precipitation";
                } else {
                    btn.message = "Stop Precipitation";
                }
            } else {
                btn.message = "Don't Change Precipitation";
            }
        }

        if (btn.id == 7) {
            if (weather.changeThundering && weather.thundering) {
                weather.thundering = false;
            } else if (weather.changeThundering && !weather.thundering) {
                weather.changeThundering = false;
            } else {
                weather.changeThundering = true;
                weather.thundering = true;
            }

            if (weather.changeThundering) {
                if (weather.thundering) {
                    btn.message = "Start Thundering";
                } else {
                    btn.message = "Stop Thundering";
                }
            } else {
                btn.message = "Don't Change Thundering";
            }
        } else if (btn.id == 1) {
            weather.changeTempOffset = !weather.changeTempOffset;
            if (weather.changeTempOffset) {
                btn.message = "Change Temperature";
            } else {
                btn.message = "Don't Change Temperature";
            }
        } else if (btn.id == 3) {
            weather.changeTimeOfDay = !weather.changeTimeOfDay;
            if (weather.changeTimeOfDay) {
                btn.message = "Change Time";
            } else {
                btn.message = "Don't Change Time";
            }
        } else if (btn.id == 5) {
            weather.changeTimeRate = !weather.changeTimeRate;
            if (weather.changeTimeRate) {
                btn.message = "Change Time Rate";
            } else {
                btn.message = "Don't Change Time Rate";
            }
        }

        weather.setChanged();
    }

    public void render(int mouseX, int mouseY, float tick) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);

        AC_TileEntityWeather weather = this.weather;
        weather.tempOffset = this.tempOffset.sliderValue * 2.0f - 1.0f;
        this.tempOffset.message = String.format("Temp Offset: %.2f", weather.tempOffset);
        weather.timeOfDay = (int) (this.timeOfDay.sliderValue * 24000.0F);
        this.timeOfDay.message = String.format("Time: %d", weather.timeOfDay);
        weather.timeRate = this.timeRate.sliderValue * 32.0F - 16.0F;
        this.timeRate.message = String.format("Time Rate: %.2f", weather.timeRate);

        super.render(mouseX, mouseY, tick);
    }

    public static void showUI(AC_TileEntityWeather entity) {
        Minecraft.instance.setScreen(new AC_GuiWeather(entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
