package com.lu2000luk.fact;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.List;

public class FactCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fact")
                .requires(cs -> cs.hasPermission(0))
                .executes(FactCommand::execute)
                .then(Commands.literal("setLeader")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> setLeader(ctx, EntityArgument.getPlayer(ctx, "player")))
                        )
                )
                .then(Commands.literal("ally")
                        .then(Commands.argument("other team", StringArgumentType.word())
                                .executes(ctx -> ally(ctx, StringArgumentType.getString(ctx, "other team")))
                        )
                )
                .then(Commands.literal("unally")
                        .then(Commands.argument("other team", StringArgumentType.word())
                                .executes(ctx -> unally(ctx, StringArgumentType.getString(ctx, "other team")))
                        )
                )
                .then(
                        Commands.literal("admin")
                                .requires(cs -> cs.hasPermission(4))
                                .then(Commands.literal("create")
                                        .then(Commands.argument("name", StringArgumentType.word())
                                                .executes(ctx -> createTeam(ctx, StringArgumentType.getString(ctx, "name")))
                                        )
                                )
                                .then(Commands.literal("delete")
                                        .then(Commands.argument("name", StringArgumentType.word())
                                                .executes(ctx -> deleteTeam(ctx, StringArgumentType.getString(ctx, "name")))
                                        )
                                )
                                .then(Commands.literal("claim").then(Commands.argument("as", StringArgumentType.word())
                                        .executes(ctx -> claimChunk(ctx, StringArgumentType.getString(ctx, "as")))
                                ))
                                .then(Commands.literal("unclaim").then(Commands.argument("as", StringArgumentType.word())
                                        .executes(ctx -> unclaimChunk(ctx, StringArgumentType.getString(ctx, "as")))
                                ))
                ));
    }

    private static Player getPlayer(CommandContext<CommandSourceStack> command) {
        return command.getSource().getEntity() instanceof Player player ? player : null;
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        Player player = getPlayer(command);
        if (player != null) {
            player.sendSystemMessage(Component.literal("Fact >> Hello, " + player.getStringUUID()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int createTeam(CommandContext<CommandSourceStack> command, String name) {


        return Command.SINGLE_SUCCESS;
    }

    private static int deleteTeam(CommandContext<CommandSourceStack> command, String name) {


        return Command.SINGLE_SUCCESS;
    }

    private static int claimChunk(CommandContext<CommandSourceStack> command, String name) {


        return Command.SINGLE_SUCCESS;
    }

    private static int unclaimChunk(CommandContext<CommandSourceStack> command, String name) {


        return Command.SINGLE_SUCCESS;
    }

    private static int setLeader(CommandContext<CommandSourceStack> command, Player player) {

        return Command.SINGLE_SUCCESS;
    }

    private static int ally(CommandContext<CommandSourceStack> command, String name) {

        return Command.SINGLE_SUCCESS;
    }

    private static int unally(CommandContext<CommandSourceStack> command, String name) {

        return Command.SINGLE_SUCCESS;
    }
}
