package darkdustry.commands;

import arc.util.Log;
import arc.util.Time;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Find;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.maps.Map;

import java.time.Duration;
import java.time.Instant;

import static arc.Core.app;
import static arc.Core.settings;
import static darkdustry.PluginVars.kickDuration;
import static darkdustry.PluginVars.serverCommands;
import static darkdustry.components.Database.getPlayerData;
import static darkdustry.components.Database.updatePlayerData;
import static darkdustry.components.EffectsCache.updateEffects;
import static darkdustry.discord.Bot.botChannel;
import static darkdustry.discord.Bot.sendMessage;
import static darkdustry.features.Ranks.updateRank;
import static darkdustry.utils.Administration.kick;
import static darkdustry.utils.Checks.alreadyHosting;
import static darkdustry.utils.Checks.notFound;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static useful.Bundle.bundled;
import static useful.Bundle.sendToChat;

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
            sendToChat("commands.say.chat", args[0]);
            sendMessage(botChannel, "Server", args[0]);
        });

        serverCommands.register("kick", "<ID/username...>", "Kick a player.", args -> {
            var target = Find.player(args[0]);
            if (notFound(target, args[0])) return;

            kick(target, kickDuration, true, "kick.kicked");
            sendToChat("events.server.kick", target.coloredName());

            Log.info("Player @ has been kicked.", target.plainName());
        });

        serverCommands.register("ban", "<ID/username/uuid/ip...>", "Ban a player.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            netServer.admins.banPlayerID(info.id);
            netServer.admins.banPlayerIP(info.lastIP);

            var target = Find.playerByUuid(info.id);
            if (target != null) {
                kick(target, 0, true, "kick.banned");
                sendToChat("events.server.ban", target.coloredName());
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

        serverCommands.register("bans", "List all banned IPs and IDs.", args -> {
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
                        bundled(target, "events.server.admin");
                    }

                    Log.info("Player @ is now admin.", info.plainLastName());
                }
                case "remove" -> {
                    netServer.admins.unAdminPlayer(info.id);

                    var target = Find.playerByUuid(info.id);
                    if (target != null) {
                        target.admin(false);
                        bundled(target, "events.server.unadmin");
                    }

                    Log.info("Player @ is no longer an admin.", info.plainLastName());
                }
                default -> Log.err("The first parameter must be either add or remove.");
            }
        });

        serverCommands.register("admins", "List all admins.", args -> {
            Log.info("Admins:");
            netServer.admins.getAdmins().each(info -> Log.info("  @ / ID: @ / IP: @", info.plainLastName(), info.id, info.lastIP));
        });

        serverCommands.register("stats", "<ID/username/uuid/ip...>", "Look up a player stats.", args -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            getPlayerData(info.id).subscribe(data -> {
                Log.info("Player '@' UUID @", data.name, data.uuid);
                Log.info("  Rank: @", data.rank.name());
                Log.info("  Games played: @", data.gamesPlayed);
                Log.info("  PvP wins: @", data.pvpWins);
                Log.info("  Hexed wins: @", data.hexedWins);
                Log.info("  Waves survived: @", data.wavesSurvived);
                Log.info("  Blocks placed: @", data.blocksPlaced);
                Log.info("  Blocks broken: @", data.blocksBroken);
                Log.info("  Playtime: @ minutes", data.playTime);
            });
        });

        serverCommands.register("setrank", "<rank> <ID/username/uuid/ip...>", "Set a player's rank.", args -> {
            var rank = Find.rank(args[0]);
            if (notFound(rank, args[0])) return;

            var info = Find.playerInfo(args[1]);
            if (notFound(info, args[1])) return;

            updatePlayerData(info.id, data -> {
                data.rank = rank;

                var target = Find.playerByUuid(info.id);
                if (target != null) {
                    updateRank(target, data);
                    updateEffects(target, data.effects);
                }

                Log.info("Successfully set rank of @ to @.", info.plainLastName(), rank.name());
            });
        });
    }
}