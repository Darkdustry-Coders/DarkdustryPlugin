package darkdustry.commands;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Time;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.maps.Map;
import darkdustry.discord.Bot;
import darkdustry.features.Ranks;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.votes.*;
import darkdustry.utils.Cooldowns;
import darkdustry.utils.Find;
import darkdustry.utils.PageIterator;

import java.util.Locale;

import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.components.Database.*;
import static darkdustry.components.MenuHandler.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;

public class ClientCommands {

    public ClientCommands(CommandHandler handler) {
        register("help", PageIterator::commands);

        register("discord", (args, player) -> bundled(player, "commands.discord.link", discordServerUrl));

        register("t", (args, player) -> player.team().data().players.each(teammate -> bundled(teammate, "commands.t.chat", player.team().color, player.coloredName(), args[0])));

        register("sync", (args, player) -> {
            if (alreadySynced(player)) return;

            player.getInfo().lastSyncTime = Time.millis();
            Call.worldDataBegin(player.con);
            netServer.sendWorldData(player);
            Cooldowns.run(player.uuid(), "sync");
        });

        register("tr", (args, player) -> {
            PlayerData data = getPlayerData(player.uuid());
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", data.language);
                case "list" -> {
                    StringBuilder result = new StringBuilder(get("commands.tr.list", Find.locale(player.locale)));
                    translatorLanguages.each((language, name) -> result.append("\n[cyan]").append(language).append("[lightgray] - [accent]").append(name));
                    Call.infoMessage(player.con, result.toString());
                }
                case "off" -> {
                    data.language = "off";
                    setPlayerData(data);
                    bundled(player, "commands.tr.disabled");
                }
                case "auto" -> {
                    data.language = Find.language(player.locale);
                    setPlayerData(data);
                    bundled(player, "commands.tr.auto", translatorLanguages.get(data.language), data.language);
                }
                default -> {
                    if (notFound(player, args[0])) return;

                    data.language = args[0];
                    setPlayerData(data);
                    bundled(player, "commands.tr.changed", translatorLanguages.get(data.language), data.language);
                }
            }
        });

        register("stats", (args, player) -> {
            Player target = args.length > 0 ? Find.player(args[0]) : player;
            if (notFound(player, target, args[0])) return;

            PlayerData data = getPlayerData(target.uuid());
            Rank rank = Ranks.getRank(data.rank);

            showMenu(player, statsMenu, "commands.stats.menu.header", "commands.stats.menu.content",
                    new String[][] {{"ui.menus.close"}}, target.coloredName(), rank.tag, rank.localisedName(Find.locale(player.locale)),
                    data.playTime, data.buildingsBuilt, data.gamesPlayed);
        });

        register("rank", (args, player) -> {
            Player target = args.length > 0 ? Find.player(args[0]) : player;
            if (notFound(player, target, args[0])) return;

            PlayerData data = getPlayerData(target.uuid());
            Rank rank = Ranks.getRank(data.rank), next = rank.next;
            Locale locale = Find.locale(player.locale);

            StringBuilder builder = new StringBuilder(format("commands.rank.menu.content", locale, rank.tag, get(rank.name, locale)));
            if (next != null && next.req != null)
                builder.append(format("commands.rank.menu.next",
                        locale,
                        next.tag,
                        get(next.name, locale),
                        data.playTime,
                        next.req.playTime(),
                        data.buildingsBuilt,
                        next.req.buildingsBuilt(),
                        data.gamesPlayed,
                        next.req.gamesPlayed()));

            Call.menu(player.con, rankInfoMenu,
                    format("commands.rank.menu.header", locale, target.coloredName()),
                    builder.toString(),
                    new String[][] {{format("ui.menus.close", locale)}, {format("commands.rank.menu.requirements", locale)}});
        });

        register("players", PageIterator::players);

        register("hub", (args, player) -> net.pingHost(config.hubIp, config.hubPort,
                host -> Call.connect(player.con, host.address, host.port),
                exception -> bundled(player, "commands.hub.offline")));

        register("vote", (args, player) -> {
            if (notVoting(player) || alreadyVoted(player)) return;
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
            if (invalidVoteSign(player, sign)) return;
            vote.vote(player, sign);
        });

        register("votekick", (args, player) -> {
            if (isVoting(player) || isCooldowned(player, "votekick") || votekickDisabled(player)) return;
            Player target = Find.player(args[0]);
            if (notFound(player, target, args[0]) || invalidVoteTarget(player, target)) return;

            vote = new VoteKick(player, target);
            vote.vote(player, 1);
            Cooldowns.run(player.uuid(), "votekick");
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
                default -> {
                    bundled(player, "commands.nominate.incorrect-mode");
                    return; // чтобы Cooldowns.run не вызвалось
                }
            }
            Cooldowns.run(player.uuid(), "nominate");
        });

        register("maps", PageIterator::maps);

        register("saves", PageIterator::saves);

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
            if (alreadyAdmin(player) || isCooldowned(player, "login")) return;

            Bot.sendAdminRequest(player);
            bundled(player, "commands.login.sent");
            Cooldowns.run(player.uuid(), "login");
        });
    }

    public void register(String name, CommandRunner<Player> runner) {
        clientCommands.register(name, get("commands." + name + ".params", ""), get("commands." + name + ".description", ""), runner);
    }
}