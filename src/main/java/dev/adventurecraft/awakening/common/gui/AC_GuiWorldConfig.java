package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.LightHelper;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiWorldConfig extends Screen {

    private int page = 0;
    private final Level world;
    private EditBox playerName;
    int selectedID = 0;
    private Button setOnNewSave;
    private Button setOnLoad;
    private Button setOnUpdate;

    private final NumberFormat numFormat;
    private Button applyLightButton;
    private Button resetLightButton;
    private Button restoreLightButton;
    private final float[] lightLevels;
    private final EditBox[] lightLevelInputs;
    private final int[] lightLevelTextColors;

    public AC_GuiWorldConfig(Level world) {
        this.world = world;
        this.numFormat = NumberFormat.getInstance();
        this.numFormat.setMaximumFractionDigits(7);
        this.lightLevels = ((ExWorldProperties) this.world.levelData).getBrightness().clone();
        this.lightLevelInputs = new EditBox[16];
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
    public void init() {
        int var1 = (this.width - 16) / 4;
        this.buttons.add(new Button(-1, 4, 0, var1, 18, "Misc"));
        this.buttons.add(new Button(-2, 4 + 4 + var1, 0, var1, 18, "Script"));
        this.buttons.add(new Button(-3, 4 + 2 * (4 + var1), 0, var1, 18, "Light Levels"));

        var1 = (this.width - 16) / 3;

        if (this.page == 0) {
            int var2 = this.font.width("Player Name:") + 8;
            this.playerName = new EditBox(
                this, this.font, var2, 24, this.width / 2 - var2, 20, ((ExWorldProperties) this.world.levelData).getPlayerName());
            this.playerName.active = true;
            this.playerName.setMaxLength(32);
            Button var3 = new Button(0, 4, 44, var1, 18, "Crafting: Enabled");
            this.buttons.add(var3);
            if (!((ExWorldProperties) this.world.levelData).getAllowsInventoryCrafting()) {
                var3.message = "Crafting: Disabled";
            }
        } else if (this.page == 1) {
            this.selectedID = 0;
            var props = (ExWorldProperties) this.world.levelData;
            this.setOnNewSave = new Button(0, 4, 24, "OnNewSave (selected): " + props.getOnNewSaveScript());
            this.setOnLoad = new Button(1, 4, 46, "OnLoad: " + props.getOnLoadScript());
            this.setOnUpdate = new Button(2, 4, 68, "OnUpdate: " + props.getOnUpdateScript());
            this.buttons.add(this.setOnNewSave);
            this.buttons.add(this.setOnLoad);
            this.buttons.add(this.setOnUpdate);
            Button var9 = new Button(3, 4, 90, 200, 20, "Reload Scripts");
            this.buttons.add(var9);
            var9 = new Button(4, 4, 112, 160, 18, "None");
            this.buttons.add(var9);
            String[] var10 = ((ExWorld) this.world).getScriptFiles();
            if (var10 != null) {
                int var4 = 1;
                String[] var5 = var10;
                int var6 = var10.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    String var8 = var5[var7];
                    var9 = new Button(4 + var4, 4 + var4 % 3 * this.width / 3, 112 + var4 / 3 * 20, 160, 18, var8);
                    this.buttons.add(var9);
                    ++var4;
                }
            }
        } else if (this.page == 2) {
            for (int i = 0; i < 16; ++i) {
                this.lightLevelInputs[i] = new EditBox(
                    this, this.font, 80, 22 + 14 * i, 80, 11, this.numFormat.format(this.lightLevels[i]));
            }

            this.applyLightButton = new Button(0, 180, 24, 100, 20, "Apply values");
            this.buttons.add(this.applyLightButton);

            this.resetLightButton = new Button(1, 180, 54, 100, 20, "Reset values");
            this.buttons.add(this.resetLightButton);

            this.restoreLightButton = new Button(2, 180, 84, 100, 20, "Restore values");
            this.buttons.add(this.restoreLightButton);
        }
    }

    @Override
    protected void buttonClicked(Button button) {
        if (button.id < 0) {
            this.page = -button.id - 1;
            this.buttons.clear();
            this.init();
            return;
        }

        if (this.page == 0) {
            if (button.id == 0) {
                var props = (ExWorldProperties) this.world.levelData;
                props.setAllowsInventoryCrafting(!props.getAllowsInventoryCrafting());
                if (props.getAllowsInventoryCrafting()) {
                    button.message = "Crafting: Enabled";
                } else {
                    button.message = "Crafting: Disabled";
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
                this.updateScriptFile(button.message);
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
        var props = (ExWorldProperties) this.world.levelData;
        if (this.selectedID == 0) {
            props.setOnNewSaveScript(var1);
        } else if (this.selectedID == 1) {
            props.setOnLoadScript(var1);
        } else if (this.selectedID == 2) {
            props.setOnUpdateScript(var1);
        }
    }

    private void resetScriptNames() {
        var props = (ExWorldProperties) this.world.levelData;
        this.setOnNewSave.message = "OnNewSave: " + props.getOnNewSaveScript();
        this.setOnLoad.message = "OnLoad: " + props.getOnLoadScript();
        this.setOnUpdate.message = "OnUpdate: " + props.getOnUpdateScript();

        if (this.selectedID == 0) {
            this.setOnNewSave.message = "OnNewSave (selected): " + props.getOnNewSaveScript();
        } else if (this.selectedID == 1) {
            this.setOnLoad.message = "OnLoad (selected): " + props.getOnLoadScript();
        } else if (this.selectedID == 2) {
            this.setOnUpdate.message = "OnUpdate (selected): " + props.getOnUpdateScript();
        }
    }

    @Override
    protected void keyPressed(char character, int key) {
        if (this.page == 0) {
            if (this.playerName.active) {
                this.playerName.charTyped(character, key);
            }
        } else if (this.page == 2) {
            for (int i = 0; i < 16; ++i) {
                if (this.lightLevelInputs[i].active) {
                    this.lightLevelInputs[i].charTyped(character, key);
                }
            }
        }

        super.keyPressed(character, key);
    }

    private void applyLightInputs() {
        float[] brightness = ((ExWorldProperties) this.world.levelData).getBrightness();
        for (int i = 0; i < 16; ++i) {
            brightness[i] = this.lightLevels[i];
            this.lightLevelInputs[i].setValue(this.numFormat.format(brightness[i]));
        }

        ((ExWorld) this.world).loadBrightness();
        ((ExWorldEventRenderer) this.minecraft.levelRenderer).updateAllTheRenderers();
    }

    private void resetLightInputs() {
        float[] brightness = ((ExWorldProperties) this.world.levelData).getBrightness().clone();
        for (int i = 0; i < 16; ++i) {
            this.lightLevelInputs[i].setValue(this.numFormat.format(brightness[i]));
        }
    }

    private void restoreLightInputs() {
        for (int i = 0; i < 16; ++i) {
            float lightValue = LightHelper.getDefaultLightAtIndex(i);
            this.lightLevelInputs[i].setValue(this.numFormat.format(lightValue));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.page == 0) {
            this.playerName.clicked(mouseX, mouseY, mouseButton);
        } else if (this.page == 2) {
            for (int i = 0; i < 16; ++i) {
                this.lightLevelInputs[i].clicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        if (this.page == 0) {
            this.drawString(this.font, "Player Name:", 4, 30, 0xA0A0A0);
            this.playerName.render();
            ((ExWorldProperties) this.world.levelData).setPlayerName(this.playerName.getValue());
            this.minecraft.player.name = this.playerName.getValue();
        } else if (this.page == 2) {
            float[] brightness = ((ExWorldProperties) this.world.levelData).getBrightness();
            for (int i = 0; i < 16; ++i) {
                this.drawString(this.font, String.format("Light Level %d", i), 4, 24 + 14 * i, this.lightLevelTextColors[i]);
                this.lightLevelInputs[i].render();

                try {
                    float value = this.numFormat.parse(this.lightLevelInputs[i].getValue()).floatValue();
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
