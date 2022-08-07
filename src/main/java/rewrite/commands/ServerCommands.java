package rewrite.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import mindustry.net.Administration.PlayerInfo;
import rewrite.discord.Bot;
import rewrite.utils.Find;

import java.util.Locale;

import static arc.Core.*;
import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.utils.Checks.*;
import static rewrite.utils.Utils.*;

public class ServerCommands extends Commands<NullPointerException> {

    public ServerCommands(CommandHandler handler, Locale def) {
        super(handler, def);

        for (String command : new String[] {"fillitems", "pause", "shuffle", "runwave"})
            handler.removeCommand(command);

        register("exit", args -> {
            Log.info("Shutting down server.");
            System.exit(2);
        });

        register("stop", args -> {
            net.closeServer();
            state.set(State.menu);
            Log.info("Stopped server.");
        });

        register("host", args -> {
            if (isLaunched()) return;

            Gamemode mode;
            if (args.length > 1) {
                mode = Find.mode(args[1]);
                if (notFound(mode, args)) return;
            } else {
                mode = Gamemode.survival;
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

            logic.reset();

            app.post(() -> {
                try {
                    Log.info("Loading map...");

                    world.loadMap(map, map.applyRules(mode));
                    state.rules = map.applyRules(mode);
                    logic.play();
                    netServer.openServer();

                    Log.info("Map loaded.");
                } catch (MapException exception) {
                    Log.err("@: @", exception.map.name(), exception.getMessage());
                }
            });
        });

        register("say", args -> {
            Log.info("&fi@: &fr&lw@", "&lcServer", "&lw" + args[0]);
            sendToChat("commands.say.chat", args[0]);
            Bot.sendMessage(Bot.botChannel, "Сервер » @", args[0]);
        });

        register("kick", args -> {
            Player target = Find.player(args[0]);
            if (notFound(player, args)) return;
            
            kick(target, kickDuration, true, "kick.kicked");
            Log.info("Player @ has been kicked.", target.name);
            sendToChat("events.server.kick", target.name);
        });

        register("pardon", args -> {
            PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
            if (info == null) info = netServer.admins.findByIP(args[0]);
            if (notFound(info, args)) return;

            info.lastKicked = 0L;
            info.ips.each(netServer.admins.kickedIPs::remove);
            Log.info("Player @ has been pardoned.", info.lastName);
        });

        register("ban", args -> {
            Player target = Find.player(args[0]);
            if (target != null) {
                netServer.admins.banPlayer(target.uuid());
                kick(target, 0, true, "kick.banned");
                Log.info("Player @ has been banned.", target.name);
                sendToChat("events.server.ban", target.name);
                return;
            }

            PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
            if (info == null) info = netServer.admins.findByIP(args[0]);
            if (notFound(info, args)) return;

            netServer.admins.banPlayer(info.id);
            Log.info("Player @ has been banned.", info.lastName);
            Groups.player.each(player -> netServer.admins.isIDBanned(player.uuid()) || netServer.admins.isIPBanned(player.ip()), player -> {
                kick(player, 0, true, "kick.banned");
                sendToChat("events.server.ban", player.name);
            });
        });

        register("unban", args -> {
            PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
            if (info == null) info = netServer.admins.findByIP(args[0]);
            if (notFound(info, args)) return;

            netServer.admins.unbanPlayerID(info.id);
            info.ips.each(netServer.admins::unbanPlayerIP);
            Log.info("Player @ has been unbanned.", info.lastName);
        });

        register("bans", args -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
                netServer.admins.getBanned().each(info -> netServer.admins.unbanPlayerID(info.id));
                netServer.admins.getBannedIPs().each(netServer.admins::unbanPlayerIP);
                Log.info("Ban list has been cleared.");
                return;
            }

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
    }
}
