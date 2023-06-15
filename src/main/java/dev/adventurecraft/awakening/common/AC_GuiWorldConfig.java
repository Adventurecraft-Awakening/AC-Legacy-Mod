package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextboxWidget;
import net.minecraft.world.World;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;

public class AC_GuiWorldConfig extends Screen {

    private int page = 0;
    private final World world;
    private TextboxWidget playerName;
    int selectedID = 0;
    private ButtonWidget setOnNewSave;
    private ButtonWidget setOnLoad;
    private ButtonWidget setOnUpdate;

    private final NumberFormat numFormat;
    private ButtonWidget applyLightButton;
    private ButtonWidget resetLightButton;
    private ButtonWidget restoreLightButton;
    private final float[] lightLevels;
    private final TextboxWidget[] lightLevelInputs;
    private final int[] lightLevelTextColors;

    public AC_GuiWorldConfig(World world) {
        this.world = world;
        this.numFormat = NumberFormat.getInstance();
        this.numFormat.setMaximumFractionDigits(7);
        this.lightLevels = ((ExWorldProperties) this.world.properties).getBrightness().clone();
        this.lightLevelInputs = new TextboxWidget[16];
        this.lightLevelTextColors = new int[this.lightLevelInputs.length];
        Arrays.fill(this.lightLevelTextColors, 0xA0A0A0);
    }

    @Override
    public void tick() {
        if (this.page == 0) {
            this.playerName.tick();
        } else if (this.page == 2) {
            for (int i = 0; i < 16; ++i) {
                this.lightLevelInputs[i].tick();
            }
        }
    }

    @Override
    public void initVanillaScreen() {
        int var1 = (this.width - 16) / 4;
        this.buttons.add(new ButtonWidget(-1, 4, 0, var1, 18, "Misc"));
        this.buttons.add(new ButtonWidget(-2, 4 + 4 + var1, 0, var1, 18, "Script"));
        this.buttons.add(new ButtonWidget(-3, 4 + 2 * (4 + var1), 0, var1, 18, "Light Levels"));

        var1 = (this.width - 16) / 3;

        if (this.page == 0) {
            int var2 = this.textRenderer.getTextWidth("Player Name:") + 8;
            this.playerName = new TextboxWidget(
                this, this.textRenderer, var2, 24, this.width / 2 - var2, 20, ((ExWorldProperties) this.world.properties).getPlayerName());
            this.playerName.selected = true;
            this.playerName.setMaxLength(32);
            ButtonWidget var3 = new ButtonWidget(0, 4, 44, var1, 18, "Crafting: Enabled");
            this.buttons.add(var3);
            if (!((ExWorldProperties) this.world.properties).getAllowsInventoryCrafting()) {
                var3.text = "Crafting: Disabled";
            }
        } else if (this.page == 1) {
            this.selectedID = 0;
            var props = (ExWorldProperties) this.world.properties;
            this.setOnNewSave = new ButtonWidget(0, 4, 24, "OnNewSave (selected): " + props.getOnNewSaveScript());
            this.setOnLoad = new ButtonWidget(1, 4, 46, "OnLoad: " + props.getOnLoadScript());
            this.setOnUpdate = new ButtonWidget(2, 4, 68, "OnUpdate: " + props.getOnUpdateScript());
            this.buttons.add(this.setOnNewSave);
            this.buttons.add(this.setOnLoad);
            this.buttons.add(this.setOnUpdate);
            ButtonWidget var9 = new ButtonWidget(3, 4, 90, 200, 20, "Reload Scripts");
            this.buttons.add(var9);
            var9 = new ButtonWidget(4, 4, 112, 160, 18, "None");
            this.buttons.add(var9);
            String[] var10 = ((ExWorld) this.world).getScriptFiles();
            if (var10 != null) {
                int var4 = 1;
                String[] var5 = var10;
                int var6 = var10.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    String var8 = var5[var7];
                    var9 = new ButtonWidget(4 + var4, 4 + var4 % 3 * this.width / 3, 112 + var4 / 3 * 20, 160, 18, var8);
                    this.buttons.add(var9);
                    ++var4;
                }
            }
        } else if (this.page == 2) {
            for (int i = 0; i < 16; ++i) {
                this.lightLevelInputs[i] = new TextboxWidget(
                    this, this.textRenderer, 80, 22 + 14 * i, 80, 11, this.numFormat.format(this.lightLevels[i]));
            }

            this.applyLightButton = new ButtonWidget(0, 180, 24, 100, 20, "Apply values");
            this.buttons.add(this.applyLightButton);

            this.resetLightButton = new ButtonWidget(1, 180, 54, 100, 20, "Reset values");
            this.buttons.add(this.resetLightButton);

            this.restoreLightButton = new ButtonWidget(2, 180, 84, 100, 20, "Restore values");
            this.buttons.add(this.restoreLightButton);
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id < 0) {
            this.page = -button.id - 1;
            this.buttons.clear();
            this.initVanillaScreen();
            return;
        }

        if (this.page == 0) {
            if (button.id == 0) {
                var props = (ExWorldProperties) this.world.properties;
                props.setAllowsInventoryCrafting(!props.getAllowsInventoryCrafting());
                if (props.getAllowsInventoryCrafting()) {
                    button.text = "Crafting: Enabled";
                } else {
                    button.text = "Crafting: Disabled";
                }
            }
        } else if (this.page == 1) {
            if (button.id < 3) {
                this.selectedID = button.id;
            } else if (button.id == 3) {
                ((ExWorld) this.world).getScriptHandler().loadScripts();
            } else if (button.id == 4) {
                this.updateScriptFile("");
            } else {
                this.updateScriptFile(button.text);
            }

            this.resetScriptNames();
        } else if (this.page == 2) {
            if (button.id == this.applyLightButton.id) {
                this.applyLightInputs();
            } else if (button.id == this.resetLightButton.id) {
                this.resetLightInputs();
            } else if (button.id == this.restoreLightButton.id) {
                this.restoreLightInputs();
            }
        }
    }

    private void updateScriptFile(String var1) {
        var props = (ExWorldProperties) this.world.properties;
        if (this.selectedID == 0) {
            props.setOnNewSaveScript(var1);
        } else if (this.selectedID == 1) {
            props.setOnLoadScript(var1);
        } else if (this.selectedID == 2) {
            props.setOnUpdateScript(var1);
        }
    }

    private void resetScriptNames() {
        var props = (ExWorldProperties) this.world.properties;
        this.setOnNewSave.text = "OnNewSave: " + props.getOnNewSaveScript();
        this.setOnLoad.text = "OnLoad: " + props.getOnLoadScript();
        this.setOnUpdate.text = "OnUpdate: " + props.getOnUpdateScript();

        if (this.selectedID == 0) {
            this.setOnNewSave.text = "OnNewSave (selected): " + props.getOnNewSaveScript();
        } else if (this.selectedID == 1) {
            this.setOnLoad.text = "OnLoad (selected): " + props.getOnLoadScript();
        } else if (this.selectedID == 2) {
            this.setOnUpdate.text = "OnUpdate (selected): " + props.getOnUpdateScript();
        }
    }

    @Override
    protected void keyPressed(char character, int key) {
        if (this.page == 0) {
            if (this.playerName.selected) {
                this.playerName.keyPressed(character, key);
            }
        } else if (this.page == 2) {
            for (int i = 0; i < 16; ++i) {
                if (this.lightLevelInputs[i].selected) {
                    this.lightLevelInputs[i].keyPressed(character, key);
                }
            }
        }

        super.keyPressed(character, key);
    }

    private void applyLightInputs() {
        float[] brightness = ((ExWorldProperties) this.world.properties).getBrightness();
        for (int i = 0; i < 16; ++i) {
            brightness[i] = this.lightLevels[i];
            this.lightLevelInputs[i].setText(this.numFormat.format(brightness[i]));
        }

        ((ExWorld) this.world).loadBrightness();
        ((ExWorldEventRenderer) this.client.worldRenderer).updateAllTheRenderers();
    }

    private void resetLightInputs() {
        float[] brightness = ((ExWorldProperties) this.world.properties).getBrightness().clone();
        for (int i = 0; i < 16; ++i) {
            this.lightLevelInputs[i].setText(this.numFormat.format(brightness[i]));
        }
    }

    private void restoreLightInputs() {
        float baseValue = 0.05F;

        for (int i = 0; i < 16; ++i) {
            float v = 1.0F - (float) i / 15.0F;
            float lightValue = (1.0F - v) / (v * 3.0F + 1.0F) * (1.0F - baseValue) + baseValue;
            this.lightLevelInputs[i].setText(this.numFormat.format(lightValue));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.page == 0) {
            this.playerName.mouseClicked(mouseX, mouseY, mouseButton);
        } else if (this.page == 2) {
            for (int i = 0; i < 16; ++i) {
                this.lightLevelInputs[i].mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        if (this.page == 0) {
            this.drawTextWithShadow(this.textRenderer, "Player Name:", 4, 30, 0xA0A0A0);
            this.playerName.draw();
            ((ExWorldProperties) this.world.properties).setPlayerName(this.playerName.getText());
            this.client.player.name = this.playerName.getText();
        } else if (this.page == 2) {
            float[] brightness = ((ExWorldProperties) this.world.properties).getBrightness();
            for (int i = 0; i < 16; ++i) {
                this.drawTextWithShadow(this.textRenderer, String.format("Light Level %d", i), 4, 24 + 14 * i, this.lightLevelTextColors[i]);
                this.lightLevelInputs[i].draw();

                try {
                    float value = this.numFormat.parse(this.lightLevelInputs[i].getText()).floatValue();
                    if (value != brightness[i]) {
                        this.lightLevels[i] = value;
                        this.lightLevelTextColors[i] = 0x00A000;
                    } else {
                        this.lightLevelTextColors[i] = 0xA0A0A0;
                    }
                } catch (ParseException ex) {
                    this.lightLevelTextColors[i] = 0xA00000;
                }
            }
        }

        super.render(mouseX, mouseY, var3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
