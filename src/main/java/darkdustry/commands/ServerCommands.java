package darkdustry.commands;

import arc.util.*;
import darkdustry.components.*;
import darkdustry.features.Ranks;
import darkdustry.listeners.SocketEvents.ServerMessageEvent;
import darkdustry.utils.*;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.maps.Map;
import mindustry.net.Packets.KickReason;
import useful.Bundle;

import java.time.Duration;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static mindustry.server.ServerControl.*;

public class ServerCommands {

    // Зарежу дарка
    // (C) xzxADIxzx, 2023 год вне н.э.
    public static void load() {
        serverCommands.register("exit", "Exit the server application.", args -> {
            netServer.kickAll(KickReason.serverRestarting);
            app.exit();

            Log.info("Server exited.");
        });

        serverCommands.register("stop", "Stop hosting the server.", args -> {
            net.closeServer();
            state.set(State.menu);

            Log.info("Server stopped.");
        });

        serverCommands.register("host", "[map] [mode]", "Start server on selected map.", args -> {
            if (alreadyHosting()) return;

            Gamemode mode;
            if (args.length > 1) {
                mode = Find.mode(args[1]);
                if (notFound(mode, args[1])) return;
            } else {
                mode = notNullElse(Find.mode(settings.getString("lastServerMode", "")), Gamemode.survival);
                Log.info("Default mode selected to be @.", mode.name());
            }

            Map map;
            if (args.length > 0) {
                map = Find.map(args[0]);
                if (notFound(map, args[0])) return;
            } else {
                map = maps.getNextMap(mode, state.map);
                Log.info("Randomized next map to be @.", map.name());
            }

            settings.put("lastServerMode", mode.name());

            app.post(() -> {
                Log.info("Loading map...");
                instance.play(false, () -> world.loadMap(map));
                Log.info("Map loaded.");

                netServer.openServer();
            });
        });

        serverCommands.register("say", "<message...>", "Send a message to all players.", args -> {
            Log.info("&fi@: &fr&lw@", "&lcServer", "&lw" + args[0]);
            Bundle.send("commands.say.chat", args[0]);

            Socket.send(new ServerMessageEvent(config.mode.name(), "Server", stripDiscord(args[0])));
        });

        serverCommands.register("kick", "<player> <duration> [reason...]", "Kick a player.", args -> {
            var target = Find.player(args[0]);
            if (notFound(target, args[0])) return;

            var duration = parseDuration(args[1]);
            if (invalidDuration(duration)) return;

            var reason = args.length > 2 ? args[2] : "Not Specified";
            Admins.kick(target, "Console", duration.toMillis(), reason);

            Log.info("Player @ has been kicked for @ for @.", target.plainName(), Bundle.formatDuration(duration), reason);
        });

        serverCommands.register("pardon", "<player...>", "Pardon a player.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0]) || notKicked(info)) return;

            info.lastKicked = 0L;
            netServer.admins.kickedIPs.remove(info.lastIP);
            netServer.admins.dosBlacklist.remove(info.lastIP);

            Log.info("Player @ has been pardoned.", info.plainLastName());
        });

        serverCommands.register("kicks", "List of all kicked players", args -> {
            var kicked = netServer.admins.kickedIPs;
            kicked.each((ip, time) -> {
                if (time < Time.millis())
                    kicked.remove(ip);
            });

            Log.info("Kicked players: (@)", kicked.size);
            kicked.each((ip, time) -> {
                var info = netServer.admins.findByIP(ip);
                Log.info("  Name: @ / ID: @ / IP: @ / Unban date: @", info == null ? "unknown" : info.plainLastName(), info == null ? "unknown" : info.id, ip, Bundle.formatDateTime(time));
            });
        });

        serverCommands.register("ban", "<player> <duration> [reason...]", "Ban a player.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            var duration = parseDuration(args[1]);
            if (invalidDuration(duration)) return;

            var reason = args.length > 2 ? args[2] : "Not Specified";
            Admins.ban(info, "Console", duration.toMillis(), reason);

            Log.info("Player @ has been banned for @ for @.", info.plainLastName(), Bundle.formatDuration(duration), reason);
        });

        serverCommands.register("unban", "<player...>", "Unban a player.", args -> {
            var ban = Database.removeBan(args[0]);
            if (notBanned(ban)) return;

            Log.info("Player @ has been unbanned.", ban.player);
        });

        serverCommands.register("bans", "List of all banned players.", args -> {
            var banned = Database.getBanned();

            Log.info("Banned players: (@)", banned.size);
            banned.each(ban -> Log.info("  Name: @ / ID: @ / UUID: @ / IP: @ / Unban date: @", ban.player, ban.id, ban.uuid, ban.ip, Bundle.formatDateTime(1, 2, ban.unbanDate)));
        });

        serverCommands.register("admin", "<add/remove> <player...>", "Make a player admin.", args -> {
            var info = Find.playerInfo(args[1]);
            if (notFound(info, args[1])) return;

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    var target = Find.playerByUUID(info.id);
                    if (target != null) {
                        target.admin(true);
                        Bundle.send(target, "events.server.admin");
                    }

                    netServer.admins.adminPlayer(info.id, target == null ? info.adminUsid : target.usid());
                    Log.info("Player @ is now admin.", info.plainLastName());
                }
                case "remove" -> {
                    var target = Find.playerByUUID(info.id);
                    if (target != null) {
                        target.admin(false);
                        Bundle.send(target, "events.server.unadmin");
                    }

                    netServer.admins.unAdminPlayer(info.id);
                    Log.info("Player @ is no longer an admin.", info.plainLastName());
                }
                default -> Log.err("The first parameter must be either add or remove.");
            }
        });

        serverCommands.register("admins", "List all admins.", args -> {
            var admins = netServer.admins.getAdmins();

            Log.info("Admins: (@)", admins.size);
            admins.each(admin -> Log.info("  Name: @ / ID: @ / IP: @", admin.plainLastName(), admin.id, admin.lastIP));
        });

        serverCommands.register("stats", "<player...>", "Look up a player stats.", args -> {
            var data = Find.playerData(args[0]);
            if (notFound(data, args[0])) return;

            Log.info("Player Stats");
            Log.info("  Name: @", data.plainName());
            Log.info("  UUID: @", data.uuid);
            Log.info("  ID: @", data.id);
            Log.info("  Rank: @", data.rank.name());
            Log.info("  Playtime: @", Bundle.formatDuration(Duration.ofMinutes(data.playTime)));
            Log.info("  Blocks placed: @", data.blocksPlaced);
            Log.info("  Blocks broken: @", data.blocksBroken);
            Log.info("  Waves survived: @", data.wavesSurvived);
            Log.info("  Games played: @", data.gamesPlayed);
            Log.info("  Attack wins: @", data.attackWins);
            Log.info("  PvP wins: @", data.pvpWins);
            Log.info("  Hexed wins: @", data.hexedWins);
        });

        serverCommands.register("setrank", "<player> <rank>", "Set a player's rank.", args -> {
            var data = Find.playerData(args[0]);
            if (notFound(data, args[0])) return;

            var rank = Find.rank(args[1]);
            if (notFound(rank, args[1])) return;

            data.rank = rank;

            var target = Find.playerByUUID(data.uuid);
            if (target != null) {
                Cache.put(target, data);
                Ranks.name(target, data);
            }

            Database.savePlayerData(data);
            Log.info("Successfully set rank of @ to @.", data.plainName(), rank.name());
        });
    }
}