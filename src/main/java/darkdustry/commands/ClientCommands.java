package darkdustry.commands;

import arc.util.CommandHandler.*;
import darkdustry.features.*;
import darkdustry.features.votes.*;
import darkdustry.utils.*;
import mindustry.gen.*;

import static arc.util.Strings.parseInt;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.components.MenuHandler.*;
import static darkdustry.components.MongoDB.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class ClientCommands {

    public static void load() {
        register("help", PageIterator::commands);

        register("discord", (args, player) -> Call.openURI(player.con, discordServerUrl));

        register("sync", (args, player) -> {
            Call.worldDataBegin(player.con);
            netServer.sendWorldData(player);
        });

        register("t", (args, player) -> player.team().data().players.each(p -> bundled(p, player, args[0], "commands.t.chat", player.team().color, player.coloredName(), args[0])));

        register("tr", (args, player) -> getPlayerData(player.uuid()).subscribe(data -> {
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", data.language);
                case "list" -> {
                    var builder = new StringBuilder(format("commands.tr.list", Find.locale(player.locale)));
                    translatorLanguages.each((language, name) -> builder.append("\n[cyan]").append(language).append("[lightgray] - [accent]").append(name));
                    Call.infoMessage(player.con, builder.toString());
                }
                case "off" -> {
                    data.language = "off";
                    setPlayerData(data).subscribe();
                    bundled(player, "commands.tr.disabled");
                }
                case "auto" -> {
                    data.language = notNullElse(Find.language(player.locale), defaultLanguage);
                    setPlayerData(data).subscribe();
                    bundled(player, "commands.tr.auto", translatorLanguages.get(data.language), data.language);
                }
                default -> {
                    String language = Find.language(args[0]);
                    if (notLanguage(player, language)) return;

                    data.language = language;
                    setPlayerData(data).subscribe();
                    bundled(player, "commands.tr.changed", translatorLanguages.get(data.language), data.language);
                }
            }
        }));

        register("settings", (args, player) -> SettingsMenu.showSettingsMenu(player));

        register("players", PageIterator::players);

        register("hub", (args, player) -> net.pingHost(config.hubIp, config.hubPort,
                host -> Call.connect(player.con, host.address, host.port),
                exception -> bundled(player, "commands.hub.failed", exception.getMessage())));

        register("stats", (args, player) -> {
            var target = args.length > 0 ? Find.player(args[0]) : player;
            if (notFound(player, target)) return;

            getPlayerData(target.uuid()).subscribe(data -> showMenu(player, statsMenu, "commands.stats.header", "commands.stats.content",
                    new String[][] {{"ui.button.close"}}, target.coloredName(), data.rank().localisedName(Find.locale(player.locale)),
                    data.playTime, data.buildingsBuilt, data.gamesPlayed));
        });

        register("rank", (args, player) -> {
            var target = args.length > 0 ? Find.player(args[0]) : player;
            if (notFound(player, target)) return;

            getPlayerData(target.uuid()).subscribe(data -> {
                var rank = data.rank();
                var locale = Find.locale(player.locale);

                if (!rank.hasNext())
                    showMenu(player, rankInfoMenu, "commands.rank.header", "commands.rank.content", new String[][] {{"ui.button.close"}, {"commands.rank.button.requirements"}}, target.coloredName(), rank.localisedName(locale), rank.localisedDesc(locale));
                else
                    showMenu(player, rankInfoMenu, "commands.rank.header", "commands.rank.next", new String[][] {{"ui.button.close"}, {"commands.rank.button.requirements"}}, target.coloredName(), rank.localisedName(locale), rank.localisedDesc(locale),
                            rank.next.localisedName(locale),
                            data.playTime, rank.next.req.playTime(),
                            data.buildingsBuilt, rank.next.req.buildingsBuilt(),
                            data.gamesPlayed, rank.next.req.gamesPlayed()
                    );
            });
        });

        register("votekick", (args, player) -> {
            if (alreadyVoting(player, voteKick) || votekickDisabled(player)) return;

            var target = Find.player(args[0]);
            if (notFound(player, target) || invalidVotekickTarget(player, target)) return;

            voteKick = new VoteKick(player, target);
            voteKick.vote(player, 1);
        });

        register("vote", (args, player) -> {
            if (notVoting(player, voteKick) || alreadyVoted(player, voteKick)) return;

            if (invalidVoteTarget(player, voteKick.target)) return;

            int sign = voteChoice(args[0]);
            if (invalidVoteSign(player, sign)) return;
            voteKick.vote(player, sign);
        });

        register("login", (args, player) -> {
            if (alreadyAdmin(player)) return;

            Authme.sendAdminRequest(player);
            bundled(player, "commands.login.sent");
        });

        if (!config.mode.isDefault()) return;

        register("rtv", (args, player) -> {
            if (alreadyVoting(player, vote)) return;

            var map = args.length > 0 ? Find.map(args[0]) : maps.getNextMap(state.rules.mode(), state.map);
            if (notFound(player, map)) return;

            vote = new VoteRtv(map);
            vote.vote(player, 1);
        });

        register("vnw", (args, player) -> {
            if (alreadyVoting(player, vote)) return;

            if (invalidAmount(player, args, 0)) return;

            int waves = args.length > 0 ? parseInt(args[0]) : 1;
            if (invalidVnwAmount(player, waves)) return;

            vote = new VoteVnw(waves);
            vote.vote(player, 1);
        });

        register("savemap", (args, player) -> {
            if (alreadyVoting(player, vote)) return;

            vote = new VoteSave(saveDirectory.child(args[0] + "." + saveExtension));
            vote.vote(player, 1);
        });

        register("loadsave", (args, player) -> {
            if (alreadyVoting(player, vote)) return;

            var save = Find.save(args[0]);
            if (notFound(player, save)) return;

            vote = new VoteLoad(save);
            vote.vote(player, 1);
        });

        register("maps", PageIterator::maps);

        register("saves", PageIterator::saves);
    }

    public static Command register(String name, CommandRunner<Player> runner) {
        return clientCommands.<Player>register(name, get("commands." + name + ".params", "", defaultLocale), get("commands." + name + ".description", defaultLocale), (args, player) -> {
            if (onCooldown(player, name)) return;
            runner.accept(args, player);
            Cooldowns.run(player.uuid(), name);
        });
    }
}