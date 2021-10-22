package pandorum.commands.client;

import arc.math.Mathf;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Bundle;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class PlayerListCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil((float) Groups.player.size() / 6.0f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder();
        result.append(Bundle.format("commands.pl.page", findLocale(player.locale), page + 1, pages)).append("\n");

        for (int i = 6 * page; i < Math.min(6 * (page + 1), Groups.player.size()); i++) {
            Player p = Groups.player.index(i);
            result.append("[#9c88ee]* [white]").append(p.admin ? Iconc.admin  + " " : "").append(Misc.colorizedName(p)).append(" [accent]/ [cyan]ID: ").append(p.id()).append(Bundle.format("commands.pl.locale", findLocale(player.locale), p.locale)).append("\n");
        }
        player.sendMessage(result.toString());
    }
}
