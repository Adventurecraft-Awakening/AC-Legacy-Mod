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
    private static void register(Class<?> type, String name, int id) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        register(ThrownEgg.class, "Egg", 12);
        register(Fireball.class, "Fireball", 13);
        register(FishingHook.class, "FishingRod", 14);
        register(AC_EntitySkeletonSword.class, "SkeletonSword", 58);
        register(AC_EntitySkeletonBoss.class, "SkeletonBoss", 59);
        register(AC_EntityBat.class, "Bat", 60);
        register(AC_EntityRat.class, "Rat", 61);
        register(AC_EntityNPC.class, "NPC", 62);
        register(AC_EntitySkeletonRifle.class, "SkeletonRifle", 63);
        register(AC_EntitySkeletonShotgun.class, "SkeletonShotgun", 64);
        register(AC_EntityZombiePistol.class, "ZombiePistol", 65);
        register(AC_EntityBomb.class, "Bomb", 1000);
        register(AC_EntityBoomerang.class, "Boomerang", 1001);
        register(AC_EntityArrowBomb.class, "Bomb Arrow", 1002);
        register(AC_EntityHookshot.class, "Hookshot", 1003);
        register(AC_EntityLivingScript.class, "Script", 1004);
    }
}
