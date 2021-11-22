package pandorum.events.handlers;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.Colors;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.NetConnection;
import mindustry.net.Packets.ConnectPacket;
import mindustry.net.Packets.KickReason;
import pandorum.PandorumPlugin;
import pandorum.comp.Bundle;

import java.util.Locale;

import static mindustry.Vars.netServer;
import static mindustry.Vars.maxNameLength;
import static pandorum.Misc.findLocale;

public class ConnectHandler {
    public static void handle(NetConnection con, ConnectPacket packet) {
        if (con.kicked) return;

        if (con.address.startsWith("steam:")) packet.uuid = con.address.substring("steam:".length());

        Events.fire(new EventType.ConnectPacketEvent(con, packet));

        con.connectTime = Time.millis();

        String uuid = packet.uuid;
        String ip = con.address;
        PlayerInfo info = netServer.admins.getInfo(uuid);

        if (netServer.admins.isIPBanned(con.address) || netServer.admins.isSubnetBanned(con.address)) return;

        if (con.hasBegunConnecting) {
            con.kick(KickReason.idInUse);
            return;
        }

        con.hasBegunConnecting = true;
        con.mobile = packet.mobile;

        if (packet.uuid == null || packet.usid == null) {
            con.kick(KickReason.idInUse);
            return;
        }

        if (netServer.admins.isIDBanned(uuid)) {
            con.kick(KickReason.banned);
            return;
        }

        if (Time.millis() < netServer.admins.getKickTime(uuid, con.address)) {
            con.kick(KickReason.recentKick);
            return;
        }

        if (netServer.admins.getPlayerLimit() > 0 && Groups.player.size() >= netServer.admins.getPlayerLimit()) {
            con.kick(KickReason.playerLimit);
            return;
        }

        if (packet.locale == null) packet.locale = "en";
        Locale locale = findLocale(packet.locale);
        packet.name = fixName(packet.name);

        Seq<String> extraMods = packet.mods.copy();
        Seq<String> missingMods = Vars.mods.getIncompatibility(extraMods);

        if (!extraMods.isEmpty() || !missingMods.isEmpty()) {
            StringBuilder reason = new StringBuilder(Bundle.format("events.incompatible-mods", locale));
            if (!missingMods.isEmpty()) {
                reason.append(Bundle.format("events.missing-mods", locale)).append("> ").append(missingMods.toString("\n> ")).append("[]\n");
            }

            if (!extraMods.isEmpty()) {
                reason.append(Bundle.format("events.unnecessary-mods", locale)).append("> ").append(extraMods.toString("\n> "));
            }
            con.kick(reason.toString(), 0);
        }

        if (!netServer.admins.isWhitelisted(packet.uuid, packet.usid)) {
            info.adminUsid = packet.usid;
            info.lastName = packet.name;
            info.id = packet.uuid;
            netServer.admins.save();
            Call.infoMessage(con, Bundle.format("events.not-whitelisted", locale, PandorumPlugin.discordServerLink));
            con.kick(KickReason.whitelist);
            return;
        }

        if (packet.versionType == null || ((packet.version == -1 || !packet.versionType.equals(Version.type)) && Version.build != -1 && !netServer.admins.allowsCustomClients())) {
            con.kick(Version.type.equals(packet.versionType) ? KickReason.customClient : KickReason.typeMismatch);
            return;
        }

        if (packet.name.trim().length() <= 0) {
            con.kick(KickReason.nameEmpty), 0);
            return;
        }

        netServer.admins.updatePlayerJoined(uuid, ip, packet.name);

        if (packet.version != Version.build && Version.build != -1 && packet.version != -1) {
            con.kick(packet.version > Version.build ? KickReason.serverOutdated : KickReason.clientOutdated);
            return;
        }

        if (packet.version == -1) con.modclient = true;

        Player player = Player.create();
        player.admin = netServer.admins.isAdmin(uuid, packet.usid);
        player.con = con;
        player.con.usid = packet.usid;
        player.con.uuid = uuid;
        player.con.mobile = packet.mobile;
        player.name = packet.name;
        player.locale = packet.locale;
        player.color.set(packet.color).a(1f);

        if (!player.admin && !info.admin) {
            info.adminUsid = packet.usid;
        }

        try {
            PandorumPlugin.writeBuffer.reset();
            player.write(PandorumPlugin.outputBuffer);
        } catch(Exception e) {
            con.kick(KickReason.nameEmpty, 0);
            return;
        }

        con.player = player;

        player.team(netServer.assignTeam(player));

        netServer.sendWorldData(player);

        PandorumPlugin.antiVPN.checkIp(ip, result -> {
            if (result) con.kick(Bundle.format("events.vpn-ip", locale));
        });

        Events.fire(new EventType.PlayerConnect(player));
    }

    private static String fixName(String name) {
        name = name.trim().replace("\n", "").replace("\t", "").replace("@", "");
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
                    } catch(Exception e) {
                        return str;
                    }
                }
            }
        }
        return str;
    }
}
