package rewrite.commands;

import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.Strings;
import arc.util.Time;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import rewrite.components.MenuHandler;
import rewrite.discord.Bot;
import rewrite.features.Ranks;
import rewrite.features.Ranks.Rank;
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

        register("t", (args, player) -> player.team().data().players.each(teammate -> {
            bundled(teammate, "commands.t.chat", player.team().color, player.name, args[0]);
        }));

        register("sync", (args, player) -> {
            if (isCooldowned(player)) return;

            player.getInfo().lastSyncTime = Time.millis();
            Call.worldDataBegin(player.con);
            netServer.sendWorldData(player);
        });

        register("tr", (args, player) -> {
            PlayerData data = getPlayerData(player.uuid());
            switch (args[0].toLowerCase()) {
                case "current" -> bundled(player, "commands.tr.current", data.language);
                case "list" -> {
                    StringBuilder result = new StringBuilder(format("commands.tr.list", Find.locale(player.locale)));
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
                        data.playTime / 60f,
                        next.req.playTime() / 60f,
                        data.buildingsBuilt,
                        next.req.buildingsBuilt(),
                        data.gamesPlayed,
                        next.req.gamesPlayed()));

            Call.menu(player.con, MenuHandler.rankInfoMenu,
                    format("commands.rank.menu.header", locale, target.name),
                    builder.toString(),
                    new String[][] { { format("ui.menus.close", locale) }, { format("commands.rank.menu.requirements", locale) } });
        });

        register("players", (args, player) -> {
            if (notPage(player, args)) return;

            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1, pages = Mathf.ceil(Groups.player.size() / 8f);
            if (notPage(player, page, pages)) return;

            StringBuilder result = new StringBuilder(format("commands.players.page", Find.locale(player.locale), page, pages));
            Seq<Player> list = Groups.player.copy(new Seq<>());
            for (int i = 8 * (page - 1); i < Math.min(8 * page + 1, list.size); i++) {
                result.append("\n[#9c88ee]* [white]");
                Player p = list.get(i);
                if (p.admin) result.append(Iconc.admin).append(" ");
                result.append(p.name).append(" [lightgray]([accent]ID: ").append(p.id).append("[lightgray])").append(" [lightgray]([accent]Locale: ").append(p.locale).append("[lightgray])");
            }

            player.sendMessage(result.toString());
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
            if (notPage(player, args)) return;

            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1, pages = Mathf.ceil(maps.customMaps().size / 8f);
            if (notPage(player, page, pages)) return;

            StringBuilder result = new StringBuilder(format("commands.maps.page", Find.locale(player.locale), page, pages));
            Seq<Map> list = maps.customMaps();
            for (int i = 8 * (page - 1); i < Math.min(8 * page, list.size); i++)
                result.append("\n[lightgray] ").append(i).append(". [orange]").append(list.get(i).name());

            result.append(format("commands.maps.current", Find.locale(player.locale), state.map.name()));
            player.sendMessage(result.toString());
        });

        register("saves", (args, player) -> {
            if (notPage(player, args)) return;

            int page = args.length > 0 ? Strings.parseInt(args[0]) : 1, pages = Mathf.ceil(saveDirectory.list().length / 8f);
            if (notPage(player, page, pages)) return;

            StringBuilder result = new StringBuilder(format("commands.saves.page", Find.locale(player.locale), page, pages));
            Seq<Fi> list = Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid);
            for (int i = 8 * (page - 1); i < Math.min(8 * page, list.size); i++)
                result.append("\n[lightgray] ").append(i).append(". [orange]").append(list.get(i).nameWithoutExtension());

            player.sendMessage(result.toString());
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
