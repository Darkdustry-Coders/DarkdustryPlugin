package darkdustry.commands;

import arc.util.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.components.*;
import darkdustry.discord.Bot;
import darkdustry.features.Ranks;
import darkdustry.utils.*;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.maps.Map;
import useful.Bundle;

import static arc.Core.*;
import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
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

        serverCommands.register("stop", "Stop hosting the server.", args -> {
            net.closeServer();
            state.set(State.menu);
            Log.info("Stopped server.");
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
            Bot.sendMessage(botChannel, "Server", args[0]);
        });

        serverCommands.register("kick", "<ID/username...>", "Kick a player.", args -> {
            var target = Find.player(args[0]);
            if (notFound(target, args[0])) return;

            Admins.kick(target, kickDuration, true, "kick.kick");
            Bundle.send("events.server.kick", target.coloredName());

            Log.info("Player @ has been kicked.", target.plainName());
        });

        serverCommands.register("ban", "<duration> <ID/username/uuid/ip...>", "Ban a player.", args -> {
            int days = parseInt(args[0]);
            if (invalidDuration(days, 0, 365)) return;

            var info = Find.playerInfo(args[1]);
            if (notFound(info, args[1])) return;

            long duration = days * 24 * 60 * 60 * 1000L;
            Admins.ban(info.id, info.lastIP, duration);

            var target = Find.playerByUuid(info.id);
            if (target != null) {
                Admins.kick(target, duration, true, "kick.ban");
                Bundle.send("events.server.ban", target.coloredName());
            }

            Log.info("Player @ has been banned.", info.plainLastName());
        });

        serverCommands.register("unban", "<uuid/ip...>", "Unban a player.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            netServer.admins.unbanPlayerID(info.id);
            info.ips.each(netServer.admins::unbanPlayerIP);

            info.lastKicked = 0L;
            info.ips.each(netServer.admins.kickedIPs::remove);

            Log.info("Player @ has been unbanned.", info.plainLastName());
        });

        serverCommands.register("bans", "List of all banned players.", args -> {
            Log.info("ID-banned players:");
            netServer.admins.getBanned().each(info -> Log.info("  Name: @ / ID: @ / IP: @", info.plainLastName(), info.id, info.lastIP));

            Log.info("IP-banned players:");
            netServer.admins.getBannedIPs().each(ip -> {
                var info = netServer.admins.findByIP(ip);
                Log.info("  Name: @ / ID: @ / IP: @", info != null ? info.plainLastName() : "unknown", info != null ? info.id : "unknown", ip);
            });

            Log.info("Temp-banned players:");
            netServer.admins.kickedIPs.each((ip, time) -> {
                if (Time.timeSinceMillis(time) > 0) return;

                var info = netServer.admins.findByIP(ip);
                Log.info("  Name: @ / ID: @ / IP: @ / Unban date: @", info != null ? info.plainLastName() : "unknown", info != null ? info.id : "unknown", ip, formatLongDate(time));
            });
        });

        serverCommands.register("admin", "<add/remove> <ID/username/uuid/ip...>", "Make a player admin.", args -> {
            var info = Find.playerInfo(args[1]);
            if (notFound(info, args[1])) return;

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    netServer.admins.adminPlayer(info.id, info.adminUsid);

                    var target = Find.playerByUuid(info.id);
                    if (target != null) {
                        target.admin(true);
                        Bundle.send(target, "events.server.admin");
                    }

                    Log.info("Player @ is now admin.", info.plainLastName());
                }
                case "remove" -> {
                    netServer.admins.unAdminPlayer(info.id);

                    var target = Find.playerByUuid(info.id);
                    if (target != null) {
                        target.admin(false);
                        Bundle.send(target, "events.server.unadmin");
                    }

                    Log.info("Player @ is no longer an admin.", info.plainLastName());
                }
                default -> Log.err("The first parameter must be either add or remove.");
            }
        });

        serverCommands.register("admins", "List all admins.", args -> {
            Log.info("Admins:");
            netServer.admins.getAdmins().each(info -> Log.info("  Name: @ / ID: @ / IP: @", info.plainLastName(), info.id, info.lastIP));
        });

        serverCommands.register("stats", "<ID/username/uuid/ip...>", "Look up a player stats.", args -> {
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

        serverCommands.register("setrank", "<rank> <ID/username/uuid/ip...>", "Set a player's rank.", args -> {
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