package dev.adventurecraft.awakening.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public final class CommandUtils {

    public static LiteralArgumentBuilder<ServerCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static <T> Class<T> getClass(ArgumentType<T> argumentType) {
        Class<?> result;
        if (argumentType instanceof FloatArgumentType) {
            result = Float.class;
        } else if (argumentType instanceof DoubleArgumentType) {
            result = Double.class;
        } else if (argumentType instanceof IntegerArgumentType) {
            result = Integer.class;
        } else if (argumentType instanceof LongArgumentType) {
            result = Long.class;
        } else if (argumentType instanceof BoolArgumentType) {
            result = Boolean.class;
        } else if (argumentType instanceof StringArgumentType) {
            result = String.class;
        } else {
            result = null;
        }
        if (result == null) {
            throw new IllegalArgumentException();
        }
        //noinspection unchecked
        return (Class<T>) result;
    }

    public static <T> LiteralArgumentBuilder<ServerCommandSource> optionalArg(
        LiteralArgumentBuilder<ServerCommandSource> builder,
        String argName,
        ArgumentType<T> argumentType,
        CommandOpt<ServerCommandSource, T> command) {

        Class<T> argClass = getClass(argumentType);
        builder
            .then(argument(argName, argumentType)
                .executes(ctx -> command.run(ctx, ctx.getArgument(argName, argClass))))
            .executes(command);
        return builder;
    }

    public static <T> LiteralArgumentBuilder<ServerCommandSource> requiredArg(
        LiteralArgumentBuilder<ServerCommandSource> builder,
        String argName,
        ArgumentType<T> argumentType,
        CommandOpt<ServerCommandSource, T> command) {

        Class<T> argClass = getClass(argumentType);
        builder
            .then(argument(argName, argumentType)
                .executes(ctx -> command.run(ctx, ctx.getArgument(argName, argClass))));
        return builder;
    }

    @FunctionalInterface
    public interface CommandOpt<S, T> extends Command<S> {

        int run(CommandContext<S> context, T argument) throws CommandSyntaxException;

        @Override
        default int run(CommandContext<S> context) throws CommandSyntaxException {
            return this.run(context, null);
        }
    }
}
