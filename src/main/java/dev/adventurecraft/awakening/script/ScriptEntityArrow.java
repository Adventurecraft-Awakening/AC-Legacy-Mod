package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import net.minecraft.world.entity.projectile.Arrow;

@SuppressWarnings("unused")
public class ScriptEntityArrow extends ScriptEntity {

    Arrow entityArrow;

    ScriptEntityArrow(Arrow var1) {
        super(var1);
        this.entityArrow = var1;
    }

    public int getInBlockID() {
        return this.entityArrow.inTile;
    }

    public ScriptVec3 getInBlockCoords() {
        if (this.entityArrow.inTile == 0) return null;
        return new ScriptVec3((float) this.entityArrow.xTile, (float) this.entityArrow.yTile, (float) this.entityArrow.zTile);
    }

    public boolean getIsPlayersArrow() {
        return this.entityArrow.player;
    }

    public void setIsPlayersArrow(boolean var1) {
        this.entityArrow.player = var1;
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
