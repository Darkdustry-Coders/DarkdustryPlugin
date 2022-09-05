package darkdustry.listeners;

import arc.Events;
import arc.util.CommandHandler.*;
import arc.util.*;
import darkdustry.utils.Find;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.net.NetConnection;
import mindustry.net.Packets.*;

import static arc.util.Strings.levenshtein;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.format;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class NetHandlers {

    public static String invalidResponse(Player player, CommandResponse response) {
        var locale = Find.locale(player.locale);
        if (response.type == ResponseType.manyArguments)
            return format("commands.unknown.many-arguments", locale, response.command.text, response.command.paramText);
        if (response.type == ResponseType.fewArguments)
            return format("commands.unknown.few-arguments", locale, response.command.text, response.command.paramText);

        var closest = clientCommands.getCommandList()
                .map(command -> command.text)
                .filter(command -> levenshtein(command, response.runCommand) < 3)
                .min(command -> levenshtein(command, response.runCommand));

        return closest != null ? format("commands.unknown.closest", locale, closest) : format("commands.unknown", locale);
    }

    public static void connect(NetConnection con, Connect packet) {
        Events.fire(new ConnectionEvent(con));
    }

    public static void packet(NetConnection con, ConnectPacket packet) {
        con.connectTime = Time.millis();

        Events.fire(new ConnectPacketEvent(con, packet));

        String uuid = con.uuid = packet.uuid,
                usid = con.usid = packet.usid,
                ip = con.address,
                locale = notNullElse(packet.locale, defaultLanguage),
                name = Reflect.invoke(netServer, "fixName", Structs.arr(packet.name), String.class);

        con.mobile = packet.mobile;
        con.modclient = packet.version == -1;

        if (con.hasBegunConnecting || Groups.player.contains(player -> player.uuid().equals(uuid) || player.usid().equals(usid))) {
            kick(con, 0, false, "kick.already-connected", locale);
            return;
        }

        con.hasBegunConnecting = true;

        if (Groups.player.count(player -> player.ip().equals(ip)) >= maxIdenticalIPs) {
            kick(con, 0, false, "kick.too-many-connections", locale);
            return;
        }

        if (netServer.admins.isIDBanned(uuid) || netServer.admins.isIPBanned(ip) || netServer.admins.isSubnetBanned(ip)) {
            kick(con, 0, true, "kick.banned", locale);
            return;
        }

        if (netServer.admins.getKickTime(uuid, ip) > Time.millis()) {
            kick(con, netServer.admins.getKickTime(uuid, ip) - Time.millis(), true, "kick.recent-kick", locale);
            return;
        }

        if (netServer.admins.getPlayerLimit() > 0 && Groups.player.size() >= netServer.admins.getPlayerLimit()) {
            kick(con, 0, false, "kick.player-limit", locale, netServer.admins.getPlayerLimit());
            return;
        }

        var extraMods = packet.mods.copy();
        var missingMods = mods.getIncompatibility(extraMods);

        if (extraMods.any()) {
            kick(con, 0, false, "kick.extra-mods", locale, extraMods.toString("\n> "));
            return;
        }

        if (missingMods.any()) {
            kick(con, 0, false, "kick.missing-mods", locale, missingMods.toString("\n> "));
            return;
        }

        var info = netServer.admins.getInfo(uuid);

        if (!netServer.admins.isWhitelisted(uuid, usid)) {
            info.lastName = name;
            info.lastIP = ip;
            info.adminUsid = usid;
            info.names.addUnique(name);
            info.ips.addUnique(ip);
            kick(con, 0, false, "kick.not-whitelisted", locale, discordServerUrl);
            return;
        }

        if (packet.versionType == null || (packet.version == -1 && !netServer.admins.allowsCustomClients())) {
            kick(con, 0, false, "kick.custom-client", locale);
            return;
        }

        if (packet.version != mindustryVersion && packet.version != -1 && mindustryVersion != -1 && !packet.versionType.equals("bleeding-edge")) {
            kick(con, 0, false, packet.version > mindustryVersion ? "kick.server-outdated" : "kick.client-outdated", locale, packet.version, mindustryVersion);
            return;
        }

        if (name.isBlank()) {
            kick(con, 0, false, "kick.name-is-empty", locale);
            return;
        }

        if (con.kicked) return;

        netServer.admins.updatePlayerJoined(uuid, ip, name);

        Player player = Player.create();
        player.con(con);
        player.name(name);
        player.locale(locale);
        player.admin(netServer.admins.isAdmin(uuid, usid));
        player.color.set(packet.color).a(1f);

        con.player = player;

        if (!player.admin && !info.admin) info.adminUsid = usid;

        player.team(netServer.assignTeam(player));
        netServer.sendWorldData(player);

        Events.fire(new PlayerConnect(player));
    }
}
