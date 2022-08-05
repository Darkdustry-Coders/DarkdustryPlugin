package rewrite.commands;

import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.Strings;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.maps.Map;
import rewrite.discord.Bot;
import rewrite.features.votes.*;
import rewrite.utils.Find;

import java.util.Locale;

import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;
import static rewrite.utils.Checks.*;
import static rewrite.utils.Utils.*;

public class ClientCommands extends Commands<Player> {

    public ClientCommands(CommandHandler handler, Locale def) {
        super(handler, def);

        register("help", (args, player) -> {
            if (notPage(player, args)) return;
            Locale locale = Find.locale(player.locale);
    
            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1, pages = Mathf.ceil(clientCommands.getCommandList().size / 8f);
            if (notPage(player, page, pages)) return;
    
            StringBuilder result = new StringBuilder(format("commands.help.page", locale, page, pages));
            Seq<Command> list = clientCommands.getCommandList();
            for (int i = 8 * (page - 1); i < Math.min(8 * page, list.size); i++) {
                Command command = list.get(i);
                result.append("\n[orange] ").append(clientCommands.getPrefix()).append(command.text).append("[white] ")
                        .append(get("commands." + command.text + ".params", command.paramText, locale)).append("[lightgray] - ")
                        .append(get("commands." + command.text + ".description", command.description, locale));
            }
    
            player.sendMessage(result.toString());
        });

        register("discord", (args, player) -> bundled(player, "commands.discord.link", discordServerUrl));

        register("t", (args, player) -> player.team().data().players.each(teammate -> 
                bundled(teammate, "commands.t.chat", player.team().color, player.name, args[0])));

        register("sync", (args, player) -> {

        });
        register("tr", (args, player) -> {

        });
        register("stats", (args, player) -> {

        });
        register("rank", (args, player) -> {

        });
        register("players", (args, player) -> {

        });

        register("hub", (args, player) -> net.pingHost(config.hubIp, config.hubPort,
                host -> Call.connect(player.con, host.address, host.port),
                exception -> bundled(player, "commands.hub.offline")));

        register("vote", (args, player) -> {
            if (notVoting(player) || isVoted(player)) return;
            if (vote instanceof VoteKick kick) {
                if (kick.target == player) {
                    bundled(player, "commands.vote.player-is-you");
                    return;
                } else if (kick.target.team() != player.team()) {
                    bundled(player, "commands.vote.player-is-enemy");
                    return;
                }
            }

            int sign = voteChoice(args[0]);
            if (isInvalide(player, sign)) return;
            vote.vote(player, sign);
        });

        register("votekick", (args, player) -> {
            if (isVoting(player) || isCooldowned(player, "votekick") || isDisabled(player)) return;
            Player target = Find.player(args[0]);
            if (notFound(player, target, args[0]) || isInvalide(player, target)) return;

            vote = new VoteKick(player, target);
            vote.vote(player, 1);
        });

        if (!config.mode.isDefault()) return;

        register("rtv", (args, player) -> {
            if (isVoting(player)) return;
            vote = new VoteRtv();
            vote.vote(player, 1);
        });

        register("vnw", (args, player) -> {
            if (isVoting(player)) return;
            vote = new VoteVnw();
            vote.vote(player, 1);
        });

        register("nominate", (args, player) -> {
            if (isVoting(player) || isCooldowned(player, "nominate")) return;

            switch (args[0].toLowerCase()) {
                case "map" -> {
                    Map map = Find.map(args[1]);
                    if (notFound(player, map)) return;
                    vote = new VoteMap(map);
                    vote.vote(player, 1);
                }
                case "save" -> {
                    vote = new VoteSave(saveDirectory.child(args[1] + "." + saveExtension));
                    vote.vote(player, 1);
                }
                case "load" -> {
                    Fi save = Find.save(args[1]);
                    if (notFound(player, save)) return;
                    vote = new VoteLoad(save);
                    vote.vote(player, 1);
                }
                default -> bundled(player, "commands.nominate.incorrect-mode");
            }
        });

        register("maps", (args, player) -> {

        });
        register("saves", (args, player) -> {

        });

        register("history", (args, player) -> {
            if (activeHistory.remove(player.uuid()))
                bundled(player, "commands.history.disabled");
            else {
                activeHistory.add(player.uuid());
                bundled(player, "commands.history.enabled");
            }
        });

        register("alerts", (args, player) -> {
            PlayerData data = getPlayerData(player.uuid());
            data.alertsEnabled = !data.alertsEnabled;
            setPlayerData(data);
            bundled(player, data.alertsEnabled ? "commands.alerts.enabled" : "commands.alerts.disabled");
        });

        register("login", (args, player) -> {
            if (isAdmin(player) || isCooldowned(player, "login")) return;

            Bot.sendMessageToAdmin(player);
            bundled(player, "commands.login.sent");
        });
    }
}
