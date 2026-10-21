package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.gui.components.BoundSlider;
import dev.adventurecraft.awakening.primitives.Property;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiLightBulb extends Screen {
    private final int blockX, blockY, blockZ;
    private final Level world;
    private BoundSlider<Integer> lightSlider;

    public AC_GuiLightBulb(Level world, int x, int y, int z) {
        this.world = world;
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
    }

    @Override public void init() {
        var value = Property.of(() -> this.world.getData(this.blockX, this.blockY, this.blockZ),
            next -> this.world.setTileAndData(this.blockX, this.blockY, this.blockZ, AC_Blocks.lightBulb.id, next));
        this.lightSlider = new BoundSlider<>(4, 4, 4, value, light -> light / 15.0F,
            position -> (int) (position * 15.0F + 0.5F), light -> String.format("Light Value: %d", light));
        this.buttons.add(this.lightSlider);
    }

    @Override public void tick() { this.lightSlider.refresh(); }
    @Override protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        BoundSlider.drag(this.buttons, mouseX);
    }
    @Override public void render(int mouseX, int mouseY, float delta) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawCenteredString(this.font, "Light Bulb", this.width / 2, 34, 0xFFFFFF);
        int helpWidth = Math.max(1, Math.min(300, this.width - 16));
        int y = this.drawHelp("An invisible, non-solid light source.", 52, helpWidth);
        y = this.drawHelp("Light Value changes its own emission only. It does not block other lights.", y, helpWidth);
        this.drawHelp("Use solid blocks for visible walls. Clip Blocks block ray-traced light but stay invisible.", y, helpWidth);
        super.render(mouseX, mouseY, delta);
    }

    private int drawHelp(String message, int y, int maxWidth) {
        String line = "";
        for (String word : message.split(" ")) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && this.font.width(next) > maxWidth) {
                this.drawCenteredString(this.font, line, this.width / 2, y, 0xD0D0D0);
                y += 12;
                line = word;
            } else {
                line = next;
            }
        }
        if (!line.isEmpty()) {
            this.drawCenteredString(this.font, line, this.width / 2, y, 0xD0D0D0);
            y += 12;
        }
        return y + 4;
    }

    public static void showUI(Level world, int x, int y, int z) { Minecraft.instance.setScreen(new AC_GuiLightBulb(world, x, y, z)); }
    @Override public boolean isPauseScreen() { return false; }
}
