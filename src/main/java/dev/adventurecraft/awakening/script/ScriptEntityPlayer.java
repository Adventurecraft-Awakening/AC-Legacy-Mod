package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

@SuppressWarnings("unused")
public class ScriptEntityPlayer extends ScriptEntityLiving {

    PlayerEntity entityPlayer;

    ScriptEntityPlayer(PlayerEntity entity) {
        super(entity);
        this.entityPlayer = entity;
    }

    public ScriptInventoryPlayer getInventory() {
        return new ScriptInventoryPlayer(this.entityPlayer.inventory);
    }

    public String getCloak() {
        return ((ExPlayerEntity) this.entityPlayer).getCloakTexture();
    }

    public void setCloak(String name) {
        ((ExPlayerEntity) this.entityPlayer).setCloakTexture(name);
    }

    public void removeCloak() {
        setCloak(null);
    }

    public void swingMainHand() {
        this.entityPlayer.swingHand();
    }

    public void swingOffHand() {
        ((ExPlayerEntity) this.entityPlayer).swingOffhandItem();
    }
    
    public void setPositionWithUpdate(double x, double y, double z){

        entityPlayer.setPosition(x,y,z);
        ((ExMinecraft)Minecraft.instance).ReloadWorld();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            ACMod.LOGGER.warn("Interrupted Exception: "+e);
        }
        entityPlayer.setPosition(x,y,z);
    }
}
