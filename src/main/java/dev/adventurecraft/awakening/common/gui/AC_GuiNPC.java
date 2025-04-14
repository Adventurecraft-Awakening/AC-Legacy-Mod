package dev.adventurecraft.awakening.common.gui;

import java.io.File;

import dev.adventurecraft.awakening.entity.AC_EntityNPC;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityNpcPath;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiNPC extends Screen {

    private AC_EntityNPC npc;
    private EditBox npcName;
    private EditBox chatMsg;
    int selectedID = 0;
    private Button setOnCreated;
    private Button setOnUpdate;
    private Button setOnPathReached;
    private Button setOnAttacked;
    private Button setOnDeath;
    private Button setOnInteraction;
    private int page = 0;

    public AC_GuiNPC(AC_EntityNPC var1) {
        this.npc = var1;
    }

    @Override
    public void tick() {
        if (this.page == 0) {
            this.npcName.tick();
            this.chatMsg.tick();
        }
    }

    @Override
    public void init() {
        this.buttons.clear();

        final int w4 = (this.width - 16) / 4;
        this.buttons.add(new Button(-20, 4, 0, w4, 18, "Misc"));
        this.buttons.add(new Button(-21, 4 + 4 + w4, 0, w4, 18, "Script"));

        if (this.page == 0) {
            this.npcName = new EditBox(this, this.font, 4, 40, 160, 20, this.npc.npcName);
            this.npcName.active = true;
            this.npcName.setMaxLength(32);

            this.chatMsg = new EditBox(this, this.font, 4, 80, 160, 20, this.npc.chatMsg);
            this.chatMsg.active = false;
            this.chatMsg.setMaxLength(32);

            this.buttons.add(new Button(-1, 4, 104, 160, 18, "Delete NPC"));

            var pathBtn = new Button(-2, 170, 24, 160, 18, "Path To Home");
            this.buttons.add(pathBtn);
            if (!this.npc.pathToHome) {
                pathBtn.message = "Don't Path Home";
            }

            var trackBtn = new Button(-3, 170, 42, 160, 18, "Track Player");
            this.buttons.add(trackBtn);
            if (!this.npc.trackPlayer) {
                trackBtn.message = "Don't Track Player";
            }

            var attackBtn = new Button(-4, 170, 64, 160, 18, "Can be attacked");
            this.buttons.add(attackBtn);
            if (!this.npc.isAttackable) {
                attackBtn.message = "Can't be attacked";
            }

            final int w3 = (this.width - 16) / 3;
            this.buttons.add(new Button(0, 4, 124, w3, 18, "Player Skin"));

            File npcDir = new File(((ExWorld) Minecraft.instance.level).getLevelDir(), "npc");
            if (npcDir.isDirectory()) {
                File[] files = npcDir.listFiles();
                if (files != null) {
                    int count = 1;
                    for (File file : files) {
                        int x = 4 + (w3 + 4) * (count % 3);
                        int y = 124 + count / 3 * 20;
                        String content = file.getName().split("\\.")[0];
                        this.buttons.add(new Button(count, x, y, w3, 18, content));
                        count++;
                    }
                }
            }
        } else if (this.page == 1) {
            this.selectedID = 0;

            this.setOnCreated = new Button(0, 4, 24, "");
            this.setOnUpdate = new Button(1, this.width / 2, 24, "");
            this.setOnPathReached = new Button(2, 4, 46, "");
            this.setOnAttacked = new Button(3, this.width / 2, 46, "");
            this.setOnDeath = new Button(4, 4, 68, "");
            this.setOnInteraction = new Button(5, this.width / 2, 68, "");
            this.buttons.add(this.setOnCreated);
            this.buttons.add(this.setOnUpdate);
            this.buttons.add(this.setOnPathReached);
            this.buttons.add(this.setOnAttacked);
            this.buttons.add(this.setOnDeath);
            this.buttons.add(this.setOnInteraction);
            this.resetScriptNames();

            this.buttons.add(new Button(6, 4, 90, 200, 20, "Reload Scripts"));
            this.buttons.add(new Button(7, 4, 112, 160, 18, "None"));

            String[] files = ((ExWorld) this.minecraft.level).getScriptFiles();
            int id = 1;
            for (String file : files) {
                int x = 6 + id % 3 * this.width / 3;
                int y = 112 + id / 3 * 20;
                this.buttons.add(new Button(7 + id, x, y, 160, 18, file));
                id++;
            }
        }
    }

    @Override
    protected void keyPressed(char ch, int key) {
        if (this.page == 0) {
            this.npcName.charTyped(ch, key);
            this.chatMsg.charTyped(ch, key);
        }

        super.keyPressed(ch, key);
    }

    @Override
    protected void buttonClicked(Button btn) {
        if (btn.id <= -20) {
            this.page = Math.abs(btn.id + 20);
            this.init();
            return;
        }

        if (this.page == 0) {
            if (btn.id == -1) {
                this.npc.remove();
                Minecraft.instance.setScreen(null);
            } else if (btn.id == -2) {
                this.npc.pathToHome = !this.npc.pathToHome;
                if (this.npc.pathToHome) {
                    btn.message = "Path To Home";
                } else {
                    btn.message = "Don't Path Home";
                }
            } else if (btn.id == -3) {
                this.npc.trackPlayer = !this.npc.trackPlayer;
                if (this.npc.trackPlayer) {
                    btn.message = "Track Player";
                } else {
                    this.npc.entityToTrack = null;
                    btn.message = "Don't Track Player";
                }
            } else if (btn.id == -4) {
                this.npc.isAttackable = !this.npc.isAttackable;
                if (this.npc.isAttackable) {
                    btn.message = "Can be attacked";
                } else {
                    btn.message = "Can't be attacked";
                }
            } else if (btn.id == 0) {
                ((ExLivingEntity) this.npc).setTexture("/mob/char.png");
            } else if (btn.id > 0) {
                File npcDir = new File(((ExWorld) Minecraft.instance.level).getLevelDir(), "npc");
                File[] files = npcDir.listFiles();
                if (files != null) {
                    if (btn.id - 1 < files.length) {
                        ((ExLivingEntity) this.npc).setTexture("/npc/" + files[btn.id - 1].getName());
                    }
                }
            }
        } else if (this.page == 1) {
            if (btn.id < 6) {
                this.selectedID = btn.id;
            } else if (btn.id == 6) {
                ((ExWorld) Minecraft.instance.level).getScriptHandler().loadScripts();
            } else if (btn.id == 7) {
                this.updateScriptFile("");
            } else {
                this.updateScriptFile(btn.message);
            }

            this.resetScriptNames();
        }

        this.npc.level.getChunkAt((int) this.npc.x, (int) this.npc.z).markUnsaved();
    }

    private void updateScriptFile(String name) {
        switch (this.selectedID) {
            case 0 -> {
                this.npc.onCreated = name;
                this.npc.runCreatedScript();
            }
            case 1 -> this.npc.onUpdate = name;
            case 2 -> this.npc.onPathReached = name;
            case 3 -> this.npc.onAttacked = name;
            case 4 -> this.npc.onDeath = name;
            case 5 -> this.npc.onInteraction = name;
        }
    }

    private void resetScriptNames() {
        AC_EntityNPC npc = this.npc;
        this.setMessage(this.setOnCreated, "OnNewSave", npc.onCreated);
        this.setMessage(this.setOnUpdate, "OnUpdate", npc.onUpdate);
        this.setMessage(this.setOnPathReached, "OnPathReached", npc.onPathReached);
        this.setMessage(this.setOnAttacked, "OnAttacked", npc.onAttacked);
        this.setMessage(this.setOnDeath, "OnDeath", npc.onDeath);
        this.setMessage(this.setOnInteraction, "OnInteraction", npc.onInteraction);
    }

    private void setMessage(Button button, String label, String value) {
        if (button.id == this.selectedID) {
            label += " (selected)";
        }
        button.message = String.format("%s: %s", label, value);
    }

    @Override
    protected void mouseClicked(int var1, int var2, int var3) {
        super.mouseClicked(var1, var2, var3);
        if (this.page == 0) {
            this.npcName.clicked(var1, var2, var3);
            this.chatMsg.clicked(var1, var2, var3);
        }
    }

    @Override
    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        if (this.page == 0) {
            this.drawString(this.font, "NPC Name", 4, 28, 14737632);
            this.drawString(this.font, "Chat Message", 4, 68, 14737632);
            this.npcName.render();
            this.chatMsg.render();
            this.npc.npcName = this.npcName.getValue();
            this.npc.chatMsg = this.chatMsg.getValue();
        }

        super.render(var1, var2, var3);
    }

    public static void showUI(AC_EntityNPC var0) {
        AC_TileEntityNpcPath.lastEntity = var0;
        Minecraft.instance.setScreen(new AC_GuiNPC(var0));
    }
}
