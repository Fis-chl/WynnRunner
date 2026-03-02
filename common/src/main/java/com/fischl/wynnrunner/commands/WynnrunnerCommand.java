/*
 * Copyright © Wynnrunner 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.commands;

import com.fischl.wynnrunner.Wynnrunner;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.consumers.commands.Command;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class WynnrunnerCommand extends Command {
    private static final SuggestionProvider<CommandSourceStack> WYNNRUNNER_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(Stream.of("version"), builder);

    public void registerWithCommands(
            Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer,
            CommandBuildContext context,
            List<Command> commands) {
        List<LiteralArgumentBuilder<CommandSourceStack>> commandBuilders = getCommandBuilders(context);

        // Also register all our commands as subcommands under the wynntils command and it's aliases
        for (LiteralArgumentBuilder<CommandSourceStack> builder : commandBuilders) {
            for (Command commandInstance : commands) {
                if (commandInstance == this) continue;

                commandInstance.getCommandBuilders(context).forEach(builder::then);
            }

            consumer.accept(builder);
        }
    }

    @Override
    public String getCommandName() {
        return "wynnrunner";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext context) {
        return base.then(Commands.literal("version").executes(this::version));
    }

    private int version(CommandContext<CommandSourceStack> context) {
        MutableComponent response = Component.literal("Wynnrunner version: ").withStyle(ChatFormatting.GREEN);
        response.append(Component.literal(Wynnrunner.getVersion()).withStyle(ChatFormatting.AQUA));
        if (Wynnrunner.isDevelopmentBuild()) {
            response.append(Component.literal(" (development build)").withStyle(ChatFormatting.YELLOW));
        }
        context.getSource().sendSuccess(() -> response, false);
        return 1;
    }
}
