package pandorum.events.handlers;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.Colors;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import mindustry.core.Version;
import mindustry.game.EventType.ConnectPacketEvent;
import mindustry.game.EventType.PlayerConnect;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.NetConnection;
import mindustry.net.Packets.ConnectPacket;
import mindustry.net.Packets.KickReason;
import pandorum.comp.Bundle;

import static mindustry.Vars.*;
import static pandorum.Misc.findLocale;
import static pandorum.PandorumPlugin.*;
import static pandorum.PluginVars.antiVPN;

public class ConnectPacketHandler {
    public static void handle(NetConnection con, ConnectPacket packet) {
        if (con.kicked) return;

        Events.fire(new ConnectPacketEvent(con, packet));

        con.connectTime = Time.millis();

        if (netServer.admins.isIPBanned(con.address) || netServer.admins.isSubnetBanned(con.address)) return;

        if (con.hasBegunConnecting) {
            con.kick(KickReason.idInUse);
            return;
        }

        con.hasBegunConnecting = true;

        if (packet.uuid == null || packet.usid == null) {
            con.kick(KickReason.idInUse);
            return;
        }

        if (netServer.admins.isIDBanned(packet.uuid)) {
            con.kick(KickReason.banned);
            return;
        }

        if (Time.millis() < netServer.admins.getKickTime(packet.uuid, con.address)) {
            con.kick(KickReason.recentKick);
            return;
        }

        if (netServer.admins.getPlayerLimit() > 0 && Groups.player.size() >= netServer.admins.getPlayerLimit()) {
            con.kick(KickReason.playerLimit);
            return;
        }

        if (packet.locale == null) packet.locale = "en";
        String locale = packet.locale;

        Seq<String> extraMods = packet.mods.copy();
        Seq<String> missingMods = mods.getIncompatibility(extraMods);

        if (!extraMods.isEmpty() || !missingMods.isEmpty()) {
            StringBuilder reason = new StringBuilder(Bundle.format("events.incompatible-mods", findLocale(locale)));
            if (missingMods.any()) {
                reason.append(Bundle.format("events.missing-mods", findLocale(locale))).append("> ").append(missingMods.toString("\n> ")).append("[]\n");
            }

            if (extraMods.any()) {
                reason.append(Bundle.format("events.unnecessary-mods", findLocale(locale))).append("> ").append(extraMods.toString("\n> "));
            }
            con.kick(reason.toString(), 0);
        }

        String uuid = packet.uuid;
        String usid = packet.usid;
        String ip = con.address;
        String name = fixName(packet.name);
        PlayerInfo info = netServer.admins.getInfo(uuid);

        if (!netServer.admins.isWhitelisted(uuid, usid)) {
            info.lastName = name;
            info.lastIP = ip;
            info.adminUsid = usid;
            if (!info.names.contains(name, false)) info.names.add(name);
            if (!info.ips.contains(ip, false)) info.ips.add(ip);
            Call.infoMessage(con, Bundle.format("events.not-whitelisted", findLocale(locale)));
            con.kick(KickReason.whitelist);
            return;
        }

        if (packet.versionType == null || ((packet.version == -1 || !packet.versionType.equals(Version.type)) && Version.build != -1 && !netServer.admins.allowsCustomClients())) {
            con.kick(Version.type.equals(packet.versionType) ? KickReason.customClient : KickReason.typeMismatch);
            return;
        }

        if (netServer.admins.isStrict() && Groups.player.contains(player -> player.uuid().equals(uuid) || player.usid().equals(usid))) {
            con.kick(KickReason.idInUse);
            return;
        }

        if (name.trim().length() <= 0) {
            con.kick(KickReason.nameEmpty);
            return;
        }

        netServer.admins.updatePlayerJoined(uuid, ip, name);

        if (packet.version != Version.build && Version.build != -1 && packet.version != -1) {
            con.kick(packet.version > Version.build ? KickReason.serverOutdated : KickReason.clientOutdated);
            return;
        }

        if (packet.version == -1) con.modclient = true;

        Player player = Player.create();
        player.admin = netServer.admins.isAdmin(uuid, usid);
        player.name = name;
        player.locale = locale;
        player.color = new Color(packet.color).a(1f);
        player.con = con;
        player.con.usid = usid;
        player.con.uuid = uuid;
        player.con.mobile = packet.mobile;

        if (!player.admin && !info.admin) {
            info.adminUsid = usid;
        }

        try {
            writeBuffer.reset();
            player.write(outputBuffer);
        } catch (Exception e) {
            con.kick(KickReason.nameEmpty);
            return;
        }

        con.player = player;

        player.team(netServer.assignTeam(player));

        netServer.sendWorldData(player);

        antiVPN.checkIp(ip, isDangerous -> {
            if (isDangerous) con.kick(Bundle.format("events.vpn-ip", findLocale(locale)), 0);
        });

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

    public static String checkColor(String str) {
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
