package pandorum.commands.client;

import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import pandorum.comp.Bundle;

import static mindustry.Vars.saveDirectory;
import static pandorum.utils.Utils.bundled;
import static pandorum.utils.Search.findLocale;

public class SavesListCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid);
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(savesList.size / 8f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder(Bundle.format("commands.saves.page", findLocale(player.locale), page + 1, pages));

        for (int i = 8 * page; i < Math.min(8 * (page + 1), savesList.size); i++) {
            Fi save = savesList.get(i);
            result.append("\n[lightgray] ").append(i + 1).append(". [orange]").append(save.nameWithoutExtension());
        }

        player.sendMessage(result.toString());
    }
}
