package darkdustry.commands;

import arc.util.*;
import darkdustry.database.*;
import darkdustry.features.OnevAll;
import darkdustry.features.Spectate;
import darkdustry.features.menus.*;
import darkdustry.features.net.*;
import darkdustry.features.votes.*;
import darkdustry.listeners.SocketEvents.*;
import darkdustry.utils.*;
import mindustry.Vars;
import mindustry.game.MapObjectives;
import mindustry.gen.*;
import useful.*;

import java.util.Arrays;
import java.util.Random;

import static darkdustry.PluginVars.*;
import static darkdustry.config.Config.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.server.ServerControl.*;

public class ClientCommands {

    public static void load() {
        Commands.create("help")
                .welcomeMessage(true)
                .register(PageIterator::commands);

        Commands.create("discord")
                .welcomeMessage(true)
                .register((args, player) -> Call.openURI(player.con, discordServerUrl));

        Commands.create("discord-link")
                .welcomeMessage(true)
                .register((args, player) -> {
                    var entry = Database.getPlayerData(player);
                    if (!entry.discordId.isEmpty()) {
                        Bundle.send(player, "commands.discord-link.already-linked", entry.discordAttachCode);
                        return;
                    }
                    if (entry.discordAttachCode.isEmpty()) {
                        var random = new Random();
                        for (int i = 0; i < 6; i++) {
                            int chr = 'a';
                            chr += random.nextInt(25);
                            entry.discordAttachCode += (char) chr;
                        }
                        Database.savePlayerData(entry);
                    }
                    Bundle.send(player, "commands.discord-link.code", entry.discordAttachCode);
                });

        Commands.create("sync")
                .cooldown(15000L)
                .register((args, player) -> {
                    Call.worldDataBegin(player.con);
                    netServer.sendWorldData(player);
                });

        Commands.create("players").register(PageIterator::players);

        Commands.create("settings")
                .welcomeMessage(true)
                .register((args, player) -> {
                    if (args.length == 0) {
                        MenuHandler.showSettingsMenu(player);
                        return;
                    }
                    var setting = Arrays.stream(MenuHandler.Setting.values()).filter(x -> x.name().equals(args[0])).findAny();
                    var data = Database.getPlayerData(player);
                    if (setting.isPresent()) {
                        setting.get().setter.get(data);
                        Database.savePlayerData(data);
                        Bundle.send(player, "commands.settings.toggled", args[0]);
                    }
                    else {
                        Bundle.send(player, "commands.settings.not-found", args[0]);
                    }
                });

        Commands.create("history")
                .register((args, player) -> {
                    var data = Database.getPlayerData(player);
                    MenuHandler.Setting.history.setter.get(data);
                    Database.savePlayerData(data);
                    if (MenuHandler.Setting.history.getter.get(data)) Bundle.send(player, "commands.history.enabled");
                    else Bundle.send(player, "commands.history.disabled");
                });

        Commands.create("hub")
                .enabled(!config.hubIp.isEmpty())
                .register((args, player) -> net.pingHost(config.hubIp, config.hubPort, host -> Call.connect(player.con, config.hubIp, config.hubPort), e -> Bundle.send(player, "commands.hub.error")));

        Commands.create("stats")
                .welcomeMessage(true)
                .register((args, player) -> {
                    var target = args.length > 0 ? Find.player(args[0]) : player;
                    if (notFound(player, target)) return;

                    MenuHandler.showStatsMenu(player, target, Cache.get(target));
                });

        Commands.create("votekick")
                .cooldown(300000L)
                .register((args, player) -> {
                    if (votekickDisabled(player) || alreadyVoting(player, voteKick)) return;

                    var target = Find.player(args[0]);
                    if (notFound(player, target) || invalidVotekickTarget(player, target)) return;

                    voteKick = new VoteKick(player, target, args[1]);
                    voteKick.vote(player, 1);
                });

        Commands.create("vote")
                .register((args, player) -> {
                    if (notVoting(player, voteKick)) return;

                    if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("cancel")) {
                        if (notAdmin(player)) return;

                        voteKick.cancel(player);
                        return;
                    }

                    int sign = voteChoice(args[0]);
                    if (invalidVoteSign(player, sign)) return;

                    voteKick.vote(player, sign);
                });

        Commands.hidden("login")
                .cooldown(300000L)
                .register((args, player) -> {
                    if (alreadyAdmin(player)) return;

                    MenuHandler.showConfirmMenu(player, "commands.login.confirm", () -> {
                        if (!Socket.isConnected()) {
                            Bundle.send(player, "commands.login.error");
                            return;
                        }

                        Socket.send(new AdminRequestEvent(config.mode.name(), Cache.get(player)));
                        Bundle.send(player, "commands.login.sent");
                    });
                });

        Commands.create("1va")
                .enabled(config.mode.enable1va)
                .cooldown(60000L)
                .welcomeMessage(true)
                .register((args, player) -> {
                    if (alreadyVoting(player, vote)) return;

                    if (Groups.player.count(x -> config.mode.enableSpectate && x.team() != config.mode.spectatorTeam) < 4 ||
                        config.mode == Gamemode.test) {
                        Bundle.send(player, "commands.1va.insufficient-players");
                        return;
                    }

                    mindustry.maps.Map map;

                    if (args.length > 0) {
                        map = Find.map(args[0]);
                        if (notFound(player, map)) return;
                        if (map.rules().objectives.all.contains(x -> (x instanceof MapObjectives.FlagObjective) &&
                                ((MapObjectives.FlagObjective) x).flag.equals("no1va"))) {
                            Bundle.send(player, "commands.1va.invalid-map");
                            return;
                        }
                    } else {
                        var prevention = 0;
                        map = maps.getNextMap(instance.lastMode, state.map);
                        while (map.rules().objectives.all.contains(x -> (x instanceof MapObjectives.FlagObjective) &&
                                ((MapObjectives.FlagObjective) x).flag.equals("no1va"))) {
                            if (++prevention >= 100) {
                                Bundle.send(player, "commands.1va.invalid-map");
                                return;
                            }
                            map = maps.getShuffleMode().next(instance.lastMode, map);
                        }
                    }

                    vote = new Vote1va(map, player);
                });

        Commands.create("rtv")
                .enabled(config.mode.enableRtv)
                .cooldown(60000L)
                .welcomeMessage(true)
                .register((args, player) -> {
                    if (alreadyVoting(player, vote)) return;

                    var map = args.length > 0 ? Find.map(args[0]) : maps.getNextMap(instance.lastMode, state.map);
                    if (notFound(player, map)) return;

                    vote = new VoteRtv(map);
                    vote.vote(player, 1);
                });

        Commands.create("maps")
                .enabled(config.mode.enableRtv)
                .register(PageIterator::maps);

        Commands.create("vnw")
                .enabled(config.mode.enableVnw)
                .cooldown(60000L)
                .welcomeMessage(true)
                .register((args, player) -> {
                    if (alreadyVoting(player, vote)) return;

                    int amount = args.length > 0 ? Strings.parseInt(args[0]) : 1;
                    if (invalidAmount(player, amount, 1, maxVnwAmount)) return;

                    vote = new VoteVnw(amount);
                    vote.vote(player, 1);
                });

        Commands.create("surrender")
                .enabled(config.mode.enableSurrender)
                .cooldown(180000L)
                .welcomeMessage(true)
                .register((args, player) -> {
                    if (state.tick < 50.0) {
                        Bundle.send(player, "commands.surrender.early");
                        return;
                    }
                    if (alreadyVoting(player, vote) || invalidSurrenderTeam(player)) return;

                    vote = new VoteSurrender(player.team());
                    vote.vote(player, 1);
                });

        Commands.create("votesave")
                .enabled(config.mode.isDefault)
                .cooldown(180000L)
                .register((args, player) -> {
                    if (alreadyVoting(player, vote)) return;
                    if (!isSafePath(args[0])) {
                        Bundle.format("commands.votesave.invalid-name", player);
                        return;
                    }

                    vote = new VoteSave(saveDirectory.child(args[0] + "." + saveExtension));
                    vote.vote(player, 1);
                });

        Commands.create("voteload")
                .enabled(config.mode.isDefault)
                .cooldown(180000L)
                .register((args, player) -> {
                    if (alreadyVoting(player, vote)) return;

                    if (!isSafePath(args[0])) {
                        Bundle.format("commands.votesave.invalid-name", player);
                        return;
                    }
                    var save = Find.save(args[0]);
                    if (notFound(player, save)) {
                        Bundle.format("commands.voteload.not-found", player, args[0]);
                        return;
                    }

                    vote = new VoteLoad(save);
                    vote.vote(player, 1);
                });

        Commands.create("saves")
                .enabled(config.mode.isDefault)
                .register(PageIterator::saves);

        Commands.create("spectate")
                .enabled(config.mode.enableSpectate)
                .register((args, player) -> {
                    if (OnevAll.enabled() && OnevAll.single == player) {
                        Bundle.send(player, "commands.spectate.1va");
                        return;
                    }
                    if (Spectate.isSpectator(player)) {
                        Spectate.stopSpectating(player);
                        return;
                    }
                    Spectate.spectate(player);
                });

        Commands.create("mute")
                .admin(true)
                .register((args, player) -> {
                    if (notAdmin(player)) return;

                    var target = Find.player(args[0]);
                    if (notFound(player, target)) return;

                    var duration = parseDuration(args[1]);
                    if (invalidDuration(player, duration)) return;

                    var reason = args[2];

                    Admins.mute(target, player, duration.toMillis(), reason);
                    Bundle.send(player, "commands.mute.success", target.coloredName());
                });

        Commands.create("unmute")
                .admin(true)
                .register((args, player) -> {
                    if (notAdmin(player)) return;

                    var target = Find.player(args[0]);
                    if (notFound(player, target)) return;

                    Admins.unmute(target);
                    Bundle.send(player, "commands.unmute.success", target.coloredName());
                });

        Commands.create("t")
                .register((args, player) -> {
                    if (Admins.checkMuted(player)) {
                        Bundle.send(player, "commands.mute.message");
                        return;
                    }
                    Translator.translate(other -> other.team() == player.team(), player, args[0], "commands.t.chat", player.team().color, player.coloredName());
                });
    }
}
