package pandorum.commands.client;

import arc.util.Strings;
import arc.util.Structs;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import pandorum.Misc;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.gamemodes.RequireSimpleGamemode;
import pandorum.comp.Icons;

import static mindustry.Vars.content;
import static pandorum.Misc.bundled;
import static pandorum.Misc.colorizedTeam;

public class SpawnCommand {
    private static final int maxAmount = 25;

    @RequireSimpleGamemode
    @ClientCommand(name = "spawn", args = "<unit> [count] [team]", description = "Spawn units.", admin = true)
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;

        if (args.length > 1 && !Strings.canParseInt(args[1])){
            bundled(player, "commands.non-int");
            return;
        }

        int count = args.length > 1 ? Strings.parseInt(args[1]) : 1;
        if (count > maxAmount || count < 1) {
            bundled(player, "commands.admin.spawn.limit", maxAmount);
            return;
        }

        Team team = args.length > 2 ? Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[2])) : player.team();
        if (team == null) {
            StringBuilder teams = new StringBuilder();
            for (Team t : Team.baseTeams) teams.append("\n[gold] - [white]").append(colorizedTeam(t));
            bundled(player, "commands.team-not-found", teams.toString());
            return;
        }

        UnitType type = content.units().find(u -> u.name.equalsIgnoreCase(args[0]) && u != UnitTypes.block);
        if (type == null) {
            StringBuilder units = new StringBuilder();
            content.units().each(u -> u != UnitTypes.block, u -> units.append(" ").append(Icons.get(u.name)).append(u.name));
            bundled(player, "commands.unit-not-found", units.toString());
            return;
        }

        for (int i = 0; i < count; i++) type.spawn(team, player.x, player.y);
        bundled(player, "commands.admin.spawn.success", count, Icons.get(type.name), colorizedTeam(team));
    }
}
