package dev.adventurecraft.awakening.extension.entity.projectile;
import net.minecraft.world.entity.Mob;

public interface ExArrowEntity {

    int getAttackStrength();

    void setAttackStrength(int value);

    void setOwner(Mob entity);
}
