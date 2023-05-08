package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.common.AC_EntityNPC;

@SuppressWarnings("unused")
public class ScriptEntityNPC extends ScriptEntityLivingScript {

    AC_EntityNPC npc;

    ScriptEntityNPC(AC_EntityNPC var1) {
        super(var1);
        this.npc = var1;
    }

    public String getName() {
        return this.npc.npcName;
    }

    public void setName(String var1) {
        this.npc.npcName = var1;
    }

    public String getChatMsg() {
        return this.npc.chatMsg;
    }

    public void setChatMsg(String var1) {
        this.npc.chatMsg = var1;
    }

    public double getSpawnX() {
        return this.npc.spawnX;
    }

    public void setSpawnX(double var1) {
        this.npc.spawnX = var1;
    }

    public double getSpawnY() {
        return this.npc.spawnY;
    }

    public void setSpawnY(double var1) {
        this.npc.spawnY = var1;
    }

    public double getSpawnZ() {
        return this.npc.spawnZ;
    }

    public void setSpawnZ(double var1) {
        this.npc.spawnZ = var1;
    }

    public boolean getPathToHome() {
        return this.npc.pathToHome;
    }

    public void setPathToHome(boolean var1) {
        this.npc.pathToHome = var1;
    }

    public boolean getTrackPlayer() {
        return this.npc.trackPlayer;
    }

    public void setTrackPlayer(boolean var1) {
        this.npc.trackPlayer = var1;
    }
}
