package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.monster.ExSlimeEntity;
import net.minecraft.world.entity.monster.Slime;

@SuppressWarnings("unused")
public class ScriptEntitySlime extends ScriptEntityLiving {

    Slime entitySlime;

    ScriptEntitySlime(Slime var1) {
        super(var1);
        this.entitySlime = var1;
    }

    public void setAttackStrength(int var1) {
        ((ExSlimeEntity) this.entitySlime).setAttackStrength(var1);
    }

    public int getAttackStrength() {
        return ((ExSlimeEntity) this.entitySlime).getAttackStrength();
    }

    public void setSlimeSize(int var1) {
        this.entitySlime.setSize(var1);
    }

    public int getSlimeSize() {
        return this.entitySlime.getSize();
    }
}
