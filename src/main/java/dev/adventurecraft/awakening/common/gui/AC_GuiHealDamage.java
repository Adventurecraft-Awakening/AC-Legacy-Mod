package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.gui.components.BoundSlider;
import dev.adventurecraft.awakening.primitives.Property;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityHealDamage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiHealDamage extends Screen {
    private final AC_TileEntityHealDamage tileEnt;
    private BoundSlider<Integer> healDamage;

    public AC_GuiHealDamage(AC_TileEntityHealDamage entity) { this.tileEnt = entity; }

    @Override public void init() {
        var value = Property.of(() -> this.tileEnt.healDamage, next -> {
            this.tileEnt.healDamage = next;
            this.tileEnt.setChanged();
        });
        this.healDamage = new BoundSlider<>(4, 4, 4, value, amount -> (amount + 40) / 80.0F,
            position -> (int) ((double) position * 80.0D - 40.0D),
            amount -> String.format(amount < 0 ? "Damage: %d" : "Heal: %d", Math.abs(amount)));
        this.buttons.add(this.healDamage);
    }

    @Override public void tick() { this.healDamage.refresh(); }
    @Override protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        BoundSlider.drag(this.buttons, mouseX);
    }
    @Override public void render(int mouseX, int mouseY, float delta) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        super.render(mouseX, mouseY, delta);
    }
    public static void showUI(AC_TileEntityHealDamage entity) { Minecraft.instance.setScreen(new AC_GuiHealDamage(entity)); }
    @Override public boolean isPauseScreen() { return false; }
}
