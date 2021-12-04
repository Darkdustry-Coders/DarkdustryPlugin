package pandorum.commands.client;

import arc.util.Strings;
import mindustry.game.Teams.TeamData;
import mindustry.gen.Player;
import mindustry.type.Item;
import pandorum.Misc;
import pandorum.comp.Icons;

import static mindustry.Vars.content;
import static mindustry.Vars.state;
import static pandorum.Misc.bundled;

public class GiveCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;

        if (args.length > 1 && !Strings.canParsePositiveInt(args[1])) {
            bundled(player, "commands.non-int");
            return;
        }

        int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1000;

        Item item = content.items().find(b -> b.name.equalsIgnoreCase(args[0]));
        if (item == null) {
            StringBuilder items = new StringBuilder();
            content.items().each(i -> items.append(" ").append(Icons.get(i.name)).append(i.name));
            bundled(player, "commands.admin.give.item-not-found", items.toString());
            return;
        }

        TeamData team = state.teams.get(player.team());
        if (team.noCores()) {
            bundled(player, "commands.admin.give.core-not-found");
            return;
        }

        team.core().items.add(item, amount);
        bundled(player, "commands.admin.give.success", amount, Icons.get(item.name));
    }
}
