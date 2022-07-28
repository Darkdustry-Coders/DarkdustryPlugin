package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.Item;
import pandorum.components.Icons;

import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findItem;
import static pandorum.util.Search.findTeam;
import static pandorum.util.StringUtils.coloredTeam;

public class GiveCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        if (args.length > 1 && !Strings.canParsePositiveInt(args[1])) {
            bundled(player, "commands.not-int");
            return;
        }

        Item item = findItem(args[0]);
        if (item == null) {
            bundled(player, "commands.item-not-found", itemsList);
            return;
        }

        int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
        if (amount < 1 || amount > maxGiveAmount) {
            bundled(player, "commands.give.limit", maxGiveAmount);
            return;
        }

        Team team = args.length > 2 ? findTeam(args[2]) : player.team();
        if (team == null) {
            bundled(player, "commands.team-not-found", teamsList);
            return;
        }

        if (team.core() == null) {
            bundled(player, "commands.give.no-core", coloredTeam(team));
            return;
        }

        team.core().items.add(item, amount);
        bundled(player, "commands.give.success", amount, Icons.get(item.name), coloredTeam(team));
    }
}
