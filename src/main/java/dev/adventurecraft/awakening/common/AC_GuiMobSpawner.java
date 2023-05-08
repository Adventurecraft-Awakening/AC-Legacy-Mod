package dev.adventurecraft.awakening.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import dev.adventurecraft.awakening.script.EntityDescriptions;

public class AC_GuiMobSpawner extends Screen {

    private AC_TileEntityMobSpawner mobSpawner;
    private static ArrayList<String> entityTypes = new ArrayList<>();
    private GuiSlider2 spawnCountSlider;
    private GuiSlider2 respawnSlider;
    private String newSliderString;
    private int displayScreen;
    ButtonWidget setOnTrigger;
    ButtonWidget setOnDetrigger;
    ButtonWidget setOnUpdate;
    int selectedID;

    public AC_GuiMobSpawner(AC_TileEntityMobSpawner var1) {
        this.mobSpawner = var1;
    }

    @Override
    public void tick() {
    }

    @Override
    public void initVanillaScreen() {
        this.buttons.clear();
        this.spawnCountSlider = new GuiSlider2(50, 4, 44, 10, String.format("Spawn Count: %d", this.mobSpawner.spawnNumber), (float) this.mobSpawner.spawnNumber / 15.0F);
        this.respawnSlider = new GuiSlider2(51, this.width / 2, 44, 10, String.format("Respawn Delay: %.1f", (float) this.mobSpawner.respawnDelay / 20.0F), (float) this.mobSpawner.respawnDelay / 12000.0F);
        this.spawnCountSlider.width = 200;
        this.respawnSlider.width = 200;
        this.buttons.add(this.spawnCountSlider);
        this.buttons.add(this.respawnSlider);
        ButtonWidget var1 = new ButtonWidget(53, this.width / 2, 4, 200, 18, "Spawn On Trigger");
        if (!this.mobSpawner.spawnOnTrigger) {
            if (this.mobSpawner.spawnOnDetrigger) {
                var1.text = "Spawn on Detrigger";
            } else {
                var1.text = "Spawn on Timer";
            }
        }

        this.buttons.add(var1);
        var1 = new ButtonWidget(55, this.width / 2, 24, 200, 18, String.format("Spawn: (%d, %d, %d), (%d, %d, %d)", this.mobSpawner.minSpawnVec.x, this.mobSpawner.minSpawnVec.y, this.mobSpawner.minSpawnVec.z, this.mobSpawner.maxSpawnVec.x, this.mobSpawner.maxSpawnVec.y, this.mobSpawner.maxSpawnVec.z));
        this.buttons.add(var1);
        int var2 = (this.width - 16) / 4;
        this.buttons.add(new ButtonWidget(57, 4, 64, var2, 18, "Select Spawn"));
        this.buttons.add(new ButtonWidget(58, 4 + 4 + var2, 64, var2, 18, "Select Drops"));
        this.buttons.add(new ButtonWidget(59, 4 + 2 * (4 + var2), 64, var2, 18, "Select Triggers"));
        this.buttons.add(new ButtonWidget(60, 4 + 3 * (4 + var2), 64, var2, 18, "Select Scripts"));
        int var3;
        String var4;
        if (this.displayScreen == 0) {
            var3 = 0;
            var4 = "Spawn Item/Block: None";
            if (Item.byId[this.mobSpawner.spawnID] != null) {
                var4 = String.format("Spawn Item/Block: %s", Item.byId[this.mobSpawner.spawnID].getTranslationKey());
            }

            this.buttons.add(new ButtonWidget(56, 2, 84, 200, 14, var4));

            for (String var6 : entityTypes) {
                this.buttons.add(new ButtonWidget(var3, 2 + (var2 + 4) * (var3 % 4), (var3 / 4 + 1) * 14 + 84, var2, 13, var6));
                ++var3;
            }

            for (String var6 : EntityDescriptions.getDescriptions()) {
                this.buttons.add(new ButtonWidget(var3, 2 + (var2 + 4) * (var3 % 4), (var3 / 4 + 1) * 14 + 84, var2, 13, var6 + " (Scripted)"));
                ++var3;
            }
        } else if (this.displayScreen == 1) {
            var1 = new ButtonWidget(52, 4, 84, 200, 18, "Drop Nothing");
            if (this.mobSpawner.dropItem > 0) {
                if (this.mobSpawner.dropItem == AC_Items.doorKey.id) {
                    var1.text = "Drop key";
                } else if (this.mobSpawner.dropItem == AC_Items.heartContainer.id) {
                    var1.text = "Drop heart container";
                } else if (this.mobSpawner.dropItem == AC_Items.bossKey.id) {
                    var1.text = "Drop boss key";
                }
            }

            this.buttons.add(var1);
        } else if (this.displayScreen == 2) {
            for (var3 = 0; var3 < 8; ++var3) {
                var4 = ": Not Set";
                if (this.mobSpawner.isTriggerSet(var3)) {
                    var4 = ": Set";
                }

                if (var3 < 4) {
                    var1 = new ButtonWidget(70 + var3, 4, 84 + var3 * 19, 200, 18, "Trigger ".concat(Integer.toString(var3)).concat(var4));
                } else {
                    var1 = new ButtonWidget(70 + var3, this.width / 2, 84 + (var3 - 4) * 19, 200, 18, "OnDeath Trigger ".concat(Integer.toString(var3)).concat(var4));
                }

                this.buttons.add(var1);
            }
        } else if (this.displayScreen == 3) {
            this.selectedID = 0;
            this.setOnTrigger = new ButtonWidget(61, 4, 84, "OnSpawn (selected): " + this.mobSpawner.onTriggerScriptFile);
            this.setOnDetrigger = new ButtonWidget(62, this.width / 2, 84, "OnDeath: " + this.mobSpawner.onDetriggerScriptFile);
            this.setOnUpdate = new ButtonWidget(63, 4, 104, "OnUpdate: " + this.mobSpawner.onUpdateScriptFile);
            this.buttons.add(this.setOnTrigger);
            this.buttons.add(this.setOnDetrigger);
            this.buttons.add(this.setOnUpdate);
            this.buttons.add(new ButtonWidget(64, this.width / 2, 104, 200, 20, "Reload Scripts"));
            this.buttons.add(new ButtonWidget(65, 4, 124, (this.width - 12) / 3, 18, "None"));
            String[] var9 = ((ExWorld) this.client.world).getScriptFiles();
            if (var9 != null) {
                int var11 = 1;
                String[] var10 = var9;
                int var12 = var9.length;

                for (int var7 = 0; var7 < var12; ++var7) {
                    String var8 = var10[var7];
                    var1 = new ButtonWidget(119 + var11, 4 + var11 % 3 * (this.width - 8) / 3, 124 + var11 / 3 * 20, (this.width - 12) / 3, 18, var8);
                    this.buttons.add(var1);
                    ++var11;
                }
            }
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id < 50) {
            this.mobSpawner.entityID = var1.text;
        } else if (var1.id == 52) {
            if (this.mobSpawner.dropItem == AC_Items.doorKey.id) {
                var1.text = "Drop heart container";
                this.mobSpawner.dropItem = AC_Items.heartContainer.id;
            } else if (this.mobSpawner.dropItem == AC_Items.heartContainer.id) {
                var1.text = "Drop boss key";
                this.mobSpawner.dropItem = AC_Items.bossKey.id;
            } else if (this.mobSpawner.dropItem == AC_Items.bossKey.id) {
                var1.text = "Drop nothing";
                this.mobSpawner.dropItem = 0;
            } else {
                var1.text = "Drop key";
                this.mobSpawner.dropItem = AC_Items.doorKey.id;
            }
        } else if (var1.id == 53) {
            if (this.mobSpawner.spawnOnTrigger) {
                this.mobSpawner.spawnOnTrigger = false;
                this.mobSpawner.spawnOnDetrigger = true;
                var1.text = "Spawn On Detrigger";
            } else if (this.mobSpawner.spawnOnDetrigger) {
                this.mobSpawner.spawnOnDetrigger = false;
                var1.text = "Spawn On Timer";
            } else {
                this.mobSpawner.spawnOnTrigger = true;
                var1.text = "Spawn on Trigger";
            }
        } else if (var1.id == 55) {
            this.mobSpawner.setSpawnVec();
            var1.text = String.format("Spawn: (%d, %d, %d), (%d, %d, %d)", this.mobSpawner.minSpawnVec.x, this.mobSpawner.minSpawnVec.y, this.mobSpawner.minSpawnVec.z, this.mobSpawner.maxSpawnVec.x, this.mobSpawner.maxSpawnVec.y, this.mobSpawner.maxSpawnVec.z);
        } else if (var1.id == 56) {
            if (this.client.player.inventory.getHeldItem() != null) {
                this.mobSpawner.spawnID = this.client.player.inventory.getHeldItem().itemId;
                this.mobSpawner.spawnMeta = this.client.player.inventory.getHeldItem().getMeta();
            } else {
                this.mobSpawner.spawnID = 0;
                this.mobSpawner.spawnMeta = 0;
            }

            String var2 = "Spawn Item/Block: None";
            if (Item.byId[this.mobSpawner.spawnID] != null) {
                var2 = String.format("Spawn Item/Block: %s", Item.byId[this.mobSpawner.spawnID].getTranslationKey());
            }

            var1.text = var2;
        } else if (var1.id >= 57 && var1.id <= 60) {
            this.displayScreen = var1.id - 57;
            this.initVanillaScreen();
        } else if (var1.id >= 61 && var1.id <= 63) {
            this.selectedID = var1.id - 61;
            this.resetNames();
        } else if (var1.id == 64) {
            ((ExWorld) this.mobSpawner.world).getScriptHandler().loadScripts();
            this.resetNames();
        } else if (var1.id == 65) {
            this.updateScriptFile("");
            this.resetNames();
        } else if (var1.id >= 120) {
            this.updateScriptFile(var1.text);
            this.resetNames();
        } else if (var1.id >= 70 && var1.id < 120) {
            int var3 = var1.id - 70;
            if (this.mobSpawner.isTriggerSet(var3)) {
                this.mobSpawner.setCursor(var3);
                this.mobSpawner.clearTrigger(var3);
                var1.text = "Trigger ".concat(Integer.toString(var3)).concat(": Not Set");
            } else {
                this.mobSpawner.setTrigger(var3);
                var1.text = "Trigger ".concat(Integer.toString(var3)).concat(": Set");
            }
        }
    }

    @Override
    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawTextWithShadow(this.textRenderer, String.format("Entity Spawn: %s", this.mobSpawner.entityID), 4, 4, 14737632);
        this.drawTextWithShadow(this.textRenderer, String.format("Entities Alive: %d", this.mobSpawner.getNumAlive()), 4, 14, 14737632);
        this.drawTextWithShadow(this.textRenderer, String.format("Respawn In: %.1fs", (float) this.mobSpawner.delay / 20.0F), 4, 24, 14737632);
        if (this.mobSpawner.hasDroppedItem) {
            this.drawTextWithShadow(this.textRenderer, "Has Dropped An Item", 4, 34, 14737632);
        } else {
            this.drawTextWithShadow(this.textRenderer, "Has Not Dropped An Item", 4, 34, 14737632);
        }

        this.mobSpawner.spawnNumber = (int) (this.spawnCountSlider.sliderValue * 15.0F + 0.5F);
        this.spawnCountSlider.text = String.format("Spawn Count: %d", this.mobSpawner.spawnNumber);
        this.mobSpawner.respawnDelay = (int) (this.respawnSlider.sliderValue * 12000.0F + 0.5F);
        this.respawnSlider.text = String.format("Respawn Delay: %.1fs", (float) this.mobSpawner.respawnDelay / 20.0F);
        super.render(var1, var2, var3);
        this.mobSpawner.world.getChunk(this.mobSpawner.x, this.mobSpawner.z).method_885();
    }

    public static void showUI(AC_TileEntityMobSpawner var0) {
        Minecraft.instance.openScreen(new AC_GuiMobSpawner(var0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void resetNames() {
        this.setOnTrigger.text = "OnSpawn: " + this.mobSpawner.onTriggerScriptFile;
        this.setOnDetrigger.text = "OnDeath: " + this.mobSpawner.onDetriggerScriptFile;
        this.setOnUpdate.text = "OnUpdate: " + this.mobSpawner.onUpdateScriptFile;
        if (this.selectedID == 0) {
            this.setOnTrigger.text = "OnSpawn (selected): " + this.mobSpawner.onTriggerScriptFile;
        } else if (this.selectedID == 1) {
            this.setOnDetrigger.text = "OnDeath (selected): " + this.mobSpawner.onDetriggerScriptFile;
        } else if (this.selectedID == 2) {
            this.setOnUpdate.text = "OnUpdate (selected): " + this.mobSpawner.onUpdateScriptFile;
        }
    }

    private void updateScriptFile(String var1) {
        if (this.selectedID == 0) {
            this.mobSpawner.onTriggerScriptFile = var1;
        } else if (this.selectedID == 1) {
            this.mobSpawner.onDetriggerScriptFile = var1;
        } else if (this.selectedID == 2) {
            this.mobSpawner.onUpdateScriptFile = var1;
        }
    }

    static {
        entityTypes.add("Bat");
        entityTypes.add("Boat");
        entityTypes.add("Chicken");
        entityTypes.add("Cow");
        entityTypes.add("Creeper");
        entityTypes.add("Falling Block");
        entityTypes.add("Ghast");
        entityTypes.add("Giant");
        entityTypes.add("Item");
        entityTypes.add("Minecart");
        entityTypes.add("Minecart Chest");
        entityTypes.add("Minecart Furnace");
        entityTypes.add("Pig Zombie");
        entityTypes.add("Pig");
        entityTypes.add("Primed Tnt");
        entityTypes.add("Rat");
        entityTypes.add("Sheep");
        entityTypes.add("Skeleton");
        entityTypes.add("Skeleton Boss");
        entityTypes.add("Skeleton Rifle");
        entityTypes.add("Skeleton Shotgun");
        entityTypes.add("Skeleton Sword");
        entityTypes.add("Slime");
        entityTypes.add("Slime Size: 1");
        entityTypes.add("Slime Size: 2");
        entityTypes.add("Slime Size: 4");
        entityTypes.add("Slime Size: 8");
        entityTypes.add("Slime Size: 16");
        entityTypes.add("Squid");
        entityTypes.add("Spider");
        entityTypes.add("Spider Skeleton");
        entityTypes.add("Spider Skeleton Sword");
        entityTypes.add("Wolf");
        entityTypes.add("Wolf (Angry)");
        entityTypes.add("Wolf (Tame)");
        entityTypes.add("Zombie");
        entityTypes.add("ZombiePistol");
    }
}
