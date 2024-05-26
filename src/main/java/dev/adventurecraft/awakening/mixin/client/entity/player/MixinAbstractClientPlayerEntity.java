package dev.adventurecraft.awakening.mixin.client.entity.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.client.entity.player.ExAbstractClientPlayerEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PlayerKeypressManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.util.Session;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity extends PlayerEntity implements ExAbstractClientPlayerEntity {

    private CommandDispatcher<ServerCommandSource> commandDispatcher;
    private CommandDescriptions commandDescriptions;

    @Shadow
    protected Minecraft client;

    public MixinAbstractClientPlayerEntity(World arg) {
        super(arg);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Minecraft minecraft, World world, Session session, int dimensionId, CallbackInfo ci) {
        this.name = ((ExWorldProperties) world.properties).getPlayerName();
        this.movementSpeed = 1.0f;

        this.commandDispatcher = new CommandDispatcher<>();
        this.commandDescriptions = new CommandDescriptions();
        ServerCommands.registerCommands(this.commandDispatcher, this.commandDescriptions);
        ServerCommands.registerCommandsWithArgs(this.commandDispatcher, this.commandDescriptions);
    }

    @Redirect(method = "tickHandSwing", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/client/PlayerKeypressManager;perpendicularMovement:F"))
    private float multiplyPerpendicularMovementBySpeed(PlayerKeypressManager instance) {
        return this.movementSpeed * instance.perpendicularMovement;
    }

    @Redirect(
        method = "tickHandSwing",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/PlayerKeypressManager;parallelMovement:F"))
    private float multiplyParallelMovementBySpeed(PlayerKeypressManager instance) {
        return this.movementSpeed * instance.parallelMovement;
    }

    @Redirect(
        method = "updateDespawnCounter",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/World;isClient:Z",
            ordinal = 1))
    private boolean disableDimensionSwitch(World instance) {
        return true;
    }

    @Redirect(
        method = "updateDespawnCounter",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;method_1372(DDD)Z"))
    private boolean disableMethod_1372(AbstractClientPlayerEntity instance, double e, double f, double v) {
        return false;
    }

    @Inject(method = "method_136", at = @At(value = "HEAD"), cancellable = true)
    private void redirectMethod136ToScript(int var1, boolean var2, CallbackInfo ci) {
        boolean press = ((ExWorld) this.world).getScript().keyboard.processPlayerKeyPress(var1, var2);
        if (!press) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "method_1373",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/PlayerKeypressManager;sneak:Z"))
    private boolean defaultSneakIfNullKeyManager(PlayerKeypressManager instance) {
        if (instance == null) {
            return false;
        }
        return instance.sneak;
    }

    @Override
    public void displayGUIPalette() {
        var debugInventory = new InventoryDebug("Palette", Item.byId.length);
        debugInventory.fillInventory(1);

        this.client.openScreen(new AC_GuiPalette(this.inventory, debugInventory, 18, 6));
    }

    @Overwrite
    public void sendChatMessage(String message) {
        if (message.startsWith("/")) {
            var source = new ServerCommandSource(this.client, this.world, this);
            var reader = new StringReader(message.substring(1));
            var parsed = commandDispatcher.parse(reader, source);
            /*if (parsed.getReader().canRead() || parsed.getExceptions().size() > 0) {
                // TODO
            } else*/
            {
                try {
                    commandDispatcher.execute(parsed);
                } catch (CommandSyntaxException ex) {
                    this.client.overlay.addChatMessage("§c" + ex.getMessage());
                }
            }
        } else {
            String result = ((ExWorld) this.world).getScript().runString(message);
            if (result != null) {
                this.client.overlay.addChatMessage("JS: " + result);
            }
        }
    }
}
