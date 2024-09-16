package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;

@SuppressWarnings("unused")
public class ScriptEntityArrow extends ScriptEntity {

    ArrowEntity entityArrow;

    ScriptEntityArrow(ArrowEntity var1) {
        super(var1);
        this.entityArrow = var1;
    }

    public int getInBlockID() {
        return this.entityArrow.inBlock;
    }

    public ScriptVec3 getInBlockCoords() {
        if (this.entityArrow.inBlock == 0) return null;
        return new ScriptVec3((float) this.entityArrow.blockX, (float) this.entityArrow.blockY, (float) this.entityArrow.blockZ);
    }

    public boolean getIsPlayersArrow() {
        return this.entityArrow.spawnedByPlayer;
    }

    public void setIsPlayersArrow(boolean var1) {
        this.entityArrow.spawnedByPlayer = var1;
    }

    public ScriptEntity getOwner() {
        return ScriptEntity.getEntityClass(this.entityArrow);
    }

    public void setAttackStrength(int value) {
        ((ExArrowEntity)this.entityArrow).setAttackStrength(value);
    }

    public int getAttackStrength(){
        return ((ExArrowEntity)this.entityArrow).getAttackStrength();
    }
}
