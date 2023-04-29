package dev.adventurecraft.awakening.mixin.client.entity.player;

import dev.adventurecraft.awakening.common.AC_CutsceneCamera;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_GuiPalette;
import dev.adventurecraft.awakening.common.InventoryDebug;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.entity.player.ExAbstractClientPlayerEntity;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PlayerKeypressManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.util.Session;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity extends PlayerEntity implements ExAbstractClientPlayerEntity {

    @Shadow
    protected Minecraft client;

    public MixinAbstractClientPlayerEntity(World arg) {
        super(arg);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Minecraft minecraft, World world, Session session, int dimensionId, CallbackInfo ci) {
        //this.name = world.properties.playerName; TODO
        movementSpeed = 1.0f;
    }

    @Redirect(method = "tickHandSwing", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/PlayerKeypressManager;perpendicularMovement:F"))
    private float multiplyPerpendicularMovementBySpeed(PlayerKeypressManager instance) {
        return this.movementSpeed * instance.perpendicularMovement;
    }

    @Redirect(method = "tickHandSwing", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/PlayerKeypressManager;parallelMovement:F"))
    private float multiplyParallelMovementBySpeed(PlayerKeypressManager instance) {
        return this.movementSpeed * instance.parallelMovement;
    }

    @Redirect(method = "updateDespawnCounter", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/World;isClient:Z"))
    private boolean disableDimensionSwitch(World instance) {
        return true;
    }

    @Redirect(method = "updateDespawnCounter", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;method_1372(DDD)Z"))
    private boolean disableMethod_1372(AbstractClientPlayerEntity instance, double e, double f, double v) {
        return false;
    }

    @Inject(method = "method_136", at = @At(value = "HEAD"), cancellable = true)
    private void redirectMethod136ToScript(int var1, boolean var2, CallbackInfo ci) {
        boolean press = true; //this.world.script.keyboard.processPlayerKeyPress(var1, var2); TODO
        if (!press) {
            ci.cancel();
        }
    }

    public void displayGUIPalette() {
        InventoryDebug var1 = new InventoryDebug("Palette", 54);
        var1.fillInventory(1);
        this.client.openScreen(new AC_GuiPalette(this.inventory, var1));
    }

    public void sendChatMessage(String var1) {
        ExWorldProperties worldProps;
        AC_CutsceneCamera activeCamera;
        String var2 = var1.toLowerCase();
        switch (var2) {
            case "/day":
                ((ExWorld) this.world).setTimeOfDay(0L);
                break;

            case "/night":
                ((ExWorld) this.world).setTimeOfDay(12000L);
                break;

            case "/fly":
                ((ExEntity) this).setIsFlying(!((ExEntity) this).handleFlying());
                this.client.overlay.addChatMessage(String.format("Flying: %b", ((ExEntity) this).handleFlying()));
                break;

            case "/health":
                this.health = 12;
                ((ExPlayerEntity) this).setMaxHealth(12);
                ((ExPlayerEntity) this).setHeartPiecesCount(0);
                break;

            case "/mapedit":
                AC_DebugMode.levelEditing = !AC_DebugMode.levelEditing;
                break;

            case "/removemobs":
                for (Entity var5 : (List<Entity>) this.world.entities) {
                    if (var5 instanceof LivingEntity && !(var5 instanceof PlayerEntity)) {
                        var5.removed = true;
                    }
                }
                break;

            case "/noclip":
                this.field_1642 = !this.field_1642;
                if (this.field_1642) {
                    ((ExEntity) this).setIsFlying(true);
                }
                this.client.overlay.addChatMessage(String.format("NoClip: %b", this.field_1642));
                break;

            case "/togglemelting":
                worldProps = ((ExWorldProperties) this.world.properties);
                worldProps.setIceMelts(!worldProps.getIceMelts());
                this.client.overlay.addChatMessage(String.format("Ice Melts: %b", worldProps.getIceMelts()));
                break;

            case "/toggledecay":
                worldProps = ((ExWorldProperties) this.world.properties);
                worldProps.setLeavesDecay(!worldProps.getLeavesDecay());
                this.client.overlay.addChatMessage(String.format("Leaves Decay: %b", worldProps.getLeavesDecay()));
                break;

            case "/cameraadd":
                activeCamera = ((ExMinecraft) this.client).getActiveCutsceneCamera();
                if (activeCamera != null) {
                    float var8;
                    try {
                        var8 = Float.parseFloat(var1.substring(11));
                    } catch (StringIndexOutOfBoundsException var6) {
                        this.client.overlay.addChatMessage("/cameraadd must have a time specified for the point");
                        return;
                    } catch (NumberFormatException var7) {
                        this.client.overlay.addChatMessage("\'" + var1.substring(11) + "\' is not a valid number");
                        return;
                    }

                    activeCamera.addCameraPoint(var8, (float) this.x, (float) (this.y - (double) this.standingEyeHeight + (double) 1.62F), (float) this.z, this.yaw, this.pitch, 2);
                    activeCamera.loadCameraEntities();
                    this.client.overlay.addChatMessage("Point Added");
                } else {
                    this.client.overlay.addChatMessage("Need to be editing a camera block");
                }
                break;

            case "/cameraclear":
                activeCamera = ((ExMinecraft) this.client).getActiveCutsceneCamera();
                if (activeCamera != null) {
                    activeCamera.clearPoints();
                    this.client.overlay.addChatMessage("Clearing Points");
                    activeCamera.loadCameraEntities();
                } else {
                    this.client.overlay.addChatMessage("Need to be editing a camera block");
                }
                break;

            case "/mobsburn":
                worldProps = ((ExWorldProperties) this.world.properties);
                worldProps.setMobsBurn(!worldProps.getMobsBurn());
                this.client.overlay.addChatMessage(String.format("Mobs Burn in Daylight: %b", worldProps.getMobsBurn()));
                break;

            /* TODO
            case "/config":
                this.client.openScreen(new AC_GuiWorldConfig(this.world));
                break;

            case "/test":
                this.client.openScreen(new AC_GuiMapEditHUD(this.world));
                break;
            */

            case "/renderpaths":
                AC_DebugMode.renderPaths = !AC_DebugMode.renderPaths;
                this.client.overlay.addChatMessage(String.format("Render Paths: %b", AC_DebugMode.renderPaths));
                break;

            case "/renderfov":
                AC_DebugMode.renderFov = !AC_DebugMode.renderFov;
                this.client.overlay.addChatMessage(String.format("Render FOV: %b", AC_DebugMode.renderFov));
                break;

            case "/fullbright":
                for (int var9 = 0; var9 < 16; ++var9) {
                    this.world.dimension.lightTable[var9] = 1.0F;
                }
                ((ExWorldEventRenderer) this.client.worldRenderer).updateAllTheRenderers();
                break;

            case "/undo":
                ((ExWorld) this.world).undo();
                break;

            case "/redo":
                ((ExWorld) this.world).redo();
                break;

            case "/fluidcollision":
                AC_DebugMode.isFluidHittable = !AC_DebugMode.isFluidHittable;
                break;

                /* TODO
            case "/scriptstats":
                AC_GuiScriptStats.showUI();
                break;

            case "/scriptstatreset":
                Iterator var3 = this.world.scriptHandler.scripts.values().iterator();
                while (var3.hasNext()) {
                    AC_JScriptInfo var10 = (AC_JScriptInfo) var3.next();
                    var10.maxTime = 0L;
                    var10.count = 0;
                    var10.totalTime = 0L;
                }
                break;
                */

            case "/help":
                this.client.overlay.addChatMessage("AdventureCraft Commands");
                this.client.overlay.addChatMessage("/config - Allows the world to be configed");
                this.client.overlay.addChatMessage("/day - Changes time to daytime");
                this.client.overlay.addChatMessage("/night - Changes time to nighttime");
                this.client.overlay.addChatMessage("/fly - Toggles flying");
                this.client.overlay.addChatMessage("/noclip - Toggles no clip");
                this.client.overlay.addChatMessage("/health - Sets health to 3 heart containers");
                this.client.overlay.addChatMessage("/mapedit - Toggles map editing mode");
                this.client.overlay.addChatMessage("/mobsburn - Toggles mobs burning in daylight");
                this.client.overlay.addChatMessage("/removemobs - Sets all mobs except the player as dead");
                this.client.overlay.addChatMessage("/togglemelting - Toggles ice melting");
                this.client.overlay.addChatMessage("/toggledecay - Toggles leaf decay");
                break;

                /* TODO
            default:
                String var11 = this.world.script.runString(var2);
                if (var11 != null) {
                    this.client.overlay.addChatMessage(var11);
                }
                break;
                */
        }
    }
}
