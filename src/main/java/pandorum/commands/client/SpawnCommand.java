package pandorum.commands.client;

import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import pandorum.Misc;

import static mindustry.Vars.content;
import static pandorum.Misc.bundled;

public class SpawnCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;

        if (args.length > 1 && !Strings.canParseInt(args[1])){
            bundled(player, "commands.non-int");
            return;
        }

        int count = args.length > 1 ? Strings.parseInt(args[1]) : 1;
        if (count > 25 || count < 1) {
            bundled(player, "commands.admin.spawn.limit");
            return;
        }

        Team team = args.length > 2 ? Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[2])) : player.team();
        if (team == null) {
            bundled(player, "commands.teams");
            return;
        }

        UnitType unit = content.units().find(b -> b.name.equalsIgnoreCase(args[0]));
        if (unit == null || args[0].equalsIgnoreCase("block")) {
            bundled(player, "commands.unit-not-found");
            return;
        }

        for (int i = 0; count > i; i++) unit.spawn(team, player.x, player.y);
        bundled(player, "commands.admin.spawn.success", count, unit.name, Misc.colorizedTeam(team));
    }
}