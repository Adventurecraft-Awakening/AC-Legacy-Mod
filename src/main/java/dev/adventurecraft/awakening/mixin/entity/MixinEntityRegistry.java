package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.entity.ExEntityRegistry;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityIO.class)
public abstract class MixinEntityRegistry implements ExEntityRegistry {

    @Shadow
    private static void setId(Class<?> type, String name, int id) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        setId(ThrownEgg.class, "Egg", 12);
        setId(Fireball.class, "Fireball", 13);
        setId(FishingHook.class, "FishingRod", 14);
        setId(AC_EntitySkeletonSword.class, "SkeletonSword", 58);
        setId(AC_EntitySkeletonBoss.class, "SkeletonBoss", 59);
        setId(AC_EntityBat.class, "Bat", 60);
        setId(AC_EntityRat.class, "Rat", 61);
        setId(AC_EntityNPC.class, "NPC", 62);
        setId(AC_EntitySkeletonRifle.class, "SkeletonRifle", 63);
        setId(AC_EntitySkeletonShotgun.class, "SkeletonShotgun", 64);
        setId(AC_EntityZombiePistol.class, "ZombiePistol", 65);
        setId(AC_EntityBomb.class, "Bomb", 1000);
        setId(AC_EntityBoomerang.class, "Boomerang", 1001);
        setId(AC_EntityArrowBomb.class, "Bomb Arrow", 1002);
        setId(AC_EntityHookshot.class, "Hookshot", 1003);
        setId(AC_EntityLivingScript.class, "Script", 1004);
    }
}
