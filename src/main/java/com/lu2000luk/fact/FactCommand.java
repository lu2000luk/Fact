package com.lu2000luk.fact;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.lu2000luk.fact.FactStore.*;

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
                                .then(Commands.literal("join")
                                        .then(Commands.argument("name", StringArgumentType.word())
                                                .executes(ctx -> adminJoin(ctx, StringArgumentType.getString(ctx, "name")))
                                        )
                                )
                                .then(Commands.literal("leave")
                                        .executes(FactCommand::adminLeave)
                                )
                ));
    }

    private static Player getPlayer(CommandContext<CommandSourceStack> command) {
        return command.getSource().getEntity() instanceof Player player ? player : null;
    }

    private static FactTeam getPlayerTeam(Player player) {
        return Arrays.stream(cachedTeams.toArray(new FactTeam[0])).filter(t -> Arrays.asList(t.getMembers()).contains(player.getStringUUID())).findFirst().orElse(null);
    }

    private static boolean isPlayerLeader(Player player, FactTeam team) {
        return team.getLeader().equals(player.getStringUUID());
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        Player player = getPlayer(command);
        if (player != null) {
            player.sendSystemMessage(Component.literal("Fact >> Hello, " + player.getStringUUID()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int createTeam(CommandContext<CommandSourceStack> command, String name) {
        try {
            Player player = getPlayer(command);

            if (player != null) {
                FactTeam team = new FactTeam();
                team.setName(name);

                if (getPlayerTeam(player) == null) {
                    team.setLeader(player.getStringUUID());
                    team.setMembers(new String[]{player.getStringUUID()});
                }

                List<FactTeam> teamList = getTeams();
                teamList.add(team);
                setTeams(teamList);

                player.sendSystemMessage(Component.literal("Fact >> Team " + name + " created. If you were not in a team, you are now the leader ."));
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to create team: ", e);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int deleteTeam(CommandContext<CommandSourceStack> command, String name) {
        Player player = getPlayer(command);
        if (player != null) {
            List<FactTeam> teamList = getTeams();
            FactTeam team = Arrays.stream(teamList.toArray(new FactTeam[0])).filter(t -> t.getName().equals(name)).findFirst().orElse(null);
            if (team != null) {
                teamList.remove(team);
                setTeams(teamList);
                player.sendSystemMessage(Component.literal("Fact >> Team " + name + " deleted."));
            } else {
                player.sendSystemMessage(Component.literal("Fact >> Team " + name + " not found."));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int adminLeave(CommandContext<CommandSourceStack> command) {
        // WARNING: Doesn't work lol (Also code is ass).

        Player player = getPlayer(command);
        if (player != null) {
            FactTeam team = getPlayerTeam(player);
            if (team != null) {
                List<FactTeam> teamList = getTeams();
                teamList.remove(team);

                team.setMembers(Arrays.stream(team.getMembers()).filter(m -> !m.equals(player.getStringUUID())).toArray(String[]::new));

                if (team.getLeader().equals(player.getStringUUID())) {
                    team.setLeader("Unknown");
                }

                teamList.add(team);

                setTeams(teamList);

                player.sendSystemMessage(Component.literal("Fact >> Left team " + team.getName()));
            } else {
                player.sendSystemMessage(Component.literal("Fact >> You are not in a team."));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int adminJoin(CommandContext<CommandSourceStack> command, String name) {
        Player player = getPlayer(command);

        if (player != null) {
            FactTeam team = getPlayerTeam(player);
            if (team != null) {
                player.sendSystemMessage(Component.literal("Fact >> You are already in a team."));
                return Command.SINGLE_SUCCESS;
            }

            List<FactTeam> teamList = getTeams();
            FactTeam newTeam = Arrays.stream(teamList.toArray(new FactTeam[0])).filter(t -> t.getName().equals(name)).findFirst().orElse(null);
            teamList.remove(newTeam);

            if (newTeam != null) {
                newTeam.setMembers(Arrays.copyOf(newTeam.getMembers(), newTeam.getMembers().length + 1));
                newTeam.getMembers()[newTeam.getMembers().length - 1] = player.getStringUUID();
                teamList.add(newTeam);
                setTeams(teamList);
                player.sendSystemMessage(Component.literal("Fact >> Joined team " + newTeam.getName()));
            } else {
                player.sendSystemMessage(Component.literal("Fact >> Team " + name + " not found."));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int claimChunk(CommandContext<CommandSourceStack> command, String name) {


        return Command.SINGLE_SUCCESS;
    }

    private static int unclaimChunk(CommandContext<CommandSourceStack> command, String name) {


        return Command.SINGLE_SUCCESS;
    }

    private static int setLeader(CommandContext<CommandSourceStack> command, Player player) {
        if (getPlayer(command) == null) {
            command.getSource().sendFailure(Component.literal("Fact >> Player not found."));
            return Command.SINGLE_SUCCESS;
        }

        FactTeam team = getPlayerTeam(getPlayer(command));
        if (team != null) {
            if (isPlayerLeader(Objects.requireNonNull(getPlayer(command)), team)) {
                team.setLeader(player.getStringUUID());
                player.sendSystemMessage(Component.literal("Fact >> Made " + player.getName().getString() + " leader of " + team.getName()));
            } else {
                player.sendSystemMessage(Component.literal("Fact >> You are not the leader of " + team.getName()));
            }
        } else {
            player.sendSystemMessage(Component.literal("Fact >> You are not in a team."));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int ally(CommandContext<CommandSourceStack> command, String name) {
        if (getPlayer(command) == null) {
            command.getSource().sendFailure(Component.literal("Fact >> Player not found."));
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int unally(CommandContext<CommandSourceStack> command, String name) {

        return Command.SINGLE_SUCCESS;
    }
}
