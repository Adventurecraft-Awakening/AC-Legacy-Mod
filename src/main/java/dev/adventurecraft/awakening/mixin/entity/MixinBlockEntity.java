package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.extension.entity.ExBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.tile.AC_TileEntityStructure;
import dev.adventurecraft.awakening.tile.entity.*;
import dev.adventurecraft.awakening.world.BlockPos;
import dev.adventurecraft.awakening.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TileEntity.class)
public abstract class MixinBlockEntity implements ExBlockEntity {

    @Shadow private static Map<Class<?>, String> classIdMap;

    @Shadow public Level level;
    @Shadow public int x;
    @Shadow public int y;
    @Shadow public int z;

    @Unique private boolean killedFromSaving;

    @Shadow
    private static void setId(Class<?> type, String string) {
        throw new AssertionError();
    }

    @Override
    public String getClassName() {
        return classIdMap.get(this.getClass());
    }

    @Override
    public BlockPos getBlockPos() {
        return new BlockPos.Mut(this.x, this.y, this.z);
    }

    @Override
    public boolean isKilledFromSaving() {
        return this.killedFromSaving;
    }

    @Override
    public void setKilledFromSaving(boolean value) {
        this.killedFromSaving = value;
    }

    @Override
    public BlockState getBlockState() {
        return ((ExWorld) this.level).ac$getBlockState(this.x, this.y, this.z);
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(stringValue = "Music"))
    private static String renameNoteblock(String constant) {
        return "Note";
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerACEntities(CallbackInfo ci) {
        setId(AC_TileEntityMobSpawner.class, "MobSpawnerNew");
        setId(AC_TileEntityTrigger.class, "Trigger");
        setId(AC_TileEntityTriggerInverter.class, "TriggerInverter");
        setId(AC_TileEntityTriggerMemory.class, "TriggerMemory");
        setId(AC_TileEntityRedstoneTrigger.class, "RedstoneTrigger");
        setId(AC_TileEntityWeather.class, "Weather");
        setId(AC_TileEntityMusic.class, "Music");
        setId(AC_TileEntityTimer.class, "Timer");
        setId(AC_TileEntityMessage.class, "Message");
        setId(AC_TileEntityCamera.class, "Camera");
        setId(AC_TileEntityTriggerPushable.class, "TriggerPushable");
        setId(AC_TileEntityStorage.class, "Storage");
        setId(AC_TileEntityHealDamage.class, "HealDamage");
        setId(AC_TileEntityTeleport.class, "Teleport");
        setId(AC_TileEntityTree.class, "Tree");
        setId(AC_TileEntityScript.class, "Script");
        setId(AC_TileEntityStore.class, "Store");
        setId(AC_TileEntityEffect.class, "Effect");
        setId(AC_TileEntityUrl.class, "Url");
        setId(AC_TileEntityNpcPath.class, "NpcPath");

        setId(AC_TileEntityStructure.class, "StructureBlock");
    }
}
