package pandorum.commands;

import arc.Core;
import arc.func.Cons;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.components.Config.Gamemode;
import pandorum.discord.Context;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.adminOnlyCommands;
import static pandorum.PluginVars.config;
import static pandorum.discord.Bot.adminCheck;
import static pandorum.util.Utils.adminCheck;
import static pandorum.util.Utils.bundled;

public class CommandsHelper {

    public static Seq<Command> getAvailableClientCommands(boolean admin) {
        return netServer.clientCommands.getCommandList().removeAll(command -> !admin && adminOnlyCommands.contains(command));
    }

    public static void removeCommand(CommandHandler handler, String text) {
        handler.removeCommand(text);
    }

    public static void registerClient(CommandHandler clientHandler, String text, String params, boolean adminOnly, Seq<Gamemode> modes, CommandRunner<Player> runner) {
        if (!modes.contains(config.mode)) return;
        Command command = clientHandler.<Player>register(text, params, Bundle.get(Strings.format("commands.@.description", text), Bundle.defaultLocale()), (args, player) -> {
            if (adminOnly && !adminCheck(player)) {
                bundled(player, "commands.permission-denied");
                return;
            }
            Core.app.post(() -> runner.accept(args, player));
        });

        if (adminOnly) adminOnlyCommands.add(command);
    }

    public static void registerClient(CommandHandler clientHandler, String text, String params, boolean adminOnly, CommandRunner<Player> runner) {
        registerClient(clientHandler, text, params, adminOnly, Seq.with(Gamemode.values()), runner);
    }

    public static void registerClient(CommandHandler clientHandler, String text, boolean adminOnly, Seq<Gamemode> modes, CommandRunner<Player> runner) {
        registerClient(clientHandler, text, "", adminOnly, modes, runner);
    }

    public static void registerClient(CommandHandler clientHandler, String text, boolean adminOnly, CommandRunner<Player> runner) {
        registerClient(clientHandler, text, "", adminOnly, runner);
    }

    public static void registerServer(CommandHandler serverHandler, String text, String params, String description, Cons<String[]> runner) {
        serverHandler.register(text, params, description, args -> Core.app.post(() -> runner.get(args)));
    }

    public static void registerServer(CommandHandler serverHandler, String text, String description, Cons<String[]> runner) {
        registerServer(serverHandler, text, "", description, runner);
    }

    public static void registerDiscord(CommandHandler discordHandler, String text, String params, String description, boolean adminOnly, CommandRunner<Context> runner) {
        discordHandler.<Context>register(text, params, description, (args, context) -> {
            if (adminOnly && !adminCheck(context.member)) {
                context.err(":no_entry: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
                return;
            }

            Core.app.post(() -> runner.accept(args, context));
        });
    }

    public static void registerDiscord(CommandHandler discordHandler, String text, String description, boolean adminOnly, CommandRunner<Context> runner) {
        registerDiscord(discordHandler, text, "", description, adminOnly, runner);
    }
}