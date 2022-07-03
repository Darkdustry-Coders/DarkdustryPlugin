package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import arc.util.Strings;

import static mindustry.Vars.netServer;

public class PlayerLimitCommand implements Cons<String[]> {
    public void get(String[] args) {
        if (args.length == 0) {
            Log.info("Текущий лимит игроков: @.", netServer.admins.getPlayerLimit() == 0 ? "0 (отключен)" : netServer.admins.getPlayerLimit());
            return;
        }

        if (args[0].equals("off")) {
            netServer.admins.setPlayerLimit(0);
            Log.info("Лимит игроков отключен.");
            return;
        }

        if (Strings.canParsePositiveInt(args[0])) {
            int limit = Strings.parseInt(args[0]);
            netServer.admins.setPlayerLimit(limit);
            Log.info("Новый лимит игроков: @.", limit);
        } else {
            Log.err("Лимит игроков должен быть положительным числом.");
        }
    }
}
