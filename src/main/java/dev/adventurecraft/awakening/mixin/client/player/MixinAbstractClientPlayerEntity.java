package dev.adventurecraft.awakening.mixin.client.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.common.gui.AC_GuiPalette;
import dev.adventurecraft.awakening.extension.client.entity.player.ExAbstractClientPlayerEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.input.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinAbstractClientPlayerEntity extends Player implements ExAbstractClientPlayerEntity {

    private CommandDispatcher<ServerCommandSource> commandDispatcher;
    private CommandDescriptions commandDescriptions;

    @Shadow
    protected Minecraft minecraft;

    public MixinAbstractClientPlayerEntity(Level arg) {
        super(arg);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Minecraft minecraft, Level world, User session, int dimensionId, CallbackInfo ci) {
        this.name = ((ExWorldProperties) world.levelData).getPlayerName();
        this.runSpeed = 1.0f;

        this.commandDispatcher = new CommandDispatcher<>();
        this.commandDescriptions = new CommandDescriptions();
        ServerCommands.registerCommands(this.commandDispatcher, this.commandDescriptions);
        ServerCommands.registerCommandsWithArgs(this.commandDispatcher, this.commandDescriptions);

        // TODO: register custom per-world gamerules for "gamerule" command
    }

    @Redirect(method = "serverAiStep", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/client/player/input/Input;leftImpulse:F"))
    private float multiplyPerpendicularMovementBySpeed(Input instance) {
        return this.runSpeed * instance.leftImpulse;
    }

    @Redirect(
        method = "serverAiStep",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/player/input/Input;forwardImpulse:F"))
    private float multiplyParallelMovementBySpeed(Input instance) {
        return this.runSpeed * instance.forwardImpulse;
    }

    @Redirect(
        method = "aiStep",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/Level;isClientSide:Z",
            ordinal = 1))
    private boolean disableDimensionSwitch(Level instance) {
        return true;
    }

    @Redirect(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;checkInBlock(DDD)Z"))
    private boolean disableMethod_1372(LocalPlayer instance, double e, double f, double v) {
        return false;
    }

    @Inject(method = "setKey", at = @At(value = "HEAD"), cancellable = true)
    private void redirectMethod136ToScript(int key, boolean isDown, CallbackInfo ci) {
        boolean press = ((ExWorld) this.level).getScript().keyboard.processPlayerKeyPress(key, isDown);
        if (!press) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "isSneaking",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/player/input/Input;isSneaking:Z"))
    private boolean defaultSneakIfNullKeyManager(Input instance) {
        if (instance == null) {
            return false;
        }
        return instance.isSneaking;
    }

    @Override
    public void displayGUIPalette() {
        var debugInventory = new InventoryDebug("Palette", Item.items.length);
        debugInventory.fillInventory(1);

        this.minecraft.setScreen(new AC_GuiPalette(this.inventory, debugInventory, 18, 6));
    }

    @Overwrite
    public void chat(String message) {
        if (message.startsWith("/")) {
            var source = new ServerCommandSource(this.minecraft, this.level, this);
            var reader = new StringReader(message.substring(1));
            var parsed = commandDispatcher.parse(reader, source);
            /*if (parsed.getReader().canRead() || parsed.getExceptions().size() > 0) {
                // TODO
            } else*/
            {
                try {
                    commandDispatcher.execute(parsed);
                } catch (CommandSyntaxException ex) {
                    this.minecraft.gui.addMessage("§c" + ex.getMessage());
                }
            }
        } else {
            String result = ((ExWorld) this.level).getScript().runString(message);
            if (result != null) {
                this.minecraft.gui.addMessage("JS: " + result);
            }
        }
    }
}
