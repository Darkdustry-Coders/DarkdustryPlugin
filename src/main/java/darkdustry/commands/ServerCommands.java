package darkdustry.commands;

import arc.Core;
import arc.struct.Seq;
import arc.util.Log;
import darkdustry.components.Database;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import mindustry.net.Administration.PlayerInfo;
import darkdustry.discord.Bot;
import darkdustry.features.Ranks;
import darkdustry.features.Ranks.Rank;
import darkdustry.utils.Find;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static arc.Core.*;
import static darkdustry.components.Database.jedisPool;
import static darkdustry.components.Database.setPlayerData;
import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;

public class ServerCommands {

    // Зарежу дарка
    // (C) xzxADIxzx, 2023 год вне н.э.
    public static void load() {
        serverCommands.register("exit", "Exit the server application.", args -> {
            Log.info("Shutting down server.");
            System.exit(2);
        });

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

            Core.settings.put("lastServerMode", mode.name());

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
            Bot.sendMessage(Bot.botChannel, "Сервер » @", args[0]);
        });

        serverCommands.register("kick", "<username/id...>", "Kick a player.", args -> {
            Player target = Find.player(args[0]);
            if (notFound(target, args)) return;
            
            kick(target, kickDuration, true, "kick.kicked");
            Log.info("Player @ has been kicked.", target.name);
            sendToChat("events.server.kick", target.name);
        });

        serverCommands.register("pardon", "<uuid/ip>", "Pardon a kicked player.", args -> {
            PlayerInfo info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            info.lastKicked = 0L;
            info.ips.each(netServer.admins.kickedIPs::remove);
            Log.info("Player @ has been pardoned.", info.lastName);
        });

        serverCommands.register("ban", "<username/uuid/ip...>", "Ban a player.", args -> {
            Player target = Find.player(args[0]);
            if (target != null) {
                netServer.admins.banPlayer(target.uuid());
                kick(target, 0, true, "kick.banned");
                Log.info("Player @ has been banned.", target.name);
                sendToChat("events.server.ban", target.name);
                return;
            }

            PlayerInfo info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            netServer.admins.banPlayer(info.id);
            Log.info("Player @ has been banned.", info.lastName);
            Groups.player.each(player -> netServer.admins.isIDBanned(player.uuid()) || netServer.admins.isIPBanned(player.ip()), player -> {
                kick(player, 0, true, "kick.banned");
                sendToChat("events.server.ban", player.name);
            });
        });

        serverCommands.register("unban", "<uuid/ip>", "Unban a player.", args -> {
            PlayerInfo info = Find.playerInfo(args[0]);
            if (notFound(info, args[0])) return;

            netServer.admins.unbanPlayerID(info.id);
            info.ips.each(netServer.admins::unbanPlayerIP);
            Log.info("Player @ has been unbanned.", info.lastName);
        });

        serverCommands.register("bans", "List all banned IPs and IDs.", args -> {
            Seq<PlayerInfo> bannedIDs = netServer.admins.getBanned();
            if (bannedIDs.isEmpty())
                Log.info("No ID-banned players have been found.");
            else {
                Log.info("ID-banned players: (@)", bannedIDs.size);
                bannedIDs.each(ban -> Log.info("  @ / Last known name: @", ban.id, ban.lastName));
            }

            Seq<String> bannedIPs = netServer.admins.getBannedIPs();
            if (bannedIPs.isEmpty())
                Log.info("No IP-banned players have been found.");
            else {
                Log.info("IP-banned players: (@)", bannedIPs.size);
                bannedIPs.each(ip -> {
                    PlayerInfo info = netServer.admins.findByIP(ip);
                    if (info == null) Log.info("  @ / (No known name or info)", ip);
                    else Log.info("  @ / Last known name: @ / ID: @", ip, info.lastName, info.id);
                });
            }
        });

        serverCommands.register("admin", "<add/remove> <username/id...>", "Make a player admin.", args -> {
            Player target = Find.player(args[1]);
            PlayerInfo info = Find.playerInfo(args[1]);
            if (notFound(info, args[1])) return;

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    netServer.admins.adminPlayer(info.id, info.adminUsid);
                    Ranks.setRankNet(info.id, Ranks.admin);
                    Log.info("Player @ is now admin.", info.lastName);
                    if (target != null) {
                        target.admin(true);
                        bundled(target, "events.server.admin");
                    }
                }
                case "remove" -> {
                    netServer.admins.unAdminPlayer(info.id);
                    Ranks.setRankNet(info.id, Ranks.player);
                    Log.info("Player @ is no longer an admin.", info.lastName);
                    if (target != null) {
                        target.admin(false);
                        bundled(target, "events.server.unadmin");
                    }
                }
                default -> Log.err("The first parameter must be either add/remove.");
            }
        });

        serverCommands.register("admins", "List all admins.", args -> {
            Seq<PlayerInfo> admins = netServer.admins.getAdmins();
            if (admins.isEmpty())  Log.info("No admins have been found.");
            else {
                Log.info("Admins: (@)", admins.size);
                admins.each(admin -> Log.info("  @ / ID: @ / IP: @", admin.lastName, admin.id, admin.lastIP));
            }
        });

        serverCommands.register("setrank", "<uuid> <rank>", "Set a player's rank.", args -> {
            if (noData(args[0])) return;

            Rank rank = Find.rank(args[1]);
            if (notFound(rank, args[1])) return;

            Ranks.setRankNet(args[0], rank);
            Log.info("Successfully set rank of @ to @.", args[0], rank.name);
        });

        serverCommands.register("ranks", "List all ranks.", args -> {
            Log.info("Ranks: (@)", Rank.ranks.size);
            Rank.ranks.each(rank -> Log.info("  @ - @", rank.id, rank.name));
        });

        serverCommands.register("reformat-db", "Testing only", args -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Set<String> keys = jedis.keys("*");
                if (keys.size() == 0) {
                    return;
                }

                AtomicInteger correct = new AtomicInteger();
                AtomicInteger incorrect = new AtomicInteger();

                keys.forEach(key -> {
                    try {
                        if ("AAAAAAAAAAAAAAAAZSLfaQ==".length() == key.length()) {
                            Database.PlayerData data = gson.fromJson(jedis.get(key), Database.PlayerData.class);
                            data.uuid = key;
                            data.playTime = data.playTime / 60;
                            setPlayerData(data);
                            correct.getAndIncrement();
                        } else {
                            jedis.del(key);
                            incorrect.getAndIncrement();
                        }
                    } catch (Exception ignored) {
                        Log.err("Error.");
                    }
                });

                Log.info("Found @ UUIDs, @ correct, @ incorrect.", keys.size(), correct.get(), incorrect.get());
                Log.info("DATABASE CONVERTED.");
            }
        });
    }
}
