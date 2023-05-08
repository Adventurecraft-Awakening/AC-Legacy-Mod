package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.monster.ExMonsterEntity;
import net.minecraft.entity.monster.MonsterEntity;

@SuppressWarnings("unused")
public class ScriptEntityMob extends ScriptEntityCreature {

    MonsterEntity entityMob;

    ScriptEntityMob(MonsterEntity var1) {
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
