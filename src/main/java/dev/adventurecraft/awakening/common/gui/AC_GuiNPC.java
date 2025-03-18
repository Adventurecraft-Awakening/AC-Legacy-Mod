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
        int var1 = (this.width - 16) / 4;
        this.buttons.add(new Button(-20, 4, 0, var1, 18, "Misc"));
        this.buttons.add(new Button(-21, 4 + 4 + var1, 0, var1, 18, "Script"));
        Button var2;
        int var4;
        int var6;
        int var7;
        if (this.page == 0) {
            this.npcName = new EditBox(this, this.font, 4, 40, 160, 20, this.npc.npcName);
            this.npcName.active = true;
            this.npcName.setMaxLength(32);
            this.chatMsg = new EditBox(this, this.font, 4, 80, 160, 20, this.npc.chatMsg);
            this.chatMsg.active = false;
            this.chatMsg.setMaxLength(32);
            var2 = new Button(-1, 4, 104, 160, 18, "Delete NPC");
            this.buttons.add(var2);
            var2 = new Button(-2, 170, 24, 160, 18, "Path To Home");
            this.buttons.add(var2);
            if (!this.npc.pathToHome) {
                var2.message = "Don't Path Home";
            }

            var2 = new Button(-3, 170, 42, 160, 18, "Track Player");
            this.buttons.add(var2);
            if (!this.npc.trackPlayer) {
                var2.message = "Don't Track Player";
            }

            var2 = new Button(-4, 170, 64, 160, 18, "Can be attacked");
            this.buttons.add(var2);
            if (!this.npc.isAttackable) {
                var2.message = "Can't be attacked";
            }

            File var3 = new File(((ExWorld) Minecraft.instance.level).getLevelDir(), "npc");
            var4 = 1;
            var1 = (this.width - 16) / 3;
            var2 = new Button(0, 4, 124, var1, 18, "Player Skin");
            this.buttons.add(var2);
            if (var3.isDirectory()) {
                File[] var5 = var3.listFiles();
                var6 = var5.length;

                for (var7 = 0; var7 < var6; ++var7) {
                    File var8 = var5[var7];
                    var2 = new Button(var4, 4 + (var1 + 4) * (var4 % 3), 124 + var4 / 3 * 20, var1, 18, var8.getName().split("\\.")[0]);
                    this.buttons.add(var2);
                    ++var4;
                }
            }
        } else if (this.page == 1) {
            this.selectedID = 0;
            this.setOnCreated = new Button(0, 4, 24, "OnCreated (selected): " + this.npc.onCreated);
            this.setOnUpdate = new Button(1, this.width / 2, 24, "OnUpdate: " + this.npc.onUpdate);
            this.setOnPathReached = new Button(2, 4, 46, "OnPathReached: " + this.npc.onPathReached);
            this.setOnAttacked = new Button(3, this.width / 2, 46, "OnAttacked: " + this.npc.onAttacked);
            this.setOnDeath = new Button(4, 4, 68, "OnDeath: " + this.npc.onDeath);
            this.setOnInteraction = new Button(5, this.width / 2, 68, "OnInteraction: " + this.npc.onInteraction);
            this.buttons.add(this.setOnCreated);
            this.buttons.add(this.setOnUpdate);
            this.buttons.add(this.setOnPathReached);
            this.buttons.add(this.setOnAttacked);
            this.buttons.add(this.setOnDeath);
            this.buttons.add(this.setOnInteraction);
            var2 = new Button(6, 4, 90, 200, 20, "Reload Scripts");
            this.buttons.add(var2);
            var2 = new Button(7, 4, 112, 160, 18, "None");
            this.buttons.add(var2);
            String[] var9 = ((ExWorld) this.minecraft.level).getScriptFiles();
            if (var9 != null) {
                var4 = 1;
                String[] var10 = var9;
                var6 = var9.length;

                for (var7 = 0; var7 < var6; ++var7) {
                    String var11 = var10[var7];
                    var2 = new Button(7 + var4, 6 + var4 % 3 * this.width / 3, 112 + var4 / 3 * 20, 160, 18, var11);
                    this.buttons.add(var2);
                    ++var4;
                }
            }
        }
    }

    @Override
    protected void keyPressed(char var1, int var2) {
        if (this.page == 0) {
            this.npcName.charTyped(var1, var2);
            this.chatMsg.charTyped(var1, var2);
        }

        super.keyPressed(var1, var2);
    }

    @Override
    protected void buttonClicked(Button var1) {
        if (var1.id <= -20) {
            this.page = Math.abs(var1.id + 20);
            this.init();
            return;
        }

        if (this.page == 0) {
            if (var1.id == -1) {
                this.npc.remove();
                Minecraft.instance.setScreen(null);
            } else if (var1.id == -2) {
                this.npc.pathToHome = !this.npc.pathToHome;
                if (this.npc.pathToHome) {
                    var1.message = "Path To Home";
                } else {
                    var1.message = "Don't Path Home";
                }
            } else if (var1.id == -3) {
                this.npc.trackPlayer = !this.npc.trackPlayer;
                if (this.npc.trackPlayer) {
                    var1.message = "Track Player";
                } else {
                    this.npc.entityToTrack = null;
                    var1.message = "Don't Track Player";
                }
            } else if (var1.id == -4) {
                this.npc.isAttackable = !this.npc.isAttackable;
                if (this.npc.isAttackable) {
                    var1.message = "Can be attacked";
                } else {
                    var1.message = "Can't be attacked";
                }
            } else if (var1.id == 0) {
                ((ExLivingEntity) this.npc).setTexture("/mob/char.png");
            } else if (var1.id > 0) {
                File var2 = new File(((ExWorld) Minecraft.instance.level).getLevelDir(), "npc");
                File[] var3 = var2.listFiles();
                if (var1.id - 1 < var3.length) {
                    ((ExLivingEntity) this.npc).setTexture("/npc/" + var3[var1.id - 1].getName());
                }
            }
        } else if (this.page == 1) {
            if (var1.id < 6) {
                this.selectedID = var1.id;
            } else if (var1.id == 6) {
                ((ExWorld) Minecraft.instance.level).getScriptHandler().loadScripts();
            } else if (var1.id == 7) {
                this.updateScriptFile("");
            } else {
                this.updateScriptFile(var1.message);
            }

            this.resetScriptNames();
        }

        this.npc.level.getChunkAt((int) this.npc.x, (int) this.npc.z).markUnsaved();
    }

    private void updateScriptFile(String var1) {
        if (this.selectedID == 0) {
            this.npc.onCreated = var1;
            this.npc.runCreatedScript();
        } else if (this.selectedID == 1) {
            this.npc.onUpdate = var1;
        } else if (this.selectedID == 2) {
            this.npc.onPathReached = var1;
        } else if (this.selectedID == 3) {
            this.npc.onAttacked = var1;
        } else if (this.selectedID == 4) {
            this.npc.onDeath = var1;
        } else if (this.selectedID == 5) {
            this.npc.onInteraction = var1;
        }
    }

    private void resetScriptNames() {
        this.setOnCreated.message = "OnNewSave: " + this.npc.onCreated;
        this.setOnUpdate.message = "OnUpdate: " + this.npc.onUpdate;
        this.setOnPathReached.message = "OnPathReached: " + this.npc.onPathReached;
        this.setOnAttacked.message = "OnAttacked: " + this.npc.onAttacked;
        this.setOnDeath.message = "OnDeath: " + this.npc.onDeath;
        this.setOnInteraction.message = "OnInteraction: " + this.npc.onInteraction;
        if (this.selectedID == 0) {
            this.setOnCreated.message = "OnNewSave (selected): " + this.npc.onCreated;
        } else if (this.selectedID == 1) {
            this.setOnUpdate.message = "OnUpdate (selected): " + this.npc.onUpdate;
        } else if (this.selectedID == 2) {
            this.setOnPathReached.message = "OnPathReached (selected): " + this.npc.onPathReached;
        } else if (this.selectedID == 3) {
            this.setOnAttacked.message = "OnAttacked (selected): " + this.npc.onAttacked;
        } else if (this.selectedID == 4) {
            this.setOnDeath.message = "OnDeath (selected): " + this.npc.onDeath;
        } else if (this.selectedID == 5) {
            this.setOnInteraction.message = "OnInteraction (selected): " + this.npc.onInteraction;
        }
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
