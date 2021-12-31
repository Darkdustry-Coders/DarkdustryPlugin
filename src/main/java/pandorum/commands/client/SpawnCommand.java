package pandorum.commands.client;

import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import pandorum.comp.Icons;

import static mindustry.Vars.content;
import static pandorum.Misc.*;

public class SpawnCommand {

    private static final int maxAmount = 25;

    public static void run(final String[] args, final Player player) {
        if (args.length > 1 && !Strings.canParseInt(args[1])) {
            bundled(player, "commands.non-int");
            return;
        }

        UnitType type = findUnit(args[0]);
        if (type == null) {
            StringBuilder units = new StringBuilder();
            content.units().each(u -> units.append(" ").append(Icons.get(u.name)).append(u.name));
            bundled(player, "commands.unit-not-found", units.toString());
            return;
        }

        int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
        if (amount > maxAmount || amount < 1) {
            bundled(player, "commands.admin.spawn.limit", maxAmount);
            return;
        }

        Team team = args.length > 2 ? findTeam(args[2]) : player.team();
        if (team == null) {
            StringBuilder teams = new StringBuilder();
            Structs.each(t -> teams.append("\n[gold] - [white]").append(colorizedTeam(t)), Team.baseTeams);
            bundled(player, "commands.team-not-found", teams.toString());
            return;
        }

        for (int i = 0; i < amount; i++) type.spawn(team, player.x, player.y);
        bundled(player, "commands.admin.spawn.success", amount, Icons.get(type.name), colorizedTeam(team));
    }
}
