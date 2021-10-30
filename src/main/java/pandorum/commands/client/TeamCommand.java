package pandorum.commands.client;

import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.Misc;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static pandorum.Misc.bundled;

public class TeamCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        Team team = Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[0]));
        if (team == null) {
            bundled(player, "commands.teams");
            return;
        }

        Player target = args.length > 1 ? Misc.findByName(args[1]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        bundled(target, "commands.admin.team.success", Misc.colorizedTeam(team));
        target.team(team);

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.successColor)
                .setTitle("Команда игрока изменена.")
                .addField("Никнейм: ", Strings.stripColors(player.name), false)
                .addField("Новая команда: ", team.name, false);

        BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
    }
}
