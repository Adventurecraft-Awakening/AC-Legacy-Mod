package dev.adventurecraft.awakening.common.gui;

import java.io.File;
import java.util.ArrayList;

import dev.adventurecraft.awakening.common.AC_TileEntityEffect;
import dev.adventurecraft.awakening.common.GuiSlider2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiEffect extends Screen {

    private static final float MAX_PARTICLES_PER_SPAWN = 1000;
    private static final float MAX_TICKS_BETWEEN_SPAWNS = 100;

    private AC_TileEntityEffect effect;
    private GuiSlider2 ticksBetweenParticles;
    private GuiSlider2 particlesPerSpawn;
    private GuiSlider2 offsetX;
    private GuiSlider2 offsetY;
    private GuiSlider2 offsetZ;
    private GuiSlider2 randX;
    private GuiSlider2 randY;
    private GuiSlider2 randZ;
    private GuiSlider2 floatArg1;
    private GuiSlider2 floatArg2;
    private GuiSlider2 floatArg3;
    private GuiSlider2 floatRand1;
    private GuiSlider2 floatRand2;
    private GuiSlider2 floatRand3;
    private GuiSlider2 fogR;
    private GuiSlider2 fogG;
    private GuiSlider2 fogB;
    private GuiSlider2 fogStart;
    private GuiSlider2 fogEnd;
    private int page = 0;
    private static ArrayList<String> particleTypes = new ArrayList<>();

    public AC_GuiEffect(AC_TileEntityEffect var1) {
        this.effect = var1;
    }

    public void init() {
        int var1 = (this.width - 16) / 4;
        this.buttons.add(new Button(-1, 4, 0, var1, 18, "Particles"));
        this.buttons.add(new Button(-2, 4 + 4 + var1, 0, var1, 18, "Fog"));
        this.buttons.add(new Button(-3, 4 + (4 + var1) * 2, 0, var1, 18, "Overlay"));
        this.buttons.add(new Button(-4, 4 + (4 + var1) * 3, 0, var1, 18, "Replacements"));
        var1 = (this.width - 16) / 3;
        if (this.page == 0) {
            this.particlesPerSpawn = new GuiSlider2(
                200, 4, 20, 10, String.format("Particles Per Spawn: %d", this.effect.particlesPerSpawn), this.effect.particlesPerSpawn / MAX_PARTICLES_PER_SPAWN);
            this.particlesPerSpawn.width = var1;
            this.buttons.add(this.particlesPerSpawn);
            this.ticksBetweenParticles = new GuiSlider2(200, 4 + 4 + var1, 20, 10, String.format("Ticks Between: %d", this.effect.ticksBetweenParticles), (float) this.effect.ticksBetweenParticles / MAX_TICKS_BETWEEN_SPAWNS);
            this.ticksBetweenParticles.width = var1;
            this.buttons.add(this.ticksBetweenParticles);
            this.offsetX = new GuiSlider2(201, 4, 40, 10, String.format("offset X: %.2f", this.effect.offsetX), this.effect.offsetX / 8.0F);
            this.offsetX.width = var1;
            this.buttons.add(this.offsetX);
            this.offsetY = new GuiSlider2(202, 4 + 4 + var1, 40, 10, String.format("offset Y: %.2f", this.effect.offsetY), this.effect.offsetY / 8.0F);
            this.offsetY.width = var1;
            this.buttons.add(this.offsetY);
            this.offsetZ = new GuiSlider2(203, 4 + 2 * (4 + var1), 40, 10, String.format("offset Z: %.2f", this.effect.offsetZ), this.effect.offsetZ / 8.0F);
            this.offsetZ.width = var1;
            this.buttons.add(this.offsetZ);
            this.randX = new GuiSlider2(201, 4, 60, 10, String.format("Rand X: %.2f", this.effect.randX), this.effect.randX / 8.0F);
            this.randX.width = var1;
            this.buttons.add(this.randX);
            this.randY = new GuiSlider2(202, 4 + 4 + var1, 60, 10, String.format("Rand Y: %.2f", this.effect.randY), this.effect.randY / 8.0F);
            this.randY.width = var1;
            this.buttons.add(this.randY);
            this.randZ = new GuiSlider2(203, 4 + 2 * (4 + var1), 60, 10, String.format("Rand Z: %.2f", this.effect.randZ), this.effect.randZ / 8.0F);
            this.randZ.width = var1;
            this.buttons.add(this.randZ);
            this.floatArg1 = new GuiSlider2(201, 4, 80, 10, String.format("Arg 1: %.2f", this.effect.floatArg1), (this.effect.floatArg1 + 1.0F) / 2.0F);
            this.floatArg1.width = var1;
            this.buttons.add(this.floatArg1);
            this.floatArg2 = new GuiSlider2(202, 4 + 4 + var1, 80, 10, String.format("Arg 2: %.2f", this.effect.floatArg2), (this.effect.floatArg2 + 1.0F) / 2.0F);
            this.floatArg2.width = var1;
            this.buttons.add(this.floatArg2);
            this.floatArg3 = new GuiSlider2(203, 4 + 2 * (4 + var1), 80, 10, String.format("Arg 3: %.2f", this.effect.floatArg3), (this.effect.floatArg3 + 1.0F) / 2.0F);
            this.floatArg3.width = var1;
            this.buttons.add(this.floatArg3);
            this.floatRand1 = new GuiSlider2(201, 4, 100, 10, String.format("Rand Arg 1: %.2f", this.effect.floatRand1), this.effect.floatRand1);
            this.floatRand1.width = var1;
            this.buttons.add(this.floatRand1);
            this.floatRand2 = new GuiSlider2(202, 4 + 4 + var1, 100, 10, String.format("Rand Arg 2: %.2f", this.effect.floatRand2), this.effect.floatRand2);
            this.floatRand2.width = var1;
            this.buttons.add(this.floatRand2);
            this.floatRand3 = new GuiSlider2(203, 4 + 2 * (4 + var1), 100, 10, String.format("Rand Arg 3: %.2f", this.effect.floatRand3), this.effect.floatRand3);
            this.floatRand3.width = var1;
            this.buttons.add(this.floatRand3);
            this.buttons.add(new Button(0, 4, 120, var1, 18, "No Particles"));
            int var2 = 1;

            for (String var4 : particleTypes) {
                this.buttons.add(new Button(var2, 4 + var2 % 3 * (4 + var1), 120 + var2 / 3 * 20, var1, 18, var4));
                ++var2;
            }
        } else {
            Button var9;
            if (this.page == 1) {
                var9 = new Button(0, 4, 20, var1, 18, "Don\'t Change Fog Color");
                if (this.effect.changeFogColor == 1) {
                    var9.message = "Change Fog Color";
                } else if (this.effect.changeFogColor == 2) {
                    var9.message = "Revert Fog Color To Normal";
                }

                this.buttons.add(var9);
                this.fogR = new GuiSlider2(201, 4, 40, 10, String.format("Red: %.2f", this.effect.fogR), this.effect.fogR);
                this.fogR.width = var1;
                this.buttons.add(this.fogR);
                this.fogG = new GuiSlider2(202, 4 + 4 + var1, 40, 10, String.format("Green: %.2f", this.effect.fogG), this.effect.fogG);
                this.fogG.width = var1;
                this.buttons.add(this.fogG);
                this.fogB = new GuiSlider2(203, 4 + 2 * (4 + var1), 40, 10, String.format("Blue: %.2f", this.effect.fogB), this.effect.fogB);
                this.fogB.width = var1;
                this.buttons.add(this.fogB);
                var9 = new Button(1, 4, 60, var1, 18, "Don\'t Change Fog Density");
                if (this.effect.changeFogDensity == 1) {
                    var9.message = "Change Fog Density";
                } else if (this.effect.changeFogDensity == 2) {
                    var9.message = "Revert Fog Density To Normal";
                }

                this.buttons.add(var9);
                this.fogStart = new GuiSlider2(201, 4, 80, 10, String.format("Start: %.1f", this.effect.fogStart), this.effect.fogStart / 512.0F);
                this.fogStart.width = var1;
                this.buttons.add(this.fogStart);
                this.fogEnd = new GuiSlider2(202, 4 + 4 + var1, 80, 10, String.format("End: %.1f", this.effect.fogEnd), this.effect.fogEnd / 512.0F);
                this.fogEnd.width = var1;
                this.buttons.add(this.fogEnd);
            } else {
                File[] var5;
                int var6;
                int var7;
                File var8;
                int var10;
                File var11;
                if (this.page == 2) {
                    var9 = new Button(0, 4, 20, var1, 18, "Change Overlay");
                    if (!this.effect.setOverlay) {
                        var9.message = "Don\'t Change Overlay";
                    }

                    this.buttons.add(var9);
                    this.buttons.add(new Button(1, 4, 40, var1, 18, "Remove Overlay"));
                    var10 = 1;
                    var11 = new File(((ExWorld) this.effect.level).getLevelDir(), "overlays");
                    if (var11.exists() && var11.isDirectory()) {
                        var5 = var11.listFiles();
                        var6 = var5.length;

                        for (var7 = 0; var7 < var6; ++var7) {
                            var8 = var5[var7];
                            this.buttons.add(new Button(1 + var10, 4 + var10 % 3 * (4 + var1), 40 + var10 / 3 * 20, var1, 18, var8.getName()));
                            ++var10;
                        }
                    }
                } else if (this.page == 3) {
                    var9 = new Button(0, 4, 20, var1, 18, "Replace Textures");
                    if (!this.effect.replaceTextures && this.effect.revertTextures) {
                        var9.message = "Revert Replacements";
                    } else if (!this.effect.replaceTextures) {
                        var9.message = "Do Nothing";
                    }

                    this.buttons.add(var9);
                    var10 = 0;
                    var11 = new File(((ExWorld) this.effect.level).getLevelDir(), "textureReplacement");
                    if (var11.exists() && var11.isDirectory()) {
                        var5 = var11.listFiles();
                        var6 = var5.length;

                        for (var7 = 0; var7 < var6; ++var7) {
                            var8 = var5[var7];
                            this.buttons.add(new Button(1 + var10, 4 + var10 % 3 * (4 + var1), 40 + var10 / 3 * 20, var1, 18, var8.getName()));
                            ++var10;
                        }
                    }
                }
            }
        }

    }

    protected void buttonClicked(Button var1) {
        if (var1.id < 0) {
            this.page = -var1.id - 1;
            this.buttons.clear();
            this.init();
        } else {
            if (this.page == 0) {
                if (var1.id == 0) {
                    this.effect.particleType = "";
                } else if (var1.id > 0 && var1.id < 100) {
                    this.effect.particleType = particleTypes.get(var1.id - 1);
                }
            } else if (this.page == 1) {
                if (var1.id == 0) {
                    this.effect.changeFogColor = (this.effect.changeFogColor + 1) % 3;
                    if (this.effect.changeFogColor == 1) {
                        var1.message = "Change Fog Color";
                    } else if (this.effect.changeFogColor == 2) {
                        var1.message = "Revert Fog Color To Normal";
                    } else {
                        var1.message = "Don\'t Change Fog Color";
                    }
                } else if (var1.id == 1) {
                    this.effect.changeFogDensity = (this.effect.changeFogDensity + 1) % 3;
                    if (this.effect.changeFogDensity == 1) {
                        var1.message = "Change Fog Density";
                    } else if (this.effect.changeFogDensity == 2) {
                        var1.message = "Revert Fog Density To Normal";
                    } else {
                        var1.message = "Don\'t Change Fog Density";
                    }
                }
            } else if (this.page == 2) {
                if (var1.id == 0) {
                    this.effect.setOverlay = !this.effect.setOverlay;
                    if (this.effect.setOverlay) {
                        var1.message = "Change Overlay";
                    } else {
                        var1.message = "Don\'t Change Overlay";
                    }
                } else if (var1.id == 1) {
                    this.effect.overlay = "";
                } else {
                    this.effect.overlay = var1.message;
                }
            } else if (this.page == 3) {
                if (var1.id == 0) {
                    if (this.effect.replaceTextures) {
                        this.effect.replaceTextures = false;
                        this.effect.revertTextures = true;
                        var1.message = "Revert Replacements";
                    } else if (this.effect.revertTextures) {
                        this.effect.replaceTextures = false;
                        this.effect.revertTextures = false;
                        var1.message = "Do Nothing";
                    } else {
                        this.effect.replaceTextures = true;
                        this.effect.revertTextures = false;
                        var1.message = "Replace Textures";
                    }
                } else {
                    this.effect.textureReplacement = var1.message;
                }
            }

        }
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        if (this.page == 0) {
            this.effect.offsetX = this.offsetX.sliderValue * 8.0F;
            this.offsetX.message = String.format("Offset X: %.2f", this.effect.offsetX);
            this.effect.offsetY = this.offsetY.sliderValue * 8.0F;
            this.offsetY.message = String.format("Offset Y: %.2f", this.effect.offsetY);
            this.effect.offsetZ = this.offsetZ.sliderValue * 8.0F;
            this.offsetZ.message = String.format("Offset Z: %.2f", this.effect.offsetZ);
            this.effect.randX = this.randX.sliderValue * 8.0F;
            this.randX.message = String.format("Rand X: %.2f", this.effect.randX);
            this.effect.randY = this.randY.sliderValue * 8.0F;
            this.randY.message = String.format("Rand Y: %.2f", this.effect.randY);
            this.effect.randZ = this.randZ.sliderValue * 8.0F;
            this.randZ.message = String.format("Rand Z: %.2f", this.effect.randZ);
            this.effect.floatArg1 = this.floatArg1.sliderValue * 2.0F - 1.0F;
            this.floatArg1.message = String.format("Arg 1: %.2f", this.effect.floatArg1);
            this.effect.floatArg2 = this.floatArg2.sliderValue * 2.0F - 1.0F;
            this.floatArg2.message = String.format("Arg 2: %.2f", this.effect.floatArg2);
            this.effect.floatArg3 = this.floatArg3.sliderValue * 2.0F - 1.0F;
            this.floatArg3.message = String.format("Arg 3: %.2f", this.effect.floatArg3);
            this.effect.floatRand1 = this.floatRand1.sliderValue;
            this.floatRand1.message = String.format("Rand 1: %.2f", this.effect.floatRand1);
            this.effect.floatRand2 = this.floatRand2.sliderValue;
            this.floatRand2.message = String.format("Rand 2: %.2f", this.effect.floatRand2);
            this.effect.floatRand3 = this.floatRand3.sliderValue;
            this.floatRand3.message = String.format("Rand 3: %.2f", this.effect.floatRand3);
            this.effect.ticksBetweenParticles = Math.round(this.ticksBetweenParticles.sliderValue * MAX_TICKS_BETWEEN_SPAWNS);
            this.ticksBetweenParticles.message = String.format("Ticks Between: %d", this.effect.ticksBetweenParticles);
            this.effect.particlesPerSpawn = Math.round(this.particlesPerSpawn.sliderValue * MAX_PARTICLES_PER_SPAWN);
            this.particlesPerSpawn.message = String.format("Particles Per Spawn: %d", this.effect.particlesPerSpawn);
            this.font.drawShadow("Particle Type: " + this.effect.particleType, 2 * this.width / 3, 25, -1);
        } else if (this.page == 1) {
            this.effect.fogR = this.fogR.sliderValue;
            this.fogR.message = String.format("Red: %.2f", this.effect.fogR);
            this.effect.fogG = this.fogG.sliderValue;
            this.fogG.message = String.format("Green: %.2f", this.effect.fogG);
            this.effect.fogB = this.fogB.sliderValue;
            this.fogB.message = String.format("Blue: %.2f", this.effect.fogB);
            this.effect.fogStart = this.fogStart.sliderValue * 512.0F;
            this.fogStart.message = String.format("Start: %.1f", this.effect.fogStart);
            this.effect.fogEnd = this.fogEnd.sliderValue * 512.0F;
            this.fogEnd.message = String.format("End: %.1f", this.effect.fogEnd);
        } else if (this.page == 2) {
            this.font.drawShadow("Overlay: " + this.effect.overlay, this.width / 3, 25, -1);
        } else if (this.page == 3) {
            this.font.drawShadow("Replacement: " + this.effect.textureReplacement, this.width / 3, 25, -1);
        }

        super.render(var1, var2, var3);
        this.effect.level.getChunkAt(this.effect.x, this.effect.z).markUnsaved();
    }

    public static void showUI(AC_TileEntityEffect var0) {
        Minecraft.instance.setScreen(new AC_GuiEffect(var0));
    }

    public boolean isPauseScreen() {
        return false;
    }

    static {
        particleTypes.add("bubble");
        particleTypes.add("explode");
        particleTypes.add("flame");
        particleTypes.add("heart");
        particleTypes.add("largesmoke");
        particleTypes.add("lava");
        particleTypes.add("note");
        particleTypes.add("portal");
        particleTypes.add("reddust");
        particleTypes.add("slime");
        particleTypes.add("smoke");
        particleTypes.add("snowballpoof");
        particleTypes.add("splash");
    }
}
