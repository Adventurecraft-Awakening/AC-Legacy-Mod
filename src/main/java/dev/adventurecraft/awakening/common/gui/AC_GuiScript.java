package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.entity.AC_TileEntityScript;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiScript extends Screen {
    AC_TileEntityScript script;
    Button setOnTrigger;
    Button setOnDetrigger;
    Button setOnUpdate;
    int selectedID;

    public AC_GuiScript(AC_TileEntityScript var1) {
        this.script = var1;
    }

    @Override
    public void init() {
        this.selectedID = 0;
        this.setOnTrigger = new Button(0, 4, 4, "OnTrigger (selected): " + this.script.onTriggerScriptFile);
        this.setOnDetrigger = new Button(1, 4, 26, "OnDetrigger: " + this.script.onDetriggerScriptFile);
        this.setOnUpdate = new Button(2, 4, 48, "OnUpdate: " + this.script.onUpdateScriptFile);
        this.buttons.add(this.setOnTrigger);
        this.buttons.add(this.setOnDetrigger);
        this.buttons.add(this.setOnUpdate);

        this.buttons.add(new Button(3, 4, 70, 200, 20, "Reload Scripts"));
        this.buttons.add(new Button(4, 4, 92, 160, 18, "None"));

        String[] files = ((ExWorld) this.minecraft.level).getScriptFiles();
        int id = 1;
        for (String file : files) {
            int x = 4 + id % 3 * this.width / 3;
            int y = 92 + id / 3 * 20;
            var btn = new Button(4 + id, x, y, 160, 18, file);
            this.buttons.add(btn);
            ++id;
        }
    }

    private void resetNames() {
        this.setOnTrigger.message = "OnTrigger: " + this.script.onTriggerScriptFile;
        this.setOnDetrigger.message = "OnDetrigger: " + this.script.onDetriggerScriptFile;
        this.setOnUpdate.message = "OnUpdate: " + this.script.onUpdateScriptFile;
        if (this.selectedID == 0) {
            this.setOnTrigger.message = "OnTrigger (selected): " + this.script.onTriggerScriptFile;
        } else if (this.selectedID == 1) {
            this.setOnDetrigger.message = "OnDetrigger (selected): " + this.script.onDetriggerScriptFile;
        } else if (this.selectedID == 2) {
            this.setOnUpdate.message = "OnUpdate (selected): " + this.script.onUpdateScriptFile;
        }
    }

    @Override
    protected void buttonClicked(Button var1) {
        if (var1.id < 3) {
            this.selectedID = var1.id;
        } else if (var1.id == 3) {
            ((ExWorld) this.script.level).getScriptHandler().loadScripts();
        } else if (var1.id == 4) {
            this.updateScriptFile("");
        } else {
            this.updateScriptFile(var1.message);
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
        Minecraft.instance.setScreen(new AC_GuiScript(var0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
