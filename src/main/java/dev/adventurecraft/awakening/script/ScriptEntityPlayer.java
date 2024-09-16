package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public class ScriptEntityPlayer extends ScriptEntityLiving {

    Player entityPlayer;

    ScriptEntityPlayer(Player entity) {
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
        this.entityPlayer.swing();
    }

    public void swingOffHand() {
        ((ExPlayerEntity) this.entityPlayer).swingOffhandItem();
    }
}
