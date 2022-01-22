package pandorum.commands;

import arc.Core;
import arc.func.Cons;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import discord4j.core.object.entity.Message;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.comp.Config.Gamemode;

import static mindustry.Vars.netServer;
import static pandorum.Misc.adminCheck;
import static pandorum.PluginVars.adminOnlyCommands;
import static pandorum.PluginVars.config;
import static pandorum.discord.BotHandler.adminCheck;
import static pandorum.discord.BotHandler.err;

public class CommandsHelper {

    public static Seq<Command> getAvailableClientCommands(boolean admin) {
        return netServer.clientCommands.getCommandList().removeAll(command -> !admin && adminOnlyCommands.contains(command));
    }

    public static void removeCommand(CommandHandler handler, String text) {
        handler.removeCommand(text);
    }

    /**
     * Методы для команд для игроков.
     */

    public static void registerClient(CommandHandler clientHandler, String text, String params, boolean adminOnly, Seq<Gamemode> modes, CommandRunner<Player> runner) {
        if (!modes.contains(config.mode)) return;
        Command command = clientHandler.<Player>register(text, params, Bundle.get(Strings.format("commands.@.description", text), Bundle.defaultLocale()), (args, player) -> {
            if (adminOnly && adminCheck(player)) return;
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

    /**
     * Методы для команд для консоли.
     */

    public static void registerServer(CommandHandler serverHandler, String text, String params, String description, Cons<String[]> runner) {
        serverHandler.register(text, params, description, args -> Core.app.post(() -> runner.get(args)));
    }

    public static void registerServer(CommandHandler serverHandler, String text, String description, Cons<String[]> runner) {
        registerServer(serverHandler, text, "", description, runner);
    }

    /**
     * Методы для команд для Discord.
     */

    public static void registerDiscord(CommandHandler discordHandler, String text, String params, String description, boolean adminOnly, CommandRunner<Message> runner) {
        discordHandler.<Message>register(text, params, description, (args, message) -> {
            if (adminOnly && adminCheck(message.getAuthorAsMember().block())) {
                err(message.getChannel().block(), "Эта команда недоступна для тебя.", "У тебя нет прав на ее использование.");
                return;
            }

            Core.app.post(() -> runner.accept(args, message));
        });
    }

    public static void registerDiscord(CommandHandler discordHandler, String text, String description, boolean adminOnly, CommandRunner<Message> runner) {
        registerDiscord(discordHandler, text, "", description, adminOnly, runner);
    }
}