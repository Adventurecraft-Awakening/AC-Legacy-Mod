package dev.adventurecraft.awakening.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import dev.adventurecraft.awakening.common.gui.AC_GuiMapEditHUD;
import dev.adventurecraft.awakening.common.gui.AC_GuiScriptStats;
import dev.adventurecraft.awakening.common.gui.AC_GuiWorldConfig;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static dev.adventurecraft.awakening.common.CommandUtils.*;

public class ServerCommands {

    public static final String DESCRIPTION_COLOR = "§e";

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
    }

    public static void registerCommandsWithArgs(
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandDescriptions descs) {

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

        dispatcher.register(optionalArg(literal("togglebonemeal"),
            "value", BoolArgumentType.bool(),
            descs.attach(ServerCommands::cmdToggleBonemeal, "Toggles bonemeal usage outside of Debug Mode")));

        dispatcher.register(optionalArg(literal("mobsburn"),
            "value", BoolArgumentType.bool(),
            descs.attach(ServerCommands::cmdMobsBurn, "Toggles mobs burning in daylight")));

        dispatcher.register(requiredArg(literal("cameraadd"),
            "time", FloatArgumentType.floatArg(),
            ServerCommands::cmdCameraAdd));

        {
            var node = dispatcher.register(optionalArg(
                literal("help"),
                "page",
                IntegerArgumentType.integer(1),
                descs.attach(
                    (ctx, page) -> ServerCommands.cmdHelp(ctx, dispatcher, descs, page),
                    "Gets the first page of available commands")));
            descs.attach(node.getChild("page").getCommand(), "Gets a page of available commands");
        }
        {
            var node = dispatcher.register(requiredArg(
                literal("help"),
                "path",
                StringArgumentType.greedyString(),
                (ctx, name) -> ServerCommands.cmdHelp(ctx, dispatcher, descs, name)));
            descs.attach(node.getChild("path").getCommand(), "Gets the description of a command node");
        }

        // TODO: save/restore for undostacks
        dispatcher.register(literal("undostack")
            .executes(descs.attach(ServerCommands::cmdUndoStack, "Gets info about the undo stack"))
            .then(literal("clear")
                .executes(descs.attach(ServerCommands::cmdUndoStackClear, "Clears the undo stack"))));
    }

    public static int cmdConfig(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            source.getClient().setScreen(new AC_GuiWorldConfig(world));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdTest(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            source.getClient().setScreen(new AC_GuiMapEditHUD(world));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdScriptStats(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            source.getClient().setScreen(new AC_GuiScriptStats(world));
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
                if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                    mobCount++;
                    if (!entity.removed) {
                        entity.remove();
                        count++;
                    }
                }
            }

            source.getClient().gui.addMessage(String.format("Removed %d out of %d mobs", count, mobCount));
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

            client.gui.addMessage(String.format("Cleared %d camera points", pointCount));
            return pointCount;
        }
        client.gui.addMessage("Need to be editing a camera block");
        return 0;
    }

    public static int cmdFullBright(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            for (int i = 0; i < 16; ++i) {
                world.dimension.brightnessRamp[i] = 1.0F;
            }
            ((ExWorldEventRenderer) source.getClient().levelRenderer).updateAllTheRenderers();

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdScriptStatReset(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world instanceof ExWorld exWorld) {
            var infos = exWorld.getScriptHandler().getScripts();
            int count = infos.size();
            for (AC_JScriptInfo info : infos) {
                info.clear();
            }

            source.getClient().gui.addMessage(String.format("Reset %d script stats", count));
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

            source.getClient().gui.addMessage(String.format(
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

            source.getClient().gui.addMessage(String.format(
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

        client.gui.addMessage(String.format("Level Editing: %b", AC_DebugMode.levelEditing));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdRenderPaths(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.renderPaths = value != null ? value : !AC_DebugMode.renderPaths;

        client.gui.addMessage(String.format("Render Paths: %b", AC_DebugMode.renderPaths));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdRenderFov(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.renderFov = value != null ? value : !AC_DebugMode.renderFov;

        client.gui.addMessage(String.format("Render FOV: %b", AC_DebugMode.renderFov));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdRenderCollisions(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.renderCollisions = value != null ? value : !AC_DebugMode.renderCollisions;

        client.gui.addMessage(String.format("Render Collisions: %b", AC_DebugMode.renderCollisions));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdRenderRays(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.renderRays = value != null ? value : !AC_DebugMode.renderRays;

        client.gui.addMessage(String.format("Render Rays: %b", AC_DebugMode.renderRays));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdFluidCollision(CommandContext<ServerCommandSource> context, Boolean value) {
        var client = context.getSource().getClient();
        AC_DebugMode.isFluidHittable = value != null ? value : !AC_DebugMode.isFluidHittable;

        client.gui.addMessage(String.format("Fluid Collision: %b", AC_DebugMode.isFluidHittable));
        return Command.SINGLE_SUCCESS;
    }

    public static int cmdFly(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var entity = source.getEntity();
        if (entity instanceof ExEntity exEntity) {
            exEntity.setIsFlying(value != null ? value : !exEntity.handleFlying());

            source.getClient().gui.addMessage(String.format("Flying: %b", exEntity.handleFlying()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdNoClip(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var entity = source.getEntity();
        if (entity instanceof ExEntity exEntity) {
            entity.noPhysics = value != null ? value : !entity.noPhysics;
            if (entity.noPhysics) {
                exEntity.setIsFlying(true);
            }

            source.getClient().gui.addMessage(String.format("NoClip: %b", entity.noPhysics));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdToggleMelting(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            var props = (ExWorldProperties) world.levelData;
            props.setIceMelts(value != null ? value : !props.getIceMelts());

            source.getClient().gui.addMessage(String.format("Ice Melts: %b", props.getIceMelts()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdToggleDecay(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            var props = (ExWorldProperties) world.levelData;
            props.setLeavesDecay(value != null ? value : !props.getLeavesDecay());

            source.getClient().gui.addMessage(String.format("Leaves Decay: %b", props.getLeavesDecay()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdToggleBonemeal(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            var props = (ExWorldProperties) world.levelData;
            props.setCanUseBonemeal(value != null ? value : !props.getCanUseBonemeal());

            source.getClient().gui.addMessage(String.format("Bonemeal usable without Debug Mode: %b", props.getCanUseBonemeal()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdMobsBurn(CommandContext<ServerCommandSource> context, Boolean value) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world != null) {
            var props = (ExWorldProperties) world.levelData;
            props.setMobsBurn(value != null ? value : !props.getMobsBurn());

            source.getClient().gui.addMessage(String.format("Mobs Burn in Daylight: %b", props.getMobsBurn()));
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
            float y = (float) (entity.y - (double) entity.heightOffset + 1.62D);
            float z = (float) entity.z;
            activeCamera.addCameraPoint(time, x, y, z, entity.yRot, entity.xRot, AC_CutsceneCameraBlendType.QUADRATIC);
            activeCamera.loadCameraEntities();

            client.gui.addMessage("Camera point added");
            return Command.SINGLE_SUCCESS;
        }
        client.gui.addMessage("Need to be editing a camera block");
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
        client.gui.addMessage(String.format("§2Help page %d out of %d:", currentPage + 1, pageCount));

        int logCount = usageMap.keySet().stream()
            .skip((long) currentPage * commandsPerPage)
            .limit(commandsPerPage)
            .map(node -> {
                var lines = new ArrayList<String>();

                String message = "/" + prettifyUsage(usageMap.get(node));
                String description = descriptions.getDescription(node.getCommand());
                if (description != null) {
                    message += DESCRIPTION_COLOR + " - " + description;
                }
                lines.add(message);

                var children = node.getChildren();
                for (var child : children) {
                    createCommandTree(child, descriptions, "  ", lines);
                }

                client.gui.addMessage(String.join("\n", lines));
                return 1;
            }).reduce(0, Integer::sum);
        return logCount;
    }

    private static void createCommandTree(
        CommandNode<ServerCommandSource> node,
        CommandDescriptions descriptions,
        String prefix,
        List<String> output) {

        var children = node.getChildren();
        for (var child : children) {
            createCommandTree(child, descriptions, "  " + prefix, output);
        }

        String description = descriptions.getDescription(node.getCommand());
        if (description != null) {
            String message = prefix + "§f" + prettifyUsage(node.getUsageText());
            message += DESCRIPTION_COLOR + " - " + description;
            output.add(message);
        }
    }

    private static String prettifyUsage(String value) {
        return value
            .replace("<", "§a<§7")
            .replace(">", "§a>§f");
    }

    public static int cmdHelp(
        CommandContext<ServerCommandSource> context,
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandDescriptions descriptions,
        String path) {

        var source = context.getSource();
        var client = source.getClient();
        var lines = new ArrayList<String>();
        int result;

        var rootNode = dispatcher.findNode(Arrays.asList(path.split(" ")));
        if (rootNode != null) {
            String rootDesc = descriptions.getDescription(rootNode.getCommand());
            String rootDescC = rootDesc != null ? DESCRIPTION_COLOR + " - " + rootDesc : "";
            lines.add(String.format("§2Command help for \"§f%s§2\"%s", path, rootDescC));

            var usageMap = dispatcher.getSmartUsage(rootNode, source);
            result = usageMap.keySet().stream()
                .map(node -> {
                    String line = prettifyUsage(usageMap.get(node));
                    String description = descriptions.getDescription(node.getCommand());
                    if (description != null) {
                        line += DESCRIPTION_COLOR + " - " + description;
                    }
                    lines.add(line);
                    return 1;
                }).reduce(0, Integer::sum);
        } else {
            lines.add(String.format("§cNo command node for \"§f%s§c\"", path));
            result = 0;
        }
        client.gui.addMessage(String.join("\n", lines));
        return result;
    }

    public static int cmdUndoStack(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world instanceof ExWorld exWorld) {
            var undoStack = exWorld.getUndoStack();

            source.getClient().gui.addMessage(String.format(
                "Undos left: %d, Redos left: %d",
                undoStack.undoStack.size(),
                undoStack.redoStack.size()));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public static int cmdUndoStackClear(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        if (world instanceof ExWorld exWorld) {
            var undoStack = exWorld.getUndoStack();
            int undoCount = undoStack.undoStack.size();
            int redoCount = undoStack.redoStack.size();
            undoStack.clear();

            source.getClient().gui.addMessage(String.format(
                "Undos cleared: %d, Redos cleared: %d", undoCount, redoCount));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}
