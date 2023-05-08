package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

@SuppressWarnings("unused")
public class ScriptEntityPlayer extends ScriptEntityLiving {

    PlayerEntity entityPlayer;

    ScriptEntityPlayer(PlayerEntity var1) {
        super(var1);
        this.entityPlayer = var1;
    }

    public ScriptInventoryPlayer getInventory() {
        return new ScriptInventoryPlayer(this.entityPlayer.inventory);
    }

    public String getCloak() {
        return ((ExPlayerEntity) this.entityPlayer).getCloakTexture();
    }

    public void setCloak(String var1) {
        ((ExPlayerEntity) this.entityPlayer).setCloakTexture(var1);
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
}
