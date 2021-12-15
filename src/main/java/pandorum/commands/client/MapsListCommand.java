package pandorum.commands.client;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.gamemodes.RequireSimpleGamemode;
import pandorum.comp.Bundle;

import static mindustry.Vars.maps;
import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class MapsListCommand {
    @RequireSimpleGamemode
    @ClientCommand(name = "maps", args = "[page]", description = "List of all maps.")
    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Map> mapsList = maps.customMaps();
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(mapsList.size / 6.0f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder(Bundle.format("commands.maps.page", findLocale(player.locale), page + 1, pages)).append("\n");

        for (int i = 6 * page; i < Math.min(6 * (page + 1), mapsList.size); i++) {
            Map map = mapsList.get(i);
            result.append("[lightgray] ").append(i + 1).append(". [orange]").append(map.name()).append("\n");
        }

        player.sendMessage(result.toString());
    }
}
