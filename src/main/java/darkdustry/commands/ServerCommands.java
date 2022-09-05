package darkdustry.commands;

import arc.util.Log;
import darkdustry.DarkdustryPlugin;
import darkdustry.components.Updater;
import darkdustry.features.Ranks;
import darkdustry.features.Ranks.Rank;
import darkdustry.utils.Find;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.gen.Groups;
import mindustry.maps.*;
import mindustry.net.Administration.PlayerInfo;

import static arc.Core.*;
import static arc.util.Strings.parseInt;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.components.MongoDB.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class ServerCommands {

    // Зарежу дарка
    // (C) xzxADIxzx, 2023 год вне н.э.
    public static void load() {
        serverCommands.register("exit", "Exit the server application.", args -> {
            Log.info("Shutting down server.");
            DarkdustryPlugin.exit();
        });

        serverCommands.register("update", "Updates the server.", args -> Updater.init());

        serverCommands.register("stop", "Stop hosting the server.", args -> {
            net.closeServer();
            state.set(State.menu);
            Log.info("Stopped server.");
        });

        serverCommands.register("host", "[map] [mode]", "Start server on selected map.", args -> {
            if (isLaunched()) return;

            Gamemode mode;
            if (args.length > 1) {
                mode = Find.mode(args[1]);
                if (notFound(mode, args)) return;
            } else {
                mode = notNullElse(Find.mode(settings.getString("lastServerMode")), Gamemode.survival);
                Log.info("Default mode selected to be @.", mode.name());
            }

            Map map;
            if (args.length > 0) {
                map = Find.map(args[0]);
                if (notFound(map, args)) return;
            } else {
                map = maps.getShuffleMode().next(mode, state.map);
                Log.info("Randomized next map to be @.", map.name());
            }

            settings.put("lastServerMode", mode.name());

            app.post(() -> {
                try {
                    Log.info("Loading map...");

                    logic.reset();
                    world.loadMap(map, map.applyRules(mode));
                    state.rules = map.applyRules(mode);
                    logic.play();

                    Log.info("Map loaded.");

                    netServer.openServer();
                } catch (MapException exception) {
                    Log.err("@: @", exception.map.name(), exception.getMessage());
                }
            });
        });

        serverCommands.register("say", "<message...>", "Send a message to all players.", args -> {
            Log.info("&fi@: &fr&lw@", "&lcServer", "&lw" + args[0]);
            sendToChat("commands.say.chat", args[0]);
            sendMessage(botChannel, "Сервер » @", args[0]);
        });

        serverCommands.register("kick", "<username/id...>", "Kick a player.", args -> {
            var target = Find.player(args[0]);
            if (notFound(target, args)) return;

            kick(target, kickDuration, true, "kick.kicked");
            Log.info("Player @ has been kicked.", target.plainName());
            sendToChat("events.server.kick", target.coloredName());
        });

        serverCommands.register("pardon", "<uuid/ip>", "Pardon a kicked player.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            info.lastKicked = 0L;
            info.ips.each(netServer.admins.kickedIPs::remove);
            Log.info("Player @ has been pardoned.", info.plainLastName());
        });

        serverCommands.register("ban", "<username/uuid/ip...>", "Ban a player.", args -> {
            var target = Find.player(args[0]);
            if (target != null) {
                netServer.admins.banPlayer(target.uuid());
                kick(target, 0, true, "kick.banned");
                Log.info("Player @ has been banned.", target.plainName());
                sendToChat("events.server.ban", target.coloredName());
                return;
            }

            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            netServer.admins.banPlayer(info.id);
            Log.info("Player @ has been banned.", info.plainLastName());
            Groups.player.each(player -> netServer.admins.isIDBanned(player.uuid()) || netServer.admins.isIPBanned(player.ip()), player -> {
                kick(player, 0, true, "kick.banned");
                sendToChat("events.server.ban", player.coloredName());
            });
        });

        serverCommands.register("unban", "<uuid/ip>", "Unban a player.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            netServer.admins.unbanPlayerID(info.id);
            info.ips.each(netServer.admins::unbanPlayerIP);
            Log.info("Player @ has been unbanned.", info.plainLastName());
        });

        serverCommands.register("bans", "List all banned IPs and IDs.", args -> {
            var bannedIDs = netServer.admins.getBanned();
            if (bannedIDs.isEmpty())
                Log.info("No ID-banned players have been found.");
            else {
                Log.info("ID-banned players: (@)", bannedIDs.size);
                bannedIDs.each(info -> Log.info("  @ / Last known name: @", info.id, info.plainLastName()));
            }

            var bannedIPs = netServer.admins.getBannedIPs();
            if (bannedIPs.isEmpty())
                Log.info("No IP-banned players have been found.");
            else {
                Log.info("IP-banned players: (@)", bannedIPs.size);
                bannedIPs.each(ip -> {
                    PlayerInfo info = netServer.admins.findByIP(ip);
                    if (info == null) Log.info("  @ / (No known name or info)", ip);
                    else Log.info("  @ / Last known name: @ / ID: @", ip, info.plainLastName(), info.id);
                });
            }
        });

        serverCommands.register("admin", "<add/remove> <username/id...>", "Make a player admin.", args -> {
            var target = Find.player(args[1]);
            var info = Find.playerInfo(args[1]);
            if (notFound(info, args[1])) return;

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    netServer.admins.adminPlayer(info.id, info.adminUsid);
                    Log.info("Player @ is now admin.", info.plainLastName());
                    if (target != null) {
                        target.admin(true);
                        bundled(target, "events.server.admin");
                    }
                }
                case "remove" -> {
                    netServer.admins.unAdminPlayer(info.id);
                    Log.info("Player @ is no longer an admin.", info.plainLastName());
                    if (target != null) {
                        target.admin(false);
                        bundled(target, "events.server.unadmin");
                    }
                }
                default -> Log.err("The first parameter must be either add/remove.");
            }
        });

        serverCommands.register("admins", "List all admins.", args -> {
            var admins = netServer.admins.getAdmins();
            if (admins.isEmpty()) Log.info("No admins have been found.");
            else {
                Log.info("Admins: (@)", admins.size);
                admins.each(info -> Log.info("  @ / ID: @ / IP: @", info.plainLastName(), info.id, info.lastIP));
            }
        });

        serverCommands.register("setrank", "<uuid> <rank>", "Set a player's rank.", args -> {
            var rank = Find.rank(args[1]);
            if (notFound(rank, args[1])) return;

            Ranks.setRankNet(args[0], rank);
            Log.info("Successfully set rank of @ to @.", args[0], rank.name);
        });

        serverCommands.register("ranks", "List all ranks.", args -> {
            Log.info("Ranks: (@)", Rank.ranks.size);
            Rank.ranks.each(rank -> Log.info("  @ - @", rank.id, rank.name));
        });

        serverCommands.register("stats", "<uuid> [playtime/buildings/games] [value]", "Set a player's stats.", args -> getPlayerData(args[0]).subscribe(data -> {
            if (args.length < 3) {
                Log.info("Player: @", args[0]);
                Log.info("  Playtime: @ / Buildings Built: @ / Games Played: @", data.playTime, data.buildingsBuilt, data.gamesPlayed);
                return;
            }

            if (invalidAmount(args, 2)) return;
            int value = parseInt(args[2]);

            switch (args[1].toLowerCase()) {
                case "playtime" -> data.playTime = value;
                case "buildings" -> data.buildingsBuilt = value;
                case "games" -> data.gamesPlayed = value;
                default -> {
                    Log.err("Second argument must be playtime, buildings or games.");
                    return;
                }
            }

            setPlayerData(data).subscribe();
            Log.info("Successfully set @ of player @ to @", args[1], args[0], value);
        }));
    }
}
