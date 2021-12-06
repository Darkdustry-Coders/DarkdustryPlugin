package pandorum.commands.client;

import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.gamemodes.RequireSimpleGamemode;
import pandorum.comp.Bundle;

import java.util.Objects;

import static mindustry.Vars.saveDirectory;
import static mindustry.Vars.saveExtension;
import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class SavesListCommand {
    @RequireSimpleGamemode
    @ClientCommand(name = "saves", args = "[page]", description = "List of all saves.", admin = false)
    public static void run(final String[] args, final Player player) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            bundled(player, "commands.page-not-int");
            return;
        }

        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(f -> Objects.equals(f.extension(), saveExtension));
        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(savesList.size / 6.0f);

        if (--page >= pages || page < 0) {
            bundled(player, "commands.under-page", pages);
            return;
        }

        StringBuilder result = new StringBuilder(Bundle.format("commands.saves.page", findLocale(player.locale), page + 1, pages)).append("\n");

        for (int i = 6 * page; i < Math.min(6 * (page + 1), savesList.size); i++) {
            Fi save = savesList.get(i);
            result.append("[lightgray] ").append(i + 1).append(". [orange]").append(save.nameWithoutExtension()).append("\n");
        }

        player.sendMessage(result.toString());
    }
}
