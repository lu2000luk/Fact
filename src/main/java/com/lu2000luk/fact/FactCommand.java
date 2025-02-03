package com.lu2000luk.fact;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.List;

public class FactCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("fact").then(Commands.argument("number", IntegerArgumentType.integer(1, 10))
                .executes(commandContext -> execute(commandContext, IntegerArgumentType.getInteger(commandContext, "number")))
        ));
    }
    private static int execute(CommandContext<CommandSourceStack> command, int arg){
        if(command.getSource().getEntity() instanceof Player player){
            player.displayClientMessage(Component.literal("Fact >> Hello, " + player.getName()), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
