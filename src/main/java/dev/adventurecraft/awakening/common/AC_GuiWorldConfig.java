package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextboxWidget;
import net.minecraft.world.World;

// TODO: go through accesses to world

public class AC_GuiWorldConfig extends Screen {

    private int page = 0;
    private World world;
    private TextboxWidget playerName;
    int selectedID = 0;
    private ButtonWidget setOnNewSave;
    private ButtonWidget setOnLoad;
    private ButtonWidget setOnUpdate;
    private TextboxWidget[] lightLevels;
    private boolean lightChanged = false;

    public AC_GuiWorldConfig(World var1) {
        this.world = var1;
        this.lightLevels = new TextboxWidget[16];
    }

    @Override
    public void tick() {
        if (this.page == 0) {
            this.playerName.tick();
        } else if (this.page == 2) {
            for (int var1 = 0; var1 < 16; ++var1) {
                this.lightLevels[var1].tick();
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
            if (!((ExWorldProperties) this.client.world.properties).getAllowsInventoryCrafting()) {
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
            String[] var10 = ((ExWorld) this.client.world).getScriptFiles();
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
            float[] brightness = ((ExWorldProperties) this.client.world.properties).getBrightness();
            for (int i = 0; i < 16; ++i) {
                this.lightLevels[i] = new TextboxWidget(
                    this, this.textRenderer, 80, 22 + 14 * i, 80, 11, String.format("%.7f", brightness[i]));
            }
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id < 0) {
            this.page = -var1.id - 1;
            this.buttons.clear();
            this.initVanillaScreen();
        } else {
            if (this.page == 0) {
                if (var1.id == 0) {
                    var props = (ExWorldProperties) this.client.world.properties;
                    props.setAllowsInventoryCrafting(!props.getAllowsInventoryCrafting());
                    if (props.getAllowsInventoryCrafting()) {
                        var1.text = "Crafting: Enabled";
                    } else {
                        var1.text = "Crafting: Disabled";
                    }
                }
            } else if (this.page == 1) {
                if (var1.id < 3) {
                    this.selectedID = var1.id;
                } else if (var1.id == 3) {
                    ((ExWorld) this.world).getScriptHandler().loadScripts();
                } else if (var1.id == 4) {
                    this.updateScriptFile("");
                } else {
                    this.updateScriptFile(var1.text);
                }

                this.resetScriptNames();
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
    protected void keyPressed(char var1, int var2) {
        if (this.page == 0) {
            if (this.playerName.selected) {
                this.playerName.keyPressed(var1, var2);
            }
        } else if (this.page == 2) {
            for (int var3 = 0; var3 < 16; ++var3) {
                if (this.lightLevels[var3].selected && (var2 == 14 || var1 >= 48 && var1 <= 57 || var1 == 46 || var1 == 9)) {
                    this.lightLevels[var3].keyPressed(var1, var2);
                }
            }
        }

        super.keyPressed(var1, var2);
        if (var2 == 1 && this.lightChanged) {
            ((ExWorldEventRenderer) this.client.worldRenderer).updateAllTheRenderers();
        }
    }

    @Override
    protected void mouseClicked(int var1, int var2, int var3) {
        super.mouseClicked(var1, var2, var3);
        if (this.page == 0) {
            this.playerName.mouseClicked(var1, var2, var3);
        } else if (this.page == 2) {
            for (int var4 = 0; var4 < 16; ++var4) {
                this.lightLevels[var4].mouseClicked(var1, var2, var3);
            }
        }
    }

    @Override
    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        if (this.page == 0) {
            this.drawTextWithShadow(this.textRenderer, "Player Name:", 4, 30, 10526880);
            this.playerName.draw();
            ((ExWorldProperties) this.world.properties).setPlayerName(this.playerName.getText());
            this.client.player.name = this.playerName.getText();
        } else if (this.page != 1) {
            if (this.page == 2) {
                for (int var4 = 0; var4 < 16; ++var4) {
                    this.drawTextWithShadow(this.textRenderer, String.format("Light Level %d", var4), 4, 24 + 14 * var4, 10526880);
                    this.lightLevels[var4].draw();

                    try {
                        float[] brightness = ((ExWorldProperties) this.client.world.properties).getBrightness();
                        float var5 = Float.parseFloat(this.lightLevels[var4].getText());
                        if ((double) var5 != Math.floor(brightness[var4] * 1.0E7F) / 1.0E7D) {
                            brightness[var4] = var5;
                            this.lightChanged = true;
                        }
                    } catch (NumberFormatException var6) {
                    }
                }

                ((ExWorld) this.client.world).loadBrightness();
            }
        }

        super.render(var1, var2, var3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
