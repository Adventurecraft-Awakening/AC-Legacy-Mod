package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.gui.FilePickerWidget;
import dev.adventurecraft.awakening.common.AC_JScriptHandler;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.layout.*;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMobSpawner;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.common.GuiSlider2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.script.EntityDescriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AC_GuiMobSpawner extends Screen {

    private AC_TileEntityMobSpawner mobSpawner;
    private static ArrayList<String> entityTypes = new ArrayList<>();
    private GuiSlider2 spawnCountSlider;
    private GuiSlider2 respawnSlider;
    private int displayScreen;
    Button setOnTrigger;
    Button setOnDetrigger;
    Button setOnUpdate;
    int selectedID;

    FilePickerWidget scriptWidget;
    int selectedScriptId;

    public AC_GuiMobSpawner(AC_TileEntityMobSpawner entity) {
        this.mobSpawner = entity;

        // create dummy picker to avoid null-checks
        this.scriptWidget = new FilePickerWidget(this, IntRect.zero, 0);
    }

    @Override
    public void init() {
        var buttons = (List<Button>) this.buttons;
        buttons.clear();

        this.spawnCountSlider = new GuiSlider2(
            50,
            4,
            44,
            10,
            String.format("Spawn Count: %d", this.mobSpawner.spawnNumber),
            (float) this.mobSpawner.spawnNumber / 15.0F
        );
        this.spawnCountSlider.width = 200;
        buttons.add(this.spawnCountSlider);

        this.respawnSlider = new GuiSlider2(
            51,
            this.width / 2,
            44,
            10,
            String.format("Respawn Delay: %.1f", (float) this.mobSpawner.respawnDelay / 20.0F),
            (float) this.mobSpawner.respawnDelay / 12000.0F
        );
        this.respawnSlider.width = 200;
        buttons.add(this.respawnSlider);

        var button53 = new Button(53, this.width / 2, 4, 200, 18, "Spawn On Trigger");
        if (!this.mobSpawner.spawnOnTrigger) {
            if (this.mobSpawner.spawnOnDetrigger) {
                button53.message = "Spawn on Detrigger";
            }
            else {
                button53.message = "Spawn on Timer";
            }
        }
        buttons.add(button53);

        var button55 = new Button(
            55,
            this.width / 2,
            24,
            200,
            18,
            formatSpawnVec(this.mobSpawner.minSpawnVec, this.mobSpawner.maxSpawnVec)
        );
        buttons.add(button55);

        int width = (this.width - 16) / 4;
        buttons.add(new Button(57, 4, 64, width, 18, "Select Spawn"));
        buttons.add(new Button(58, 4 + 4 + width, 64, width, 18, "Select Drops"));
        buttons.add(new Button(59, 4 + 2 * (4 + width), 64, width, 18, "Select Triggers"));
        buttons.add(new Button(60, 4 + 3 * (4 + width), 64, width, 18, "Select Scripts"));

        int scriptIdOffset = 120;

        if (this.displayScreen == 0) {
            String name = "Spawn Item/Block: None";
            if (Item.items[this.mobSpawner.spawnID] != null) {
                name = String.format("Spawn Item/Block: %s", Item.items[this.mobSpawner.spawnID].getDescriptionId());
            }

            buttons.add(new Button(56, 2, 84, 200, 14, name));

            int id = 0;
            for (String type : entityTypes) {
                int x = 2 + (width + 4) * (id % 4);
                int y = (id / 4 + 1) * 14 + 84;
                buttons.add(new Button(id + scriptIdOffset, x, y, width, 13, type));
                ++id;
            }

            for (String desc : EntityDescriptions.getDescriptions()) {
                int x = 2 + (width + 4) * (id % 4);
                int y = (id / 4 + 1) * 14 + 84;
                buttons.add(new Button(id + scriptIdOffset, x, y, width, 13, desc + " (Scripted)"));
                ++id;
            }
        }
        else if (this.displayScreen == 1) {
            var button52 = new Button(52, 4, 84, 200, 18, "Drop Nothing");
            if (this.mobSpawner.dropItem > 0) {
                if (this.mobSpawner.dropItem == AC_Items.doorKey.id) {
                    button52.message = "Drop key";
                }
                else if (this.mobSpawner.dropItem == AC_Items.heartContainer.id) {
                    button52.message = "Drop heart container";
                }
                else if (this.mobSpawner.dropItem == AC_Items.bossKey.id) {
                    button52.message = "Drop boss key";
                }
            }
            buttons.add(button52);
        }
        else if (this.displayScreen == 2) {
            for (int triggerId = 0; triggerId < 8; ++triggerId) {
                String suffix = this.mobSpawner.isTriggerSet(triggerId) ? ": Set" : ": Not Set";

                Button button70;
                if (triggerId < 4) {
                    button70 = new Button(
                        70 + triggerId,
                        4,
                        84 + triggerId * 19,
                        200,
                        18,
                        "Trigger ".concat(Integer.toString(triggerId)).concat(suffix)
                    );
                }
                else {
                    button70 = new Button(
                        70 + triggerId,
                        this.width / 2,
                        84 + (triggerId - 4) * 19,
                        200,
                        18,
                        "OnDeath Trigger ".concat(Integer.toString(triggerId)).concat(suffix)
                    );
                }
                buttons.add(button70);
            }
        }
        else if (this.displayScreen == 3) {
            this.selectedID = 0;
            int subWidth = this.width / 2 - 5;
            this.setOnTrigger = new Button(
                61,
                4,
                84,
                subWidth,
                20,
                "OnSpawn (selected): " + this.mobSpawner.onTriggerScriptFile
            );
            this.setOnDetrigger = new Button(
                62,
                this.width / 2 - 1,
                84,
                subWidth,
                20,
                "OnDeath: " + this.mobSpawner.onDetriggerScriptFile
            );
            this.setOnUpdate = new Button(63, 4, 104, subWidth, 20, "OnUpdate: " + this.mobSpawner.onUpdateScriptFile);
            buttons.add(this.setOnTrigger);
            buttons.add(this.setOnDetrigger);
            buttons.add(this.setOnUpdate);
            buttons.add(new Button(64, this.width / 2 - 1, 104, subWidth, 20, "Reload Scripts"));
            buttons.add(new Button(65, 4, 124, (this.width - 12) / 3, 18, "None"));

            int scriptY = 124;
            int scriptHeight = this.height - scriptY;
            this.scriptWidget = new FilePickerWidget(
                this,
                new IntRect(this.width / 2, scriptY, this.width / 2, scriptHeight),
                16
            );
            this.scriptWidget.setLayoutBorder(new IntBorder(0, 0, 20, 0));
            this.selectedScriptId = -1;

            this.reloadScriptList(((ExWorld) this.minecraft.level).getScriptHandler());
        }
    }

    @Override
    protected void buttonClicked(Button button) {
        if (this.scriptWidget.buttonClicked(button)) {
            return;
        }

        if (button.id == 52) {
            if (this.mobSpawner.dropItem == AC_Items.doorKey.id) {
                button.message = "Drop heart container";
                this.mobSpawner.dropItem = AC_Items.heartContainer.id;
            }
            else if (this.mobSpawner.dropItem == AC_Items.heartContainer.id) {
                button.message = "Drop boss key";
                this.mobSpawner.dropItem = AC_Items.bossKey.id;
            }
            else if (this.mobSpawner.dropItem == AC_Items.bossKey.id) {
                button.message = "Drop nothing";
                this.mobSpawner.dropItem = 0;
            }
            else {
                button.message = "Drop key";
                this.mobSpawner.dropItem = AC_Items.doorKey.id;
            }
        }
        else if (button.id == 53) {
            if (this.mobSpawner.spawnOnTrigger) {
                this.mobSpawner.spawnOnTrigger = false;
                this.mobSpawner.spawnOnDetrigger = true;
                button.message = "Spawn On Detrigger";
            }
            else if (this.mobSpawner.spawnOnDetrigger) {
                this.mobSpawner.spawnOnDetrigger = false;
                button.message = "Spawn On Timer";
            }
            else {
                this.mobSpawner.spawnOnTrigger = true;
                button.message = "Spawn on Trigger";
            }
        }
        else if (button.id == 55) {
            this.mobSpawner.setSpawnVec();
            button.message = formatSpawnVec(this.mobSpawner.minSpawnVec, this.mobSpawner.maxSpawnVec);
        }
        else if (button.id == 56) {
            if (this.minecraft.player.inventory.getSelected() != null) {
                this.mobSpawner.spawnID = this.minecraft.player.inventory.getSelected().id;
                this.mobSpawner.spawnMeta = this.minecraft.player.inventory.getSelected().getAuxValue();
            }
            else {
                this.mobSpawner.spawnID = 0;
                this.mobSpawner.spawnMeta = 0;
            }

            String txt = "Spawn Item/Block: None";
            if (Item.items[this.mobSpawner.spawnID] != null) {
                txt = String.format("Spawn Item/Block: %s", Item.items[this.mobSpawner.spawnID].getDescriptionId());
            }
            button.message = txt;
        }
        else if (button.id >= 57 && button.id <= 60) {
            this.displayScreen = button.id - 57;
            this.init();
        }
        else if (button.id >= 61 && button.id <= 63) {
            if (this.displayScreen == 3) {
                this.selectedID = button.id - 61;
                this.resetNames();
            }
        }
        else if (button.id == 64) {
            if (this.displayScreen == 3) {
                var scriptHandler = ((ExWorld) this.mobSpawner.level).getScriptHandler();
                scriptHandler.loadScripts();
                this.reloadScriptList(scriptHandler);
                this.resetNames();
            }
        }
        else if (button.id == 65) {
            if (this.displayScreen == 3) {
                this.updateScriptFile("");
                this.resetNames();
            }
        }
        else if (button.id >= 120) {
            if (this.displayScreen == 0) {
                this.mobSpawner.entityID = button.message;
            }
        }
        else if (button.id >= 70 && button.id < 120) {
            if (this.displayScreen == 2) {
                int triggerId = button.id - 70;
                if (this.mobSpawner.isTriggerSet(triggerId)) {
                    this.mobSpawner.setCursor(triggerId);
                    this.mobSpawner.clearTrigger(triggerId);
                    button.message = "Trigger ".concat(Integer.toString(triggerId)).concat(": Not Set");
                }
                else {
                    this.mobSpawner.setTrigger(triggerId);
                    button.message = "Trigger ".concat(Integer.toString(triggerId)).concat(": Set");
                }
            }
        }
    }

    @Override
    public void mouseEvent() {
        super.mouseEvent();

        if (this.displayScreen == 3) {
            this.scriptWidget.onMouseEvent();
        }
    }

    protected @Override void keyPressed(char codepoint, int key) {
        super.keyPressed(codepoint, key);

        this.scriptWidget.charTyped(codepoint, key);
    }

    protected @Override void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        this.scriptWidget.clicked(new IntPoint(mouseX, mouseY), button);
    }

    public @Override void tick() {
        this.scriptWidget.tick();
    }

    @Override
    public void render(int mouseX, int mouseY, float tickTime) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawString(this.font, String.format("Entity Spawn: %s", this.mobSpawner.entityID), 4, 4, 14737632);
        this.drawString(this.font, String.format("Entities Alive: %d", this.mobSpawner.getNumAlive()), 4, 14, 14737632);
        this.drawString(
            this.font,
            String.format("Respawn In: %.1fs", (float) this.mobSpawner.delay / 20.0F),
            4,
            24,
            14737632
        );
        if (this.mobSpawner.hasDroppedItem) {
            this.drawString(this.font, "Has Dropped An Item", 4, 34, 14737632);
        }
        else {
            this.drawString(this.font, "Has Not Dropped An Item", 4, 34, 14737632);
        }

        this.mobSpawner.spawnNumber = (int) (this.spawnCountSlider.sliderValue * 15.0F + 0.5F);
        this.spawnCountSlider.message = String.format("Spawn Count: %d", this.mobSpawner.spawnNumber);

        this.mobSpawner.respawnDelay = (int) (this.respawnSlider.sliderValue * 12000.0F + 0.5F);
        this.respawnSlider.message = String.format(
            "Respawn Delay: %.1fs",
            (float) this.mobSpawner.respawnDelay / 20.0F
        );

        if (this.displayScreen == 3) {
            this.scriptWidget.render(new IntPoint(mouseX, mouseY), tickTime);

            int currentScriptId = this.scriptWidget.getSelectedIndex();
            if (this.selectedScriptId != currentScriptId) {
                this.selectedScriptId = currentScriptId;

                var entry = this.scriptWidget.getSelectedEntry();
                this.updateScriptFile(entry == null ? "" : entry.value().getFileName().toString());
                this.resetNames();
            }
        }

        super.render(mouseX, mouseY, tickTime);
    }

    @Override
    public void removed() {
        super.removed();

        this.mobSpawner.setChanged();
    }

    public static void showUI(AC_TileEntityMobSpawner entity) {
        Minecraft.instance.setScreen(new AC_GuiMobSpawner(entity));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void reloadScriptList(AC_JScriptHandler scriptHandler) {
        List<FilePickerWidget.Entry> list = this.scriptWidget.getStorageList();
        list.clear();

        Path[] files = scriptHandler.getFiles();
        Arrays.stream(files).map(FilePickerWidget.Entry::new).collect(Collectors.toCollection(() -> list));

        this.scriptWidget.refresh();
    }

    private void resetNames() {
        this.setOnTrigger.message = "OnSpawn: " + this.mobSpawner.onTriggerScriptFile;
        this.setOnDetrigger.message = "OnDeath: " + this.mobSpawner.onDetriggerScriptFile;
        this.setOnUpdate.message = "OnUpdate: " + this.mobSpawner.onUpdateScriptFile;

        if (this.selectedID == 0) {
            this.setOnTrigger.message = "OnSpawn (selected): " + this.mobSpawner.onTriggerScriptFile;
        }
        else if (this.selectedID == 1) {
            this.setOnDetrigger.message = "OnDeath (selected): " + this.mobSpawner.onDetriggerScriptFile;
        }
        else if (this.selectedID == 2) {
            this.setOnUpdate.message = "OnUpdate (selected): " + this.mobSpawner.onUpdateScriptFile;
        }
    }

    private void updateScriptFile(String var1) {
        if (this.selectedID == 0) {
            this.mobSpawner.onTriggerScriptFile = var1;
        }
        else if (this.selectedID == 1) {
            this.mobSpawner.onDetriggerScriptFile = var1;
        }
        else if (this.selectedID == 2) {
            this.mobSpawner.onUpdateScriptFile = var1;
        }
    }

    private static String formatSpawnVec(Coord min, Coord max) {
        return String.format("Spawn: (%d, %d, %d), (%d, %d, %d)", min.x, min.y, min.z, max.x, max.y, max.z);
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
