package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.world.World;

public class AC_GuiHealDamage extends Screen {
    private AC_TileEntityHealDamage tileEnt;
    GuiSlider2 healDamage;

    public AC_GuiHealDamage(AC_TileEntityHealDamage var1) {
        this.tileEnt = var1;
    }

    public void tick() {
    }

    public void initVanillaScreen() {
        this.healDamage = new GuiSlider2(4, 4, 4, 10, String.format("Heal: %d", this.tileEnt.healDamage), (float) (this.tileEnt.healDamage + 40) / 80.0F);
        if (this.tileEnt.healDamage < 0) {
            this.healDamage.text = String.format("Damage: %d", -this.tileEnt.healDamage);
        }

        this.buttons.add(this.healDamage);
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.tileEnt.healDamage = (int) ((double) this.healDamage.sliderValue * 80.0D - 40.0D);
        if (this.tileEnt.healDamage < 0) {
            this.healDamage.text = String.format("Damage: %d", -this.tileEnt.healDamage);
        } else {
            this.healDamage.text = String.format("Heal: %d", this.tileEnt.healDamage);
        }

        super.render(var1, var2, var3);
        this.tileEnt.world.getChunk(this.tileEnt.x, this.tileEnt.z).method_885();
    }

    public static void showUI(World var0, AC_TileEntityHealDamage var1) {
        Minecraft.instance.openScreen(new AC_GuiHealDamage(var1));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
