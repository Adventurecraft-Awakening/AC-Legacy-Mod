package dev.adventurecraft.awakening.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

import static dev.adventurecraft.awakening.common.CommandUtils.*;

public class ServerCommands {

    public static void registerCommands(
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandDescriptions descs) {

        dispatcher.register(literal("config").executes(
            descs.attach(ServerCommands::cmdConfig, "Opens world configuration")));

        dispatcher.register(literal("test").executes(
            ServerCommands::cmdTest));

        dispatcher.register(literal("scriptstats").executes(
            ServerCommands::cmdScriptStats));

        dispatcher.register(literal("day").executes(
            descs.attach(ServerCommands::cmdDay, "Changes time to daytime")));

        dispatcher.register(literal("night").executes(
            descs.attach(ServerCommands::cmdNight, "Changes time to nighttime")));

        dispatcher.register(literal("removemobs").executes(
            descs.attach(ServerCommands::cmdRemoveMobs, "Sets all mobs except the player as dead")));

        dispatcher.register(literal("cameraclear").executes(
            ServerCommands::cmdCameraClear));

        dispatcher.register(literal("fullbright").executes(
            ServerCommands::cmdFullBright));

        dispatcher.register(literal("scriptstatreset").executes(
            ServerCommands::cmdScriptStatReset));

        dispatcher.register(optionalArg(literal("health"),
            "amount", IntegerArgumentType.integer(1),
            descs.attach(ServerCommands::cmdHealth, "Sets health and max health")));

        dispatcher.register(optionalArg(literal("undo"),
            "amount", IntegerArgumentType.integer(1),
            ServerCommands::cmdUndo));

        dispatcher.register(optionalArg(literal("redo"),
            "amount", IntegerArgumentType.integer(1),
            ServerCommands::cmdRedo));

        dispatcher.register(optionalArg(literal("mapedit"),
            "value", BoolArgumentType.bool(),
            descs.attach(ServerCommands::cmdMapEdit, "Toggles map editing mode")));

        dispatcher.register(optionalArg(literal("renderpaths"),
            "value", BoolArgumentType.bool(),
            ServerCommands::cmdRenderPaths));

        dispatcher.register(optionalArg(literal("renderfov"),
            "value", BoolArgumentType.bool(),
            ServerCommands::cmdRenderFov));

        dispatcher.register(optionalArg(literal("rendercollisions"),
            "value", BoolArgumentType.bool(),
            ServerCommands::cmdRenderCollisions));

        dispatcher.register(optionalArg(literal("renderrays"),
            "value", BoolArgumentType.bool(),
            ServerCommands::cmdRenderRays));

        dispatcher.register(optionalArg(literal("fluidcollision"),
            "value", BoolArgumentType.bool(),
            ServerCommands::cmdFluidCollision));

        dispatcher.register(optionalArg(literal("fly"),
            "value", BoolArgumentType.bool(),
            descs.attach(ServerCommands::cmdFly, "Toggles flying")));

        dispatcher.register(optionalArg(literal("noclip"),
            "value", BoolArgumentType.bool(),
            descs.attach(ServerCommands::cmdNoClip, "Toggles no clip")));

        dispatcher.register(optionalArg(literal("togglemelting"),
            "value", BoolArgumentType.bool(),
            descs.attach(ServerCommands::cmdToggleMelting, "Toggles ice melting")));

        dispatcher.register(optionalArg(literal("toggledecay"),
            "value", BoolArgumentType.bool(),
            descs.attach(ServerCommands::cmdToggleDecay, "Toggles leaf decay")));

        dispatcher.register(optionalArg(literal("mobsburn"),
            "value", BoolArgumentType.bool(),
            descs.attach(ServerCommands::cmdMobsBurn, "Toggles mobs burning in daylight")));

        dispatcher.register(requiredArg(literal("cameraadd"),
            "time", FloatArgumentType.floatArg(),
            ServerCommands::cmdCameraAdd));

        dispatcher.register(optionalArg(literal("help"),
            "page", IntegerArgumentType.integer(1),
            (ctx, page) -> ServerCommands.cmdHelp(ctx, dispatcher, descs, page)));
    }

    public static int cmdConfig(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            source.getClient().openScreen(new AC_GuiWorldConfig(world));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdTest(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            source.getClient().openScreen(new AC_GuiMapEditHUD(world));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdScriptStats(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            source.getClient().openScreen(new AC_GuiScriptStats(world));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdDay(CommandContext<ServerCommandSource> context) {
        var world = context.getSource().getWorld();
        if (world instanceof ExWorld exWorld) {
            exWorld.setTimeOfDay(0L);
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdNight(CommandContext<ServerCommandSource> context) {
        var world = context.getSource().getWorld();
        if (world instanceof ExWorld exWorld) {
            exWorld.setTimeOfDay(14000L);
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdRemoveMobs(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            int mobCount = 0;
            int count = 0;
            for (Entity entity : (List<Entity>) world.entities) {
                if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
                    mobCount++;
                    if (!entity.removed) {
                        entity.removed = true;
                        count++;
                    }
                }
            }

            source.getClient().overlay.addChatMessage(String.format("Removed %d out of %d mobs", count, mobCount));
            return count;
        }
        return 0;
    }

    public static int cmdCameraClear(CommandContext<ServerCommandSource> context) {
        var client = context.getSource().getClient();
        var activeCamera = ((ExMinecraft) client).getActiveCutsceneCamera();
        if (activeCamera != null) {
            int pointCount = activeCamera.cameraPoints.size();
            activeCamera.clearPoints();
            activeCamera.loadCameraEntities();

            client.overlay.addChatMessage(String.format("Cleared %d camera points", pointCount));
            return pointCount;
        }
        client.overlay.addChatMessage("Need to be editing a camera block");
        return 0;
    }

    public static int cmdFullBright(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            for (int i = 0; i < 16; ++i) {
                world.dimension.lightTable[i] = 1.0F;
            }
            ((ExWorldEventRenderer) source.getClient().worldRenderer).updateAllTheRenderers();

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdScriptStatReset(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world instanceof ExWorld exWorld) {
            var infos = exWorld.getScriptHandler().scripts.values();
            int count = infos.size();
            for (AC_JScriptInfo info : infos) {
                info.clear();
            }

            source.getClient().overlay.addChatMessage(String.format("Reset %d script stats", count));
            return count;
        }
        return 0;
    }

    public static int cmdHealth(CommandContext<ServerCommandSource> context, Integer amount) {
        var source = context.getSource();
        var entity = source.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            int health = amount != null ? amount : 12;
            livingEntity.health = health;
            ((ExLivingEntity) livingEntity).setMaxHealth(health);
            if (livingEntity instanceof ExPlayerEntity exPlayer) {
                exPlayer.setHeartPiecesCount(0);
            }

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdUndo(CommandContext<ServerCommandSource> context, Integer amount) {
        return cmdUndo(context.getSource(), amount);
    }

    public static int cmdUndo(ServerCommandSource source, Integer amount) {
        var world = source.getWorld();
        if (world instanceof ExWorld exWorld) {
            var undoStack = exWorld.getUndoStack();
            int reqCount = amount != null ? amount : 1;
            int count = Math.min(reqCount, undoStack.undoStack.size());
            for (int i = 0; i < count; i++) {
                undoStack.undo(world);
            }

            source.getClient().overlay.addChatMessage(String.format(
                "Undone %d actions (Undos left: %d, Redos left: %d)",
                count,
                undoStack.undoStack.size(),
                undoStack.redoStack.size()));
            return count;
        }
        return 0;
    }

    public static int cmdRedo(CommandContext<ServerCommandSource> context, Integer amount) {
        return cmdRedo(context.getSource(), amount);
    }

    public static int cmdRedo(ServerCommandSource source, Integer amount) {
        var world = source.getWorld();
        if (world instanceof ExWorld exWorld) {
            var undoStack = exWorld.getUndoStack();
            int reqCount = amount != null ? amount : 1;
            int count = Math.min(reqCount, undoStack.redoStack.size());
            for (int i = 0; i < count; i++) {
                undoStack.redo(world);
            }

            source.getClient().overlay.addChatMessage(String.format(
                "Redone %d actions (Undos left: %d, Redos left: %d)",
                count,
                undoStack.undoStack.size(),
                undoStack.redoStack.size()));
            return count;
        }
        return 0;
    }

    public static int cmdMapEdit(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.levelEditing = value != null ? value : !AC_DebugMode.levelEditing;

        client.overlay.addChatMessage(String.format("Level Editing: %b", AC_DebugMode.levelEditing));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdRenderPaths(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.renderPaths = value != null ? value : !AC_DebugMode.renderPaths;

        client.overlay.addChatMessage(String.format("Render Paths: %b", AC_DebugMode.renderPaths));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdRenderFov(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.renderFov = value != null ? value : !AC_DebugMode.renderFov;

        client.overlay.addChatMessage(String.format("Render FOV: %b", AC_DebugMode.renderFov));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdRenderCollisions(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.renderCollisions = value != null ? value : !AC_DebugMode.renderCollisions;

        client.overlay.addChatMessage(String.format("Render Collisions: %b", AC_DebugMode.renderCollisions));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdRenderRays(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.renderRays = value != null ? value : !AC_DebugMode.renderRays;

        client.overlay.addChatMessage(String.format("Render Rays: %b", AC_DebugMode.renderRays));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdFluidCollision(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.isFluidHittable = value != null ? value : !AC_DebugMode.isFluidHittable;

        client.overlay.addChatMessage(String.format("Fluid Collision: %b", AC_DebugMode.isFluidHittable));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdFly(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var entity = source.getEntity();
        if (entity instanceof ExEntity exEntity) {
            exEntity.setIsFlying(value != null ? value : !exEntity.handleFlying());

            source.getClient().overlay.addChatMessage(String.format("Flying: %b", exEntity.handleFlying()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdNoClip(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var entity = source.getEntity();
        if (entity instanceof ExEntity exEntity) {
            entity.field_1642 = value != null ? value : !entity.field_1642;
            if (entity.field_1642) {
                exEntity.setIsFlying(true);
            }

            source.getClient().overlay.addChatMessage(String.format("NoClip: %b", entity.field_1642));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdToggleMelting(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            var props = (ExWorldProperties) world.properties;
            props.setIceMelts(value != null ? value : !props.getIceMelts());

            source.getClient().overlay.addChatMessage(String.format("Ice Melts: %b", props.getIceMelts()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdToggleDecay(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            var props = (ExWorldProperties) world.properties;
            props.setLeavesDecay(value != null ? value : !props.getLeavesDecay());

            source.getClient().overlay.addChatMessage(String.format("Leaves Decay: %b", props.getLeavesDecay()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdMobsBurn(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            var props = (ExWorldProperties) world.properties;
            props.setMobsBurn(value != null ? value : !props.getMobsBurn());

            source.getClient().overlay.addChatMessage(String.format("Mobs Burn in Daylight: %b", props.getMobsBurn()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdCameraAdd(CommandContext<ServerCommandSource> context, float time) {
        var source = context.getSource();
        var client = source.getClient();
        var entity = source.getEntity();

        AC_CutsceneCamera activeCamera = ((ExMinecraft) client).getActiveCutsceneCamera();
        if (activeCamera != null) {
            float x = (float) entity.x;
            float y = (float) (entity.y - (double) entity.standingEyeHeight + 1.62D);
            float z = (float) entity.z;
            activeCamera.addCameraPoint(time, x, y, z, entity.yaw, entity.pitch, AC_CutsceneCameraBlendType.QUADRATIC);
            activeCamera.loadCameraEntities();

            client.overlay.addChatMessage("Camera point added");
            return Command.SINGLE_SUCCESS;
        }
        client.overlay.addChatMessage("Need to be editing a camera block");
        return 0;
    }

    public static int cmdHelp(
        CommandContext<ServerCommandSource> context,
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandDescriptions descriptions,
        Integer page) {

        int commandsPerPage = 8;
        int currentPage = page != null ? (page - 1) : 0;
        var source = context.getSource();
        var client = source.getClient();

        var usageMap = dispatcher.getSmartUsage(dispatcher.getRoot(), source);
        int pageCount = (usageMap.size() + commandsPerPage - 1) / commandsPerPage;
        client.overlay.addChatMessage(String.format("§2Help page %d out of %d:", currentPage + 1, pageCount));

        long logCount = usageMap.keySet().stream()
            .skip((long) currentPage * commandsPerPage)
            .limit(commandsPerPage)
            .map(node -> {
                String smartUsage = usageMap.get(node)
                    .replace("[<", "§3[§7")
                    .replace(">]", "§3]")
                    .replace("<", "§a<§f")
                    .replace(">", "§a>");

                String message = "/" + smartUsage;
                String description = descriptions.getDescription(node.getCommand());
                if (description != null) {
                    message += "§e - " + description;
                }

                client.overlay.addChatMessage(message);
                return 1;
            }).reduce(0, Integer::sum);
        return (int) logCount;
    }
}
