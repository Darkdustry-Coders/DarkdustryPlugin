package pandorum.commands;

import arc.Core;
import arc.util.CommandHandler;
import arc.util.CommandHandler.*;
import pandorum.discord.Context;

import static pandorum.PluginVars.discordAdminOnlyCommands;
import static pandorum.util.Utils.adminCheck;

public class DiscordCommandsHandler {

    public CommandHandler handler;

    public DiscordCommandsHandler(CommandHandler handler) {
        this.handler = handler;
    }

    public void register(String text, String params, String description, boolean adminOnly, CommandRunner<Context> runner) {
        Command command = handler.<Context>register(text, params, description, (args, context) -> {
            if (adminOnly && !adminCheck(context.member)) {
                context.err(":no_entry_sign: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
                return;
            }
            Core.app.post(() -> runner.accept(args, context));
        });

        if (adminOnly) discordAdminOnlyCommands.add(command);
    }

    public void register(String text, String description, boolean adminOnly, CommandRunner<Context> runner) {
        register(text, "", description, adminOnly, runner);
    }

    public void removeCommand(String text) {
        handler.removeCommand(text);
    }
}
