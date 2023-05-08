package dev.adventurecraft.awakening.common;

import java.io.File;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class AC_GuiScript extends Screen {
    AC_TileEntityScript script;
    ButtonWidget setOnTrigger;
    ButtonWidget setOnDetrigger;
    ButtonWidget setOnUpdate;
    int selectedID;

    public AC_GuiScript(AC_TileEntityScript var1) {
        this.script = var1;
    }

    @Override
    public void initVanillaScreen() {
        this.selectedID = 0;
        this.setOnTrigger = new ButtonWidget(0, 4, 4, "OnTrigger (selected): " + this.script.onTriggerScriptFile);
        this.setOnDetrigger = new ButtonWidget(1, 4, 26, "OnDetrigger: " + this.script.onDetriggerScriptFile);
        this.setOnUpdate = new ButtonWidget(2, 4, 48, "OnUpdate: " + this.script.onUpdateScriptFile);
        this.buttons.add(this.setOnTrigger);
        this.buttons.add(this.setOnDetrigger);
        this.buttons.add(this.setOnUpdate);
        ButtonWidget var1 = new ButtonWidget(3, 4, 70, 200, 20, "Reload Scripts");
        this.buttons.add(var1);
        var1 = new ButtonWidget(4, 4, 92, 160, 18, "None");
        this.buttons.add(var1);
        String[] var2 = ((ExWorld) this.client.world).getScriptFiles();
        if (var2 != null) {
            int var3 = 1;
            String[] var4 = var2;
            int var5 = var2.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String var7 = var4[var6];
                var1 = new ButtonWidget(4 + var3, 4 + var3 % 3 * this.width / 3, 92 + var3 / 3 * 20, 160, 18, var7);
                this.buttons.add(var1);
                ++var3;
            }
        }
    }

    private void resetNames() {
        this.setOnTrigger.text = "OnTrigger: " + this.script.onTriggerScriptFile;
        this.setOnDetrigger.text = "OnDetrigger: " + this.script.onDetriggerScriptFile;
        this.setOnUpdate.text = "OnUpdate: " + this.script.onUpdateScriptFile;
        if (this.selectedID == 0) {
            this.setOnTrigger.text = "OnTrigger (selected): " + this.script.onTriggerScriptFile;
        } else if (this.selectedID == 1) {
            this.setOnDetrigger.text = "OnDetrigger (selected): " + this.script.onDetriggerScriptFile;
        } else if (this.selectedID == 2) {
            this.setOnUpdate.text = "OnUpdate (selected): " + this.script.onUpdateScriptFile;
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id < 3) {
            this.selectedID = var1.id;
        } else if (var1.id == 3) {
            ((ExWorld) this.script.world).getScriptHandler().loadScripts();
        } else if (var1.id == 4) {
            this.updateScriptFile("");
        } else {
            this.updateScriptFile(var1.text);
        }

        this.resetNames();
    }

    private void updateScriptFile(String var1) {
        if (this.selectedID == 0) {
            this.script.onTriggerScriptFile = var1;
        } else if (this.selectedID == 1) {
            this.script.onDetriggerScriptFile = var1;
        } else if (this.selectedID == 2) {
            this.script.onUpdateScriptFile = var1;
        }
    }

    @Override
    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        super.render(var1, var2, var3);
    }

    public static void showUI(AC_TileEntityScript var0) {
        Minecraft.instance.openScreen(new AC_GuiScript(var0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
