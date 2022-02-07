package pandorum.commands.client;

import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.type.Item;
import pandorum.comp.Icons;

import static mindustry.Vars.state;
import static pandorum.util.Search.findItem;
import static pandorum.util.Utils.bundled;

public class GiveCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 1 && !Strings.canParsePositiveInt(args[1])) {
            bundled(player, "commands.non-int");
            return;
        }

        Item item = findItem(args[0]);
        if (item == null) {
            bundled(player, "commands.item-not-found", Icons.itemsList());
            return;
        }

        int amount = Strings.parseInt(args[1]);

        if (state.teams.get(player.team()).noCores()) {
            bundled(player, "commands.admin.give.no-core");
            return;
        }

        state.teams.get(player.team()).core().items.add(item, amount);
        bundled(player, "commands.admin.give.success", amount, Icons.get(item.name));
    }
}
