package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class WhiteListCommand implements Cons<String[]> {
    public void get(String[] args) {
        if (!netServer.admins.isWhitelistEnabled()) {
            Log.err("Белый список отключен.");
            return;
        }

        if (args.length == 0) {
            Seq<PlayerInfo> whitelist = netServer.admins.getWhitelisted();
            if (whitelist.isEmpty()) {
                Log.info("В белом списке нет игроков.");
            } else {
                Log.info("Белый список сервера: (@)", whitelist.size);
                whitelist.each(info -> Log.info("  - Никнейм: @ / UUID: @", info.lastName, info.id));
            }
        } else if (args.length == 1) {
            Log.err("Не хватает последнего аргумента. Необходимо указать UUID игрока.");
        } else if (args.length == 2) {
            PlayerInfo info = netServer.admins.getInfoOptional(args[1]);
            if (info == null) {
                Log.err("Игрок с таким UUID не найден.");
                return;
            }

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    netServer.admins.whitelist(info.id);
                    Log.info("Игрок '@' добавлен в белый список.", info.lastName);
                }
                case "remove" -> {
                    netServer.admins.unwhitelist(info.id);
                    Log.info("Игрок '@' удален из белого списка.", info.lastName);
                }
                default -> Log.err("Второй параметр должен быть или 'add' или 'remove'.");
            }
        }
    }
}
