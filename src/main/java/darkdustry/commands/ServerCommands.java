package darkdustry.commands;

import arc.util.*;
import darkdustry.components.*;
import darkdustry.discord.Bot;
import darkdustry.features.Ranks;
import darkdustry.utils.*;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.maps.Map;
import mindustry.net.Packets.KickReason;
import useful.Bundle;

import static arc.Core.*;
import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static java.util.concurrent.TimeUnit.*;
import static mindustry.Vars.*;

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
                reloadWorld(() -> world.loadMap(map, map.applyRules(mode)));
                Log.info("Map loaded.");

                netServer.openServer();
            });
        });

        serverCommands.register("say", "<message...>", "Send a message to all players.", args -> {
            Log.info("&fi@: &fr&lw@", "&lcServer", "&lw" + args[0]);
            Bundle.send("commands.say.chat", args[0]);
            Bot.sendMessage("Server", args[0]);
        });

        serverCommands.register("kick", "<ID/name> <minutes> [reason...]", "Kick a player.", args -> {
            var target = Find.player(args[0]);
            if (notFound(target, args[0])) return;

            int minutes = parseInt(args[1]);
            if (invalidDuration(minutes, 1, 1440)) return;

            var reason = args.length > 2 ? args[2] : "Not Specified";
            Admins.kick(target, "Console", MINUTES.toMillis(minutes), reason);

            Log.info("Player @ has been kicked for @ minutes.", target.plainName(), minutes);
        });

        serverCommands.register("pardon", "<uuid/ip...>", "Pardon a player.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            info.lastKicked = 0L;
            info.ips.each(netServer.admins.kickedIPs::remove);

            Log.info("Player @ has been pardoned.", info.plainLastName());
        });

        serverCommands.register("kicks", "List of all kicked players", args -> {
            var kicks = netServer.admins.kickedIPs;
            kicks.each((ip, time) -> {
                if (time <= Time.millis())
                    kicks.remove(ip);
            });

            Log.info("Kicked players: (@)", kicks.size);
            kicks.each((ip, time) -> {
                var info = netServer.admins.findByIP(ip);
                Log.info("  Name: @ / ID: @ / IP: @ / Unban date: @", info != null ? info.plainLastName() : "unknown", info != null ? info.id : "unknown", ip, formatDateTime(time));
            });
        });

        serverCommands.register("ban", "<ID/name/uuid/ip> <days> [reason...]", "Ban a player.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            int days = parseInt(args[1]);
            if (invalidDuration(days, 1, 365)) return;

            var reason = args.length > 2 ? args[2] : "Not Specified";
            Admins.ban(info, "Console", DAYS.toMillis(days), reason);

            Log.info("Player @ has been banned for @ days.", info.plainLastName(), days);
        });

        serverCommands.register("unban", "<name/uuid/ip...>", "Unban a player.", args -> {
            var ban = Database.removeBan(args[0]);
            if (notUnbanned(ban)) return;

            Log.info("Player @ has been unbanned.", ban.player);
        });

        serverCommands.register("bans", "List of all banned players.", args -> {
            var bans = Database.getBans();

            Log.info("Banned players: (@)", bans.size);
            bans.each(ban -> Log.info("  Name: @ / UUID: @ / IP: @ / Unban date: @", ban.player, ban.uuid, ban.ip, formatDateTime(ban.unbanDate.getTime())));
        });

        serverCommands.register("admin", "<add/remove> <ID/name/uuid/ip>", "Make a player admin.", args -> {
            var info = Find.playerInfo(args[1]);
            if (notFound(info, args[1])) return;

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    var target = Find.playerByUuid(info.id);
                    if (target != null) {
                        target.admin(true);
                        Bundle.send(target, "events.server.admin");
                    }

                    netServer.admins.adminPlayer(info.id, target == null ? info.adminUsid : target.usid());
                    Log.info("Player @ is now admin.", info.plainLastName());
                }
                case "remove" -> {
                    var target = Find.playerByUuid(info.id);
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

        serverCommands.register("stats", "<ID/name/uuid/ip>", "Look up a player stats.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            var data = Database.getPlayerData(info.id);
            Log.info("Player '@' UUID @", data.name, data.uuid);
            Log.info("  Rank: @", data.rank.name());
            Log.info("  Playtime: @ minutes", data.playTime);
            Log.info("  Blocks placed: @", data.blocksPlaced);
            Log.info("  Blocks broken: @", data.blocksBroken);
            Log.info("  Waves survived: @", data.wavesSurvived);
            Log.info("  Games played: @", data.gamesPlayed);
            Log.info("  Attack wins: @", data.attackWins);
            Log.info("  PvP wins: @", data.pvpWins);
            Log.info("  Hexed wins: @", data.hexedWins);
        });

        serverCommands.register("setrank", "<rank> <ID/name/uuid/ip...> ", "Set a player's rank.", args -> {
            var rank = Find.rank(args[0]);
            if (notFound(rank, args[0])) return;

            var info = Find.playerInfo(args[1]);
            if (notFound(info, args[1])) return;

            var data = Database.getPlayerData(info.id);
            data.rank = rank;

            var target = Find.playerByUuid(info.id);
            if (target != null) {
                Cache.put(target, data);
                Ranks.name(target, data);
            }

            Database.savePlayerData(data);
            Log.info("Successfully set rank of @ to @.", info.plainLastName(), rank.name());
        });
    }
}