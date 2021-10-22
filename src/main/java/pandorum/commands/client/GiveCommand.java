package pandorum.commands.client;

import arc.util.Strings;
import mindustry.game.Teams;
import mindustry.gen.Player;
import mindustry.type.Item;
import pandorum.Misc;

import static mindustry.Vars.content;
import static mindustry.Vars.state;
import static pandorum.Misc.bundled;

public class GiveCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;

        if (args.length > 1 && !Strings.canParseInt(args[1])) {
            bundled(player, "commands.non-int");
            return;
        }

        int count = args.length > 1 ? Strings.parseInt(args[1]) : 1000;

        Item item = content.items().find(b -> b.name.equalsIgnoreCase(args[0]));
        if (item == null) {
            bundled(player, "commands.admin.give.item-not-found");
            return;
        }

        Teams.TeamData team = state.teams.get(player.team());
        if (!team.hasCore()) {
            bundled(player, "commands.admin.give.core-not-found");
            return;
        }

        team.core().items.add(item, count);
        bundled(player, "commands.admin.give.success");
    }
}
