package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.entity.ExBlockEntity;
import net.minecraft.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements ExBlockEntity {

    private boolean killedFromSaving;

    @Shadow
    private static Map<Class<?>, String> CLASS_TO_ID;

    @Shadow
    private static void register(Class<?> type, String string) {
        throw new AssertionError();
    }

    @Override
    public String getClassName() {
        return CLASS_TO_ID.get(this.getClass());
    }

    @Override
    public boolean isKilledFromSaving() {
        return this.killedFromSaving;
    }

    @Override
    public void setKilledFromSaving(boolean value) {
        this.killedFromSaving = value;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(stringValue = "Music"))
    private static String renameNoteblock(String constant) {
        return "Note";
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerACEntities(CallbackInfo ci) {
        //register(AC_TileEntityMobSpawner.class, "MobSpawnerNew"); TODO
        register(AC_TileEntityTrigger.class, "Trigger");
        register(AC_TileEntityTriggerInverter.class, "TriggerInverter");
        register(AC_TileEntityTriggerMemory.class, "TriggerMemory");
        register(AC_TileEntityRedstoneTrigger.class, "RedstoneTrigger");
        register(AC_TileEntityWeather.class, "Weather");
        register(AC_TileEntityMusic.class, "Music");
        register(AC_TileEntityTimer.class, "Timer");
        register(AC_TileEntityMessage.class, "Message");
        register(AC_TileEntityCamera.class, "Camera");
        register(AC_TileEntityTriggerPushable.class, "TriggerPushable");
        register(AC_TileEntityStorage.class, "Storage");
        register(AC_TileEntityHealDamage.class, "HealDamage");
        register(AC_TileEntityTeleport.class, "Teleport");
        register(AC_TileEntityTree.class, "Tree");
        //register(AC_TileEntityScript.class, "Script"); TODO
        register(AC_TileEntityStore.class, "Store");
        register(AC_TileEntityEffect.class, "Effect");
        register(AC_TileEntityUrl.class, "Url");
        //register(AC_TileEntityNpcPath.class, "NpcPath"); TODO
    }
}
