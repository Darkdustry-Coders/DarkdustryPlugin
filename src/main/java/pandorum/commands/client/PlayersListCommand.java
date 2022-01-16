package pandorum.commands.client;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.Player;
import pandorum.comp.Bundle;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class PlayersListCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Player> playersList = Groups.player.copy(new Seq<>());
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(playersList.size / 6.0f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder(Bundle.format("commands.players.page", findLocale(player.locale), page + 1, pages));

        for (int i = 6 * page; i < Math.min(6 * (page + 1), playersList.size); i++) {
            result.append("\n[#9c88ee]* [white]");
            Player p = playersList.get(i);
            if (p.admin) result.append(Iconc.admin).append(" ");
            result.append(p.coloredName()).append("[accent] / [cyan]").append(Bundle.format("commands.players.id", findLocale(player.locale), p.id)).append("[accent] / [cyan]").append(Bundle.format("commands.players.locale", findLocale(player.locale), p.locale));
        }

        player.sendMessage(result.toString());
    }
}
