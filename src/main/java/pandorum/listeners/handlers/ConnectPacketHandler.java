package pandorum.listeners.handlers;

import arc.Events;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.Colors;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import mindustry.core.Version;
import mindustry.game.EventType.ConnectPacketEvent;
import mindustry.game.EventType.PlayerConnect;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.NetConnection;
import mindustry.net.Packets.ConnectPacket;
import pandorum.components.Bundle;
import pandorum.util.Utils;

import static mindustry.Vars.*;
import static pandorum.PluginVars.*;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.millisecondsToMinutes;

public class ConnectPacketHandler implements Cons2<NetConnection, ConnectPacket> {

    public void get(NetConnection con, ConnectPacket packet) {
        if (con.kicked) return;

        Events.fire(new ConnectPacketEvent(con, packet));

        con.connectTime = Time.millis();

        String locale = Utils.notNullElse(packet.locale, defaultLocale);
        String uuid = packet.uuid;
        String usid = packet.usid;
        String ip = con.address;
        String name = fixName(packet.name);

        if (con.hasBegunConnecting || uuid == null || usid == null) {
            con.kick(Bundle.format("kick.already-connected", findLocale(locale)), 0);
            return;
        }

        con.hasBegunConnecting = true;

        if (netServer.admins.isIDBanned(uuid) || netServer.admins.isIPBanned(ip) || netServer.admins.isSubnetBanned(ip)) {
            con.kick(Bundle.format("kick.banned", findLocale(locale), discordServerUrl), 0);
            return;
        }

        if (Time.millis() < netServer.admins.getKickTime(uuid, ip)) {
            con.kick(Bundle.format("kick.recent-kick", findLocale(locale), millisecondsToMinutes(netServer.admins.getKickTime(uuid, ip) - Time.millis()), discordServerUrl), 0);
            return;
        }

        if (netServer.admins.getPlayerLimit() > 0 && Groups.player.size() >= netServer.admins.getPlayerLimit()) {
            con.kick(Bundle.format("kick.player-limit", findLocale(locale)), 0);
            return;
        }

        Seq<String> extraMods = packet.mods.copy();
        Seq<String> missingMods = mods.getIncompatibility(extraMods);

        if (extraMods.any() || missingMods.any()) {
            String reason = Bundle.format("kick.incompatible-mods", findLocale(locale));

            if (extraMods.any()) reason += Bundle.format("kick.unnecessary-mods", findLocale(locale), extraMods.toString("\n> "));
            if (missingMods.any()) reason += Bundle.format("kick.missing-mods", findLocale(locale), missingMods.toString("\n> "));

            con.kick(reason, 0);
        }

        PlayerInfo info = netServer.admins.getInfo(uuid);

        if (!netServer.admins.isWhitelisted(uuid, usid)) {
            info.lastName = name;
            info.lastIP = ip;
            info.adminUsid = usid;
            if (!info.names.contains(name)) info.names.add(name);
            if (!info.ips.contains(ip)) info.ips.add(ip);
            con.kick(Bundle.format("kick.not-whitelisted", findLocale(locale), discordServerUrl), 0);
            return;
        }

        if (packet.versionType == null || (packet.version == -1 && !netServer.admins.allowsCustomClients())) {
            con.kick(Bundle.format("kick.custom-client", findLocale(locale)), 0);
            return;
        }

        if (netServer.admins.isStrict() && Groups.player.contains(player -> player.uuid().equals(uuid) || player.usid().equals(usid))) {
            con.kick(Bundle.format("kick.already-connected", findLocale(locale)), 0);
            return;
        }

        if (name.trim().length() <= 0) {
            con.kick(Bundle.format("kick.name-is-empty", findLocale(locale)), 0);
            return;
        }

        if (packet.version != Version.build && packet.version != -1 && Version.build != -1 && !packet.versionType.equals("bleeding-edge")) {
            con.kick(Bundle.format(packet.version > Version.build ? "kick.server-outdated" : "kick.client-outdated", findLocale(locale), packet.version, Version.build));
            return;
        }

        netServer.admins.updatePlayerJoined(uuid, ip, name);

        Player player = Player.create();
        player.admin(netServer.admins.isAdmin(uuid, usid));
        player.name(name);
        player.locale(locale);
        player.color(new Color(packet.color).a(1f));
        player.con(con);
        player.con.usid = usid;
        player.con.uuid = uuid;
        player.con.mobile = packet.mobile;
        player.con.modclient = packet.version == -1;

        if (!player.admin && !info.admin) info.adminUsid = usid;

        try {
            writeBuffer.reset();
            player.write(outputBuffer);
        } catch (Exception e) {
            con.kick(Bundle.format("kick.name-is-empty", findLocale(locale)), 0);
            return;
        }

        con.player = player;

        player.team(netServer.assignTeam(player));
        netServer.sendWorldData(player);

        Events.fire(new PlayerConnect(player));
    }

    private static String fixName(String name) {
        name = name.trim().replace("\n", "").replace("\t", "");
        if (name.equals("[") || name.equals("]")) {
            return "";
        }

        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '[' && i != name.length() - 1 && name.charAt(i + 1) != '[' && (i == 0 || name.charAt(i - 1) != '[')) {
                String prev = name.substring(0, i);
                String next = name.substring(i);
                String result = checkColor(next);
                name = prev + result;
            }
        }

        StringBuilder result = new StringBuilder();
        int curChar = 0;
        while (curChar < name.length() && result.toString().getBytes(Strings.utf8).length < maxNameLength) {
            result.append(name.charAt(curChar++));
        }
        return result.toString();
    }

    private static String checkColor(String str) {
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) == ']') {
                String color = str.substring(1, i);

                if (Colors.get(color.toUpperCase()) != null || Colors.get(color.toLowerCase()) != null) {
                    Color result = (Colors.get(color.toLowerCase()) == null ? Colors.get(color.toUpperCase()) : Colors.get(color.toLowerCase()));
                    if (result.a <= 0.8f) return str.substring(i + 1);
                } else {
                    try {
                        Color result = Color.valueOf(color);
                        if (result.a <= 0.8f) return str.substring(i + 1);
                    } catch (Exception e) {
                        return str;
                    }
                }
            }
        }
        return str;
    }
}
