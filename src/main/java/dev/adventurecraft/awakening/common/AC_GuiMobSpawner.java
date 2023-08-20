package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.script.EntityDescriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class AC_GuiMobSpawner extends Screen {

    private AC_TileEntityMobSpawner mobSpawner;
    private static ArrayList<String> entityTypes = new ArrayList<>();
    private GuiSlider2 spawnCountSlider;
    private GuiSlider2 respawnSlider;
    private int displayScreen;
    ButtonWidget setOnTrigger;
    ButtonWidget setOnDetrigger;
    ButtonWidget setOnUpdate;
    int selectedID;

    public AC_GuiMobSpawner(AC_TileEntityMobSpawner entity) {
        this.mobSpawner = entity;
    }

    @Override
    public void tick() {
    }

    @Override
    public void initVanillaScreen() {
        var buttons = (List<ButtonWidget>) this.buttons;
        buttons.clear();

        this.spawnCountSlider = new GuiSlider2(
            50, 4, 44, 10,
            String.format("Spawn Count: %d", this.mobSpawner.spawnNumber),
            (float) this.mobSpawner.spawnNumber / 15.0F);
        this.spawnCountSlider.width = 200;
        buttons.add(this.spawnCountSlider);

        this.respawnSlider = new GuiSlider2(
            51, this.width / 2, 44, 10,
            String.format("Respawn Delay: %.1f", (float) this.mobSpawner.respawnDelay / 20.0F),
            (float) this.mobSpawner.respawnDelay / 12000.0F);
        this.respawnSlider.width = 200;
        buttons.add(this.respawnSlider);

        var button53 = new ButtonWidget(53, this.width / 2, 4, 200, 18, "Spawn On Trigger");
        if (!this.mobSpawner.spawnOnTrigger) {
            if (this.mobSpawner.spawnOnDetrigger) {
                button53.text = "Spawn on Detrigger";
            } else {
                button53.text = "Spawn on Timer";
            }
        }
        buttons.add(button53);

        var button55 = new ButtonWidget(
            55, this.width / 2, 24, 200, 18,
            String.format("Spawn: (%d, %d, %d), (%d, %d, %d)",
                this.mobSpawner.minSpawnVec.x, this.mobSpawner.minSpawnVec.y, this.mobSpawner.minSpawnVec.z,
                this.mobSpawner.maxSpawnVec.x, this.mobSpawner.maxSpawnVec.y, this.mobSpawner.maxSpawnVec.z));
        buttons.add(button55);

        int width = (this.width - 16) / 4;
        buttons.add(new ButtonWidget(57, 4, 64, width, 18, "Select Spawn"));
        buttons.add(new ButtonWidget(58, 4 + 4 + width, 64, width, 18, "Select Drops"));
        buttons.add(new ButtonWidget(59, 4 + 2 * (4 + width), 64, width, 18, "Select Triggers"));
        buttons.add(new ButtonWidget(60, 4 + 3 * (4 + width), 64, width, 18, "Select Scripts"));

        int scriptIdOffset = 120;

        if (this.displayScreen == 0) {
            String name = "Spawn Item/Block: None";
            if (Item.byId[this.mobSpawner.spawnID] != null) {
                name = String.format("Spawn Item/Block: %s", Item.byId[this.mobSpawner.spawnID].getTranslationKey());
            }

            buttons.add(new ButtonWidget(56, 2, 84, 200, 14, name));

            int id = 0;
            for (String type : entityTypes) {
                int x = 2 + (width + 4) * (id % 4);
                int y = (id / 4 + 1) * 14 + 84;
                buttons.add(new ButtonWidget(id + scriptIdOffset, x, y, width, 13, type));
                ++id;
            }

            for (String desc : EntityDescriptions.getDescriptions()) {
                int x = 2 + (width + 4) * (id % 4);
                int y = (id / 4 + 1) * 14 + 84;
                buttons.add(new ButtonWidget(id + scriptIdOffset, x, y, width, 13, desc + " (Scripted)"));
                ++id;
            }
        } else if (this.displayScreen == 1) {
            var button52 = new ButtonWidget(52, 4, 84, 200, 18, "Drop Nothing");
            if (this.mobSpawner.dropItem > 0) {
                if (this.mobSpawner.dropItem == AC_Items.doorKey.id) {
                    button52.text = "Drop key";
                } else if (this.mobSpawner.dropItem == AC_Items.heartContainer.id) {
                    button52.text = "Drop heart container";
                } else if (this.mobSpawner.dropItem == AC_Items.bossKey.id) {
                    button52.text = "Drop boss key";
                }
            }
            buttons.add(button52);
        } else if (this.displayScreen == 2) {
            for (int triggerId = 0; triggerId < 8; ++triggerId) {
                String suffix = this.mobSpawner.isTriggerSet(triggerId) ? ": Set" : ": Not Set";

                ButtonWidget button70;
                if (triggerId < 4) {
                    button70 = new ButtonWidget(
                        70 + triggerId, 4, 84 + triggerId * 19, 200, 18,
                        "Trigger ".concat(Integer.toString(triggerId)).concat(suffix));
                } else {
                    button70 = new ButtonWidget(
                        70 + triggerId, this.width / 2, 84 + (triggerId - 4) * 19, 200, 18,
                        "OnDeath Trigger ".concat(Integer.toString(triggerId)).concat(suffix));
                }
                buttons.add(button70);
            }
        } else if (this.displayScreen == 3) {
            this.selectedID = 0;
            int subWidth = this.width / 2 - 5;
            this.setOnTrigger = new ButtonWidget(61, 4, 84, subWidth, 20, "OnSpawn (selected): " + this.mobSpawner.onTriggerScriptFile);
            this.setOnDetrigger = new ButtonWidget(62, this.width / 2 - 1, 84, subWidth, 20, "OnDeath: " + this.mobSpawner.onDetriggerScriptFile);
            this.setOnUpdate = new ButtonWidget(63, 4, 104, subWidth, 20, "OnUpdate: " + this.mobSpawner.onUpdateScriptFile);
            buttons.add(this.setOnTrigger);
            buttons.add(this.setOnDetrigger);
            buttons.add(this.setOnUpdate);
            buttons.add(new ButtonWidget(64, this.width / 2 - 1, 104, subWidth, 20, "Reload Scripts"));
            buttons.add(new ButtonWidget(65, 4, 124, (this.width - 12) / 3, 18, "None"));

            String[] scriptFiles = ((ExWorld) this.client.world).getScriptFiles();
            if (scriptFiles != null) {
                int id = 0;
                for (String scriptFile : scriptFiles) {
                    var button = new ButtonWidget(
                        id + scriptIdOffset,
                        4 + (id + 1) % 3 * (this.width - 8) / 3,
                        124 + (id + 1) / 3 * 20,
                        (this.width - 12) / 3, 18,
                        scriptFile);
                    buttons.add(button);
                    ++id;
                }
            }
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 52) {
            if (this.mobSpawner.dropItem == AC_Items.doorKey.id) {
                button.text = "Drop heart container";
                this.mobSpawner.dropItem = AC_Items.heartContainer.id;
            } else if (this.mobSpawner.dropItem == AC_Items.heartContainer.id) {
                button.text = "Drop boss key";
                this.mobSpawner.dropItem = AC_Items.bossKey.id;
            } else if (this.mobSpawner.dropItem == AC_Items.bossKey.id) {
                button.text = "Drop nothing";
                this.mobSpawner.dropItem = 0;
            } else {
                button.text = "Drop key";
                this.mobSpawner.dropItem = AC_Items.doorKey.id;
            }
        } else if (button.id == 53) {
            if (this.mobSpawner.spawnOnTrigger) {
                this.mobSpawner.spawnOnTrigger = false;
                this.mobSpawner.spawnOnDetrigger = true;
                button.text = "Spawn On Detrigger";
            } else if (this.mobSpawner.spawnOnDetrigger) {
                this.mobSpawner.spawnOnDetrigger = false;
                button.text = "Spawn On Timer";
            } else {
                this.mobSpawner.spawnOnTrigger = true;
                button.text = "Spawn on Trigger";
            }
        } else if (button.id == 55) {
            this.mobSpawner.setSpawnVec();
            button.text = String.format("Spawn: (%d, %d, %d), (%d, %d, %d)", this.mobSpawner.minSpawnVec.x, this.mobSpawner.minSpawnVec.y, this.mobSpawner.minSpawnVec.z, this.mobSpawner.maxSpawnVec.x, this.mobSpawner.maxSpawnVec.y, this.mobSpawner.maxSpawnVec.z);
        } else if (button.id == 56) {
            if (this.client.player.inventory.getHeldItem() != null) {
                this.mobSpawner.spawnID = this.client.player.inventory.getHeldItem().itemId;
                this.mobSpawner.spawnMeta = this.client.player.inventory.getHeldItem().getMeta();
            } else {
                this.mobSpawner.spawnID = 0;
                this.mobSpawner.spawnMeta = 0;
            }

            String txt = "Spawn Item/Block: None";
            if (Item.byId[this.mobSpawner.spawnID] != null) {
                txt = String.format("Spawn Item/Block: %s", Item.byId[this.mobSpawner.spawnID].getTranslationKey());
            }
            button.text = txt;
        } else if (button.id >= 57 && button.id <= 60) {
            this.displayScreen = button.id - 57;
            this.initVanillaScreen();
        } else if (button.id >= 61 && button.id <= 63) {
            if (this.displayScreen == 3) {
                this.selectedID = button.id - 61;
                this.resetNames();
            }
        } else if (button.id == 64) {
            if (this.displayScreen == 3) {
                ((ExWorld) this.mobSpawner.world).getScriptHandler().loadScripts();
                this.resetNames();
            }
        } else if (button.id == 65) {
            if (this.displayScreen == 3) {
                this.updateScriptFile("");
                this.resetNames();
            }
        } else if (button.id >= 120) {
            if (this.displayScreen == 0) {
                this.mobSpawner.entityID = button.text;
            } else if (this.displayScreen == 3) {
                this.updateScriptFile(button.text);
                this.resetNames();
            }
        } else if (button.id >= 70 && button.id < 120) {
            if (this.displayScreen == 2) {
                int triggerId = button.id - 70;
                if (this.mobSpawner.isTriggerSet(triggerId)) {
                    this.mobSpawner.setCursor(triggerId);
                    this.mobSpawner.clearTrigger(triggerId);
                    button.text = "Trigger ".concat(Integer.toString(triggerId)).concat(": Not Set");
                } else {
                    this.mobSpawner.setTrigger(triggerId);
                    button.text = "Trigger ".concat(Integer.toString(triggerId)).concat(": Set");
                }
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
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
        super.render(mouseX, mouseY, deltaTime);
        this.mobSpawner.world.getChunk(this.mobSpawner.x, this.mobSpawner.z).method_885();
    }

    public static void showUI(AC_TileEntityMobSpawner entity) {
        Minecraft.instance.openScreen(new AC_GuiMobSpawner(entity));
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
