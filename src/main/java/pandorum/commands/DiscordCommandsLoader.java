package pandorum.commands;

import arc.Core;
import arc.util.CommandHandler;
import pandorum.commands.discord.*;
import pandorum.discord.Context;

import static pandorum.util.Utils.adminCheck;

public class DiscordCommandsLoader {

    public static void registerDiscordCommands(CommandHandler handler) {
        registerDiscord(handler, "help", "Список всех команд.", false, HelpCommand::run);
        registerDiscord(handler, "ip", "IP адрес сервера.", false, IpCommand::run);
        registerDiscord(handler, "addmap", "Добавить карту на сервер.", true, AddMapCommand::run);
        registerDiscord(handler, "map", "<название...>", "Получить карту с сервера.", false, MapCommand::run);
        registerDiscord(handler, "removemap", "<название...>", "Удалить карту с сервера.", true, RemoveMapCommand::run);
        registerDiscord(handler, "maps", "[страница]", "Список карт сервера.", false, MapsListCommand::run);
        registerDiscord(handler, "players", "[страница]", "Список игроков сервера.", false, PlayersListCommand::run);
        registerDiscord(handler, "status", "Состояние сервера.", false, StatusCommand::run);
    }

    public static void registerDiscord(CommandHandler discordHandler, String text, String params, String description, boolean adminOnly, CommandHandler.CommandRunner<Context> runner) {
        discordHandler.<Context>register(text, params, description, (args, context) -> {
            if (adminOnly && !adminCheck(context.member)) {
                context.err(":no_entry: Эта команда только для администрации.", "У тебя нет прав на ее использование.");
                return;
            }
            Core.app.post(() -> runner.accept(args, context));
        });
    }

    public static void registerDiscord(CommandHandler discordHandler, String text, String description, boolean adminOnly, CommandHandler.CommandRunner<Context> runner) {
        registerDiscord(discordHandler, text, "", description, adminOnly, runner);
    }
}
