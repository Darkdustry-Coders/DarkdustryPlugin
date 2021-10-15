package pandorum.commands.client;

import arc.util.Strings;
import arc.util.Structs;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.DiscordWebhookManager;

import static pandorum.Misc.bundled;

public class TeamCommand {
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

        String text = args.length > 1 ? "Команда игрока " + Strings.stripColors(target.name()) + " изменена на " + team + "." : "Команда изменена на " + team + ".";
        WebhookEmbedBuilder teamEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setTitle(new WebhookEmbed.EmbedTitle(text, null))
                .addField(new WebhookEmbed.EmbedField(true, "Администратором", Strings.stripColors(player.name)));
        DiscordWebhookManager.client.send(teamEmbedBuilder.build());
    }
}
