package pandorum.commands.client;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.comp.Bundle;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class MapListCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Map> mapList = Vars.maps.customMaps();
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(mapList.size / 6.0f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder();
        result.append(Bundle.format("commands.maps.page", findLocale(player.locale), page + 1, pages)).append("\n");
        for (int i = 6 * page; i < Math.min(6 * (page + 1), mapList.size); i++) {
            result.append("[lightgray] ").append(i + 1).append("[orange] ").append(mapList.get(i).name()).append("[white] ").append("\n");
        }

        player.sendMessage(result.toString());
    }
}
