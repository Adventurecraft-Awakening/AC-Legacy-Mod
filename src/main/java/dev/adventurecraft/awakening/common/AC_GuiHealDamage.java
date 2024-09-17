package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiHealDamage extends Screen {
    private AC_TileEntityHealDamage tileEnt;
    GuiSlider2 healDamage;

    public AC_GuiHealDamage(AC_TileEntityHealDamage var1) {
        this.tileEnt = var1;
    }

    public void tick() {
    }

    public void init() {
        this.healDamage = new GuiSlider2(4, 4, 4, 10, String.format("Heal: %d", this.tileEnt.healDamage), (float) (this.tileEnt.healDamage + 40) / 80.0F);
        if (this.tileEnt.healDamage < 0) {
            this.healDamage.message = String.format("Damage: %d", -this.tileEnt.healDamage);
        }

        this.buttons.add(this.healDamage);
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.tileEnt.healDamage = (int) ((double) this.healDamage.sliderValue * 80.0D - 40.0D);
        if (this.tileEnt.healDamage < 0) {
            this.healDamage.message = String.format("Damage: %d", -this.tileEnt.healDamage);
        } else {
            this.healDamage.message = String.format("Heal: %d", this.tileEnt.healDamage);
        }

        super.render(var1, var2, var3);
        this.tileEnt.level.getChunkAt(this.tileEnt.x, this.tileEnt.z).markUnsaved();
    }

    public static void showUI(Level var0, AC_TileEntityHealDamage var1) {
        Minecraft.instance.setScreen(new AC_GuiHealDamage(var1));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
