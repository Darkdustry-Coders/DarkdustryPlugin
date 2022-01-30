package pandorum.commands;

import arc.util.CommandHandler;
import pandorum.commands.discord.*;

public class DiscordCommandsLoader {

    public static void registerDiscordCommands(CommandHandler handler) {
        CommandsHelper.registerDiscord(handler, "help", "Список команд.", false, HelpCommand::run);
        CommandsHelper.registerDiscord(handler, "ip", "Узнать IP адрес сервера.", false, IpCommand::run);
        CommandsHelper.registerDiscord(handler, "addmap", "Добавить карту на сервер.", true, AddMapCommand::run);
        CommandsHelper.registerDiscord(handler, "map", "<название...>", "Получить карту с сервера.", false, MapCommand::run);
        CommandsHelper.registerDiscord(handler, "removemap", "<название...>", "Удалить карту с сервера.", true, RemoveMapCommand::run);
        CommandsHelper.registerDiscord(handler, "maps", "[страница]", "Список всех карт сервера.", false, MapsListCommand::run);
        CommandsHelper.registerDiscord(handler, "players", "[страница]", "Посмотреть список игроков на сервере.", false, PlayersListCommand::run);
        CommandsHelper.registerDiscord(handler, "status", "Узнать статус сервера.", false, StatusCommand::run);
    }
}
