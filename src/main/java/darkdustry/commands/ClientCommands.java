package darkdustry.commands;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Time;
import darkdustry.features.Ranks;
import darkdustry.features.votes.*;
import darkdustry.utils.Cooldowns;
import darkdustry.utils.Find;
import darkdustry.utils.PageIterator;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static arc.util.Strings.parseInt;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.components.Bundle.get;
import static darkdustry.components.Database.getPlayerData;
import static darkdustry.components.Database.setPlayerData;
import static darkdustry.components.MenuHandler.*;
import static darkdustry.discord.Bot.sendAdminRequest;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.voteChoice;
import static mindustry.Vars.*;

public class ClientCommands {

    public static void load() {
        register("help", PageIterator::commands);

        register("discord", (args, player) -> Call.openURI(player.con, discordServerUrl));

        register("t", (args, player) -> player.team().data().players.each(teammate -> bundled(teammate, "commands.t.chat", player.team().color, player.coloredName(), args[0])));

        register("sync", (args, player) -> {
            if (alreadySynced(player)) return;

            player.getInfo().lastSyncTime = Time.millis();
            Call.worldDataBegin(player.con);
            netServer.sendWorldData(player);
            Cooldowns.run(player.uuid(), "sync");
        });

        register("tr", (args, player) -> {
            var data = getPlayerData(player.uuid());
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", data.language);
                case "list" -> {
                    var builder = new StringBuilder(get("commands.tr.list", Find.locale(player.locale)));
                    translatorLanguages.each((language, name) -> builder.append("\n[cyan]").append(language).append("[lightgray] - [accent]").append(name));
                    Call.infoMessage(player.con, builder.toString());
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
            var target = args.length > 0 ? Find.player(args[0]) : player;
            if (notFound(player, target)) return;

            var data = getPlayerData(target.uuid());
            var rank = Ranks.getRank(data.rank);

            showMenu(player, statsMenu, "commands.stats.menu.header", "commands.stats.menu.content",
                    new String[][]{{"ui.menus.close"}}, target.coloredName(), rank.localisedName(Find.locale(player.locale)),
                    data.playTime, data.buildingsBuilt, data.gamesPlayed);
        });

        register("rank", (args, player) -> {
            var target = args.length > 0 ? Find.player(args[0]) : player;
            if (notFound(player, target)) return;

            var data = getPlayerData(target.uuid());
            var rank = Ranks.getRank(data.rank);
            var locale = Find.locale(player.locale);

            if (!rank.hasNext()) {
                showMenu(player, rankInfoMenu, "commands.rank.menu.header", "commands.rank.menu.content", new String[][]{{"ui.menus.close"}, {"commands.rank.menu.requirements"}}, target.coloredName(), rank.localisedName(locale), rank.localisedDesc(locale));
            } else {
                showMenu(player, rankInfoMenu, "commands.rank.menu.header", "commands.rank.menu.content.next", new String[][]{{"ui.menus.close"}, {"commands.rank.menu.requirements"}}, target.coloredName(), rank.localisedName(locale), rank.localisedDesc(locale),
                        rank.next.localisedName(locale),
                        data.playTime, rank.next.req.playTime(),
                        data.buildingsBuilt, rank.next.req.buildingsBuilt(),
                        data.gamesPlayed, rank.next.req.gamesPlayed()
                );
            }
        });

        register("players", PageIterator::players);

        register("hub", (args, player) -> net.pingHost(config.hubIp, config.hubPort,
                host -> Call.connect(player.con, host.address, host.port),
                e -> bundled(player, "commands.hub.failed", e.getMessage())));

        register("votekick", (args, player) -> {
            if (isVoting(player, voteKick) || isCooldowned(player, "votekick") || votekickDisabled(player)) return;

            var target = Find.player(args[0]);
            if (notFound(player, target) || invalidVotekickTarget(player, target)) return;

            voteKick = new VoteKick(player, target);
            voteKick.vote(player, 1);
            Cooldowns.run(player.uuid(), "votekick");
        });

        register("vote", (args, player) -> {
            if (notVoting(player, voteKick) || alreadyVoted(player, voteKick)) return;

            if (invalidVoteTarget(player, voteKick.target)) return;

            int sign = voteChoice(args[0]);
            if (invalidVoteSign(player, sign)) return;
            voteKick.vote(player, sign);
        });

        register("login", (args, player) -> {
            if (alreadyAdmin(player) || isCooldowned(player, "login")) return;

            sendAdminRequest(player);
            bundled(player, "commands.login.sent");
            Cooldowns.run(player.uuid(), "login");
        });

        if (!config.mode.isDefault()) return;

        register("rtv", (args, player) -> {
            if (isVoting(player, vote) || isCooldowned(player, "rtv")) return;

            var map = args.length > 0 ? Find.map(args[0]) : maps.getNextMap(state.rules.mode(), state.map);
            if (notFound(player, map)) return;

            vote = new VoteRtv(map);
            vote.vote(player, 1);
            Cooldowns.run(player.uuid(), "rtv");
        });

        register("vnw", (args, player) -> {
            if (isVoting(player, vote) || isCooldowned(player, "vnw")) return;

            if (invalidAmount(player, args, 0)) return;

            int waves = args.length > 0 ? parseInt(args[0]) : 1;
            if (invalidVnwAmount(player, waves)) return;

            vote = new VoteVnw(waves);
            vote.vote(player, 1);
            Cooldowns.run(player.uuid(), "vnw");
        });

        register("savemap", (args, player) -> {
            if (isVoting(player, vote) || isCooldowned(player, "savemap")) return;

            vote = new VoteSave(saveDirectory.child(args[0] + "." + saveExtension));
            vote.vote(player, 1);
            Cooldowns.run(player.uuid(), "savemap");
        });

        register("loadsave", (args, player) -> {
            if (isVoting(player, vote) || isCooldowned(player, "loadsave")) return;

            var save = Find.save(args[0]);
            if (notFound(player, save)) return;

            vote = new VoteLoad(save);
            vote.vote(player, 1);
            Cooldowns.run(player.uuid(), "loadsave");
        });

        register("maps", PageIterator::maps);

        register("saves", PageIterator::saves);

        register("history", (args, player) -> {
            if (activeHistory.remove(player.uuid())) bundled(player, "commands.history.disabled");
            else {
                activeHistory.add(player.uuid());
                bundled(player, "commands.history.enabled");
            }
        });

        register("alerts", (args, player) -> {
            var data = getPlayerData(player.uuid());
            data.alertsEnabled = !data.alertsEnabled;
            setPlayerData(data);
            bundled(player, data.alertsEnabled ? "commands.alerts.enabled" : "commands.alerts.disabled");
        });
    }

    public static void register(String name, CommandRunner<Player> runner) {
        clientCommands.register(name, get("commands." + name + ".params", ""), get("commands." + name + ".description", ""), runner);
    }
}
