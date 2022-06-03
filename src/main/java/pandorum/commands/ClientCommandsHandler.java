package pandorum.commands;

import arc.Core;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.components.Gamemode;
import pandorum.util.Utils;

import static pandorum.PluginVars.clientAdminOnlyCommands;
import static pandorum.PluginVars.config;
import static pandorum.util.Utils.bundled;

public class ClientCommandsHandler {

    public CommandHandler handler;

    public ClientCommandsHandler(CommandHandler handler) {
        this.handler = handler;
    }

    public void register(String text, String params, boolean adminOnly, Seq<Gamemode> modes, CommandRunner<Player> runner) {
        if (!modes.contains(config.mode)) return;
        Command command = handler.<Player>register(text, params, Bundle.get(Strings.format("commands.@.description", text), Bundle.defaultLocale()), (args, player) -> {
            if (adminOnly && !Utils.isAdmin(player)) {
                bundled(player, "commands.permission-denied");
                return;
            }
            Core.app.post(() -> runner.accept(args, player));
        });

        if (adminOnly) clientAdminOnlyCommands.add(command);
    }

    public void register(String text, String params, boolean adminOnly, CommandRunner<Player> runner) {
        register(text, params, adminOnly, Seq.with(Gamemode.values()), runner);
    }

    public void register(String text, boolean adminOnly, Seq<Gamemode> modes, CommandRunner<Player> runner) {
        register(text, "", adminOnly, modes, runner);
    }

    public void register(String text, boolean adminOnly, CommandRunner<Player> runner) {
        register(text, "", adminOnly, runner);
    }

    public void removeCommand(String text) {
        handler.removeCommand(text);
    }
}
