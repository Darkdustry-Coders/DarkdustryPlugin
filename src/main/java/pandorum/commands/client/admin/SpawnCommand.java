package pandorum.commands.client.admin;

import arc.util.Strings;
import arc.util.Structs;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import pandorum.Misc;
import pandorum.comp.Icons;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static mindustry.Vars.content;
import static pandorum.Misc.bundled;

public class SpawnCommand {

    private static final int maxAmount = 25;

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
            for (Team t : Team.baseTeams) teams.append("\n[gold] - [white]").append(Icons.get(t.name)).append(Misc.colorizedTeam(t));
            bundled(player, "commands.team-not-found", teams.toString());
            return;
        }

        UnitType unit = content.units().find(b -> b.name.equalsIgnoreCase(args[0]));
        if (unit == null || unit == UnitTypes.block) {
            bundled(player, "commands.unit-not-found");
            return;
        }

        for (int i = 0; i < count; i++) unit.spawn(team, player.x, player.y);
        bundled(player, "commands.admin.spawn.success", count, unit.name, Misc.colorizedTeam(team));

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.normalColor)
                .title("Заспавнены юниты!")
                .addField("Заспавнил:", Strings.stripColors(player.name), false)
                .addField("Тип юнита:", unit.name, false)
                .addField("Команда:", team.name, false)
                .addField("Количество:", Integer.toString(count), false)
                .build();

        BotHandler.sendEmbed(embed);
    }
}
