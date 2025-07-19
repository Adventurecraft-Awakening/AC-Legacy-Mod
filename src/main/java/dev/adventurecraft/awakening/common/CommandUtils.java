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
        Class<?> result = switch (argumentType) {
            case FloatArgumentType ignored -> Float.class;
            case DoubleArgumentType ignored -> Double.class;
            case IntegerArgumentType ignored -> Integer.class;
            case LongArgumentType ignored -> Long.class;
            case BoolArgumentType ignored -> Boolean.class;
            case StringArgumentType ignored -> String.class;
            default -> throw new IllegalArgumentException();
        };
        //noinspection unchecked
        return (Class<T>) result;
    }

    public static <T> String getShortName(Class<T> cls) {
        if (cls.equals(Float.class)) {
            return "f32";
        }
        else if (cls.equals(Double.class)) {
            return "f64";
        }
        else if (cls.equals((Integer.class))) {
            return "i32";
        }
        else if (cls.equals(Long.class)) {
            return "i64";
        }
        else if (cls.equals(Boolean.class)) {
            return "bool";
        }
        else if (cls.equals(String.class)) {
            return "str";
        }
        return cls.getSimpleName();
    }

    public static <T> LiteralArgumentBuilder<ServerCommandSource> optionalArg(
        LiteralArgumentBuilder<ServerCommandSource> builder,
        String argName,
        ArgumentType<T> argType,
        CommandOpt<ServerCommandSource, T> command
    ) {
        return requiredArg(builder, argName, argType, command).executes(command);
    }

    public static <T> LiteralArgumentBuilder<ServerCommandSource> requiredArg(
        LiteralArgumentBuilder<ServerCommandSource> builder,
        String argName,
        ArgumentType<T> argType,
        CommandOpt<ServerCommandSource, T> command
    ) {
        return builder.then(argument(argName, argType).executes(ctx -> command.run(
            ctx,
            ctx.getArgument(argName, getClass(argType))
        )));
    }

    @FunctionalInterface
    public interface CommandOpt<S, T> extends Command<S> {

        int run(CommandContext<S> context, T argument)
            throws CommandSyntaxException;

        @Override
        default int run(CommandContext<S> context)
            throws CommandSyntaxException {
            return this.run(context, null);
        }
    }
}
