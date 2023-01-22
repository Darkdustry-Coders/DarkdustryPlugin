package darkdustry.commands;

import arc.util.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.features.Ranks;
import darkdustry.utils.Find;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.maps.*;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.features.Effects.updateEffects;
import static darkdustry.features.Ranks.updateRank;
import static darkdustry.utils.Administration.kick;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static useful.Bundle.*;

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
                mode = notNullElse(Find.mode(settings.getString("lastServerMode")), Gamemode.survival);
                Log.info("Default mode selected to be @.", mode.name());
            }

            Map map;
            if (args.length > 0) {
                map = Find.map(args[0]);
                if (notFound(map, args[0])) return;
            } else {
                map = maps.getShuffleMode().next(mode, state.map);
                Log.info("Randomized next map to be @.", map.name());
            }

            settings.put("lastServerMode", mode.name());

            app.post(() -> {
                try {
                    Log.info("Loading map...");
                    reloadWorld(() -> world.loadMap(map, map.applyRules(mode)));
                    Log.info("Map loaded.");

                    netServer.openServer();
                } catch (MapException e) {
                    Log.err("@: @", e.map.name(), e.getMessage());
                }
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

        serverCommands.register("kicks", "List all kicked players.", args -> {
            var kicks = netServer.admins.kickedIPs;
            kicks.each((ip, time) -> {
                if (Time.timeSinceMillis(time) > 0)
                    kicks.remove(ip);
            });

            if (kicks.isEmpty())
                Log.info("No kicked players have been found.");
            else {
                Log.info("Kicked players: (@)", kicks.size);
                kicks.each((ip, time) -> {
                    var info = netServer.admins.findByIP(ip);
                    if (info == null) Log.info("  @ / @ / (No known name or info)", ip, formatLongDate(time));
                    else
                        Log.info("  @ / End: @ / Name: @ / ID: @", ip, formatLongDate(time), info.plainLastName(), info.id);
                });
            }
        });

        serverCommands.register("ban", "<ID/username/uuid/ip...>", "Ban a player.", args -> {
            var target = Find.player(args[0]);
            var playerInfo = Find.playerInfo(args[0]);
            if (notFound(playerInfo, args[0])) return;

            netServer.admins.banPlayerID(playerInfo.id);
            netServer.admins.banPlayerIP(playerInfo.lastIP);
            if (target != null) {
                kick(target, 0, true, "kick.banned");
                sendToChat("events.server.ban", target.coloredName());
            }

            Log.info("Player @ has been banned.", playerInfo.plainLastName());
        });

        serverCommands.register("unban", "<uuid/ip...>", "Unban a player.", args -> {
            var playerInfo = Find.playerInfo(args[0]);
            if (notFound(playerInfo, args[0])) return;

            netServer.admins.unbanPlayerID(playerInfo.id);
            playerInfo.ips.each(netServer.admins::unbanPlayerIP);

            playerInfo.lastKicked = 0L;
            playerInfo.ips.each(netServer.admins.kickedIPs::remove);

            Log.info("Player @ has been unbanned.", playerInfo.plainLastName());
        });

        serverCommands.register("bans", "List all banned IPs and IDs.", args -> {
            var bannedIDs = netServer.admins.getBanned();
            if (bannedIDs.isEmpty())
                Log.info("No ID-banned players have been found.");
            else {
                Log.info("ID-banned players: (@)", bannedIDs.size);
                bannedIDs.each(info -> Log.info("  @ / Name: @", info.id, info.plainLastName()));
            }

            var bannedIPs = netServer.admins.getBannedIPs();
            if (bannedIPs.isEmpty())
                Log.info("No IP-banned players have been found.");
            else {
                Log.info("IP-banned players: (@)", bannedIPs.size);
                bannedIPs.each(ip -> {
                    var info = netServer.admins.findByIP(ip);
                    if (info == null) Log.info("  @ / (No known name or info)", ip);
                    else Log.info("  @ / Name: @ / ID: @", ip, info.plainLastName(), info.id);
                });
            }
        });

        serverCommands.register("admin", "<add/remove> <ID/username/uuid/ip...>", "Make a player admin.", args -> {
            var target = Find.player(args[1]);
            var playerInfo = Find.playerInfo(args[1]);
            if (notFound(playerInfo, args[1])) return;

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    netServer.admins.adminPlayer(playerInfo.id, playerInfo.adminUsid);
                    if (target != null) {
                        target.admin(true);
                        bundled(target, "events.server.admin");
                    }

                    Log.info("Player @ is now admin.", playerInfo.plainLastName());
                }
                case "remove" -> {
                    netServer.admins.unAdminPlayer(playerInfo.id);
                    if (target != null) {
                        target.admin(false);
                        bundled(target, "events.server.unadmin");
                    }

                    Log.info("Player @ is no longer an admin.", playerInfo.plainLastName());
                }
                default -> Log.err("The first parameter must be either add or remove.");
            }
        });

        serverCommands.register("admins", "List all admins.", args -> {
            var admins = netServer.admins.getAdmins();
            if (admins.isEmpty())
                Log.info("No admins have been found.");
            else {
                Log.info("Admins: (@)", admins.size);
                admins.each(info -> Log.info("  @ / ID: @ / IP: @", info.plainLastName(), info.id, info.lastIP));
            }
        });

        serverCommands.register("stats", "<ID/username/uuid/ip...>", "Look up a player stats.", args -> {
            var playerInfo = Find.playerInfo(args[0]);
            if (notFound(playerInfo, args[0])) return;

            getPlayerData(playerInfo.id).subscribe(data -> {
                Log.info("Player '@' UUID @", data.name, data.uuid);
                Log.info("  Rank: @", data.rank.name());
                Log.info("  Games played: @", data.gamesPlayed);
                Log.info("  PvP wins: @", data.pvpWins);
                Log.info("  PvP losses: @", data.pvpLosses);
                Log.info("  Waves survived: @", data.wavesSurvived);
                Log.info("  Blocks placed: @", data.blocksPlaced);
                Log.info("  Blocks broken: @", data.blocksBroken);
                Log.info("  Playtime: @ minutes", data.playTime);
            });
        });

        serverCommands.register("setrank", "<rank> <ID/username/uuid/ip...>", "Set a player's rank.", args -> {
            var rank = Find.rank(args[0]);
            if (notFound(rank, args[0])) return;

            var target = Find.player(args[1]);
            var playerInfo = Find.playerInfo(args[1]);
            if (notFound(playerInfo, args[1])) return;

            updatePlayerData(playerInfo.id, data -> {
                data.rank = rank;

                if (target != null) {
                    updateRank(target, data);
                    updateEffects(target, data);
                }

                Log.info("Successfully set rank of @ to @.", playerInfo.plainLastName(), rank.name());
            });
        });

        serverCommands.register("ranks", "List all ranks.", args -> {
            Log.info("Ranks: (@)", Ranks.all.size);
            Ranks.all.each(rank -> Log.info("  @ - @", rank.ordinal(), rank.name()));
        });
    }
}