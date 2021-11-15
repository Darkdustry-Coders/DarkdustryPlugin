package pandorum.commands.client.player;

import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Player;
import pandorum.comp.Bundle;

import java.util.Objects;

import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class SavesListCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Fi> saves = Seq.with(Vars.saveDirectory.list()).filter(f -> Objects.equals(f.extension(), Vars.saveExtension));
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(saves.size / 6.0f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder();
        result.append(Bundle.format("commands.saves.page", findLocale(player.locale), page + 1, pages)).append("\n");
        for (int i = 6 * page; i < Math.min(6 * (page + 1), saves.size); i++) {
            result.append("[lightgray] ").append(i + 1).append("[orange] ").append(saves.get(i).nameWithoutExtension()).append("[white] ").append("\n");
        }

        player.sendMessage(result.toString());
    }
}
