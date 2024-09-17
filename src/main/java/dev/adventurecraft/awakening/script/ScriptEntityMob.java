package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.monster.ExMonsterEntity;
import net.minecraft.world.entity.monster.Monster;

@SuppressWarnings("unused")
public class ScriptEntityMob extends ScriptEntityCreature {

    Monster entityMob;

    ScriptEntityMob(Monster var1) {
        super(var1);
        this.entityMob = var1;
    }

    public void setAttackStrength(int var1) {
        ((ExMonsterEntity) this.entityMob).setAttackDamage(var1);
    }

    public int getAttackStrength() {
        return ((ExMonsterEntity) this.entityMob).getAttackDamage();
    }
}
