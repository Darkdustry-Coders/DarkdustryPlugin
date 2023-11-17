package darkdustry.listeners;

import arc.Core;
import arc.Events;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.CommandHandler.CommandResponse;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import darkdustry.database.Cache;
import darkdustry.features.menus.MenuHandler;
import darkdustry.features.net.Socket;
import darkdustry.features.net.Translator;
import darkdustry.listeners.SocketEvents.ServerMessageEvent;
import darkdustry.utils.Admins;
import darkdustry.utils.Utils;
import mindustry.game.EventType.AdminRequestEvent;
import mindustry.game.EventType.ConnectPacketEvent;
import mindustry.game.EventType.ConnectionEvent;
import mindustry.game.EventType.PlayerConnect;
import mindustry.game.Team;
import mindustry.gen.AdminRequestCallPacket;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.TraceInfo;
import mindustry.net.NetConnection;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.ConnectPacket;
import useful.AntiVpn;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static darkdustry.config.Config.config;
import static darkdustry.utils.Checks.alreadyVoted;
import static darkdustry.utils.Checks.notAdmin;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class NetHandlers {
    public static final ObjectSet<String> alreadyBlockedIps = new ObjectSet<>();
    public static final Seq<Subnet> subnets = new Seq<>();
    public record Subnet(int ip, int mask) {}


    public static String chat(Player from, String message) {
        int sign = voteChoice(message);
        if (sign == 0 || vote == null) {
            Log.info("&fi@: @", "&lc" + from.plainName(), "&lw" + message);
            Translator.translate(from, message);

            Socket.send(new ServerMessageEvent(config.mode.name(), stripDiscord(from.plainName()), stripDiscord(message)));
            return null;
        }

        if (!alreadyVoted(from, vote)) vote.vote(from, sign);
        return null;
    }

    public static String invalidResponse(Player player, CommandResponse response) {
        return switch (response.type) {
            case fewArguments -> Bundle.format("commands.unknown.few-arguments", player, response.command.text, Bundle.get("commands." + response.command.text + ".params", response.command.paramText, player));
            case manyArguments -> Bundle.format("commands.unknown.many-arguments", player, response.command.text, Bundle.get("commands." + response.command.text + ".params", response.command.paramText, player));
            default -> {
                var closest = availableCommands(player)
                        .map(command -> command.name)
                        .retainAll(command -> Strings.levenshtein(command, response.runCommand) < 3)
                        .min(command -> Strings.levenshtein(command, response.runCommand));

                yield closest == null ? Bundle.format("commands.unknown", player) : Bundle.format("commands.unknown.closest", player, closest);
            }
        };
    }

    public static void connect(NetConnection con, Connect packet) {
        Events.fire(new ConnectionEvent(con));

        var connections = Seq.with(net.getConnections()).retainAll(other -> other.address.equals(con.address));
        if (connections.size >= maxIdenticalIPs) {
            netServer.admins.blacklistDos(con.address);
            connections.each(NetConnection::close);
        }
    }

    public static void connect(NetConnection con, ConnectPacket packet) {
        con.connectTime = Time.millis();

        Events.fire(new ConnectPacketEvent(con, packet));

        String uuid = con.uuid = packet.uuid,
                usid = con.usid = packet.usid,
                ip = con.address,
                locale = packet.locale,
                name = netServer.fixName(packet.name);

        con.mobile = packet.mobile;
        con.modclient = packet.version == -1;

        if (con.hasBegunConnecting || Seq.with(net.getConnections()).count(other -> other.uuid.equals(uuid) || other.usid.equals(usid)) > 1) {
            Bundle.kick(con, locale, 0L, "kick.already-connected");
            return;
        }

        if (netServer.admins.isSubnetBanned(ip)) {
            Admins.kick(con, locale, "kick.subnet-ban").kick();
            return;
        }

        Admins.checkKicked(con, locale);
        Admins.checkBanned(con, locale);

        con.hasBegunConnecting = true;

        if (Strings.stripColors(name).trim().isEmpty()) {
            Bundle.kick(con, locale, 0L, "kick.name-is-empty");
            return;
        }

        if (netServer.admins.getPlayerLimit() > 0 && Groups.player.size() >= netServer.admins.getPlayerLimit()) {
            Bundle.kick(con, locale, 0L, "kick.player-limit", netServer.admins.getPlayerLimit());
            return;
        }

        var extraMods = packet.mods.copy();
        var missingMods = mods.getIncompatibility(extraMods);

        if (extraMods.any()) {
            Bundle.kick(con, locale, 0L, "kick.extra-mods", extraMods.toString("\n> "));
            return;
        }

        if (missingMods.any()) {
            Bundle.kick(con, locale, 0L, "kick.missing-mods", missingMods.toString("\n> "));
            return;
        }

        if (packet.versionType == null || (packet.version == -1 && !netServer.admins.allowsCustomClients())) {
            Bundle.kick(con, locale, 0L, "kick.custom-client");
            return;
        }

        if (packet.version != mindustryVersion && packet.version != -1 && mindustryVersion != -1 && !packet.versionType.equals("bleeding-edge")) {
            Bundle.kick(con, locale, 0L, packet.version > mindustryVersion ? "kick.server-outdated" : "kick.client-outdated", packet.version, mindustryVersion);
            return;
        }

        var info = netServer.admins.getInfo(uuid);
        if (!netServer.admins.isWhitelisted(uuid, usid)) {
            info.adminUsid = usid;
            info.names.addUnique(info.lastName = name);
            info.ips.addUnique(info.lastIP = ip);
            Bundle.kick(con, locale, 0L, "kick.not-whitelisted", discordServerUrl);
            return;
        }

        if (AntiVpn.checkAddress(ip)) {
            if(!verify(ip)) {
                con.close();
            }
            //redundant
            try {
                Bundle.kick(con, locale, 0L, "kick.vpn", discordServerUrl);
            }
            catch (Exception e) {
                //ignored
            }
            return;
        }

        if (con.kicked) return;

        var player = Player.create();
        player.con(con);
        player.name(name);
        player.locale(locale);
        player.admin(netServer.admins.isAdmin(uuid, usid));
        player.color.set(packet.color).a(1f);

        con.player = player;

        netServer.admins.updatePlayerJoined(uuid, ip, name);
        if (!info.admin) info.adminUsid = usid;

        player.team(netServer.assignTeam(player));
        netServer.sendWorldData(player);

        Events.fire(new PlayerConnect(player));
    }

    public static boolean verify(String address) {
        if (alreadyBlockedIps.contains(address)) return true;
        int ip;

        try {
            ip = Utils.parseSubnet(address).ip;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (var subnet : subnets)
            if ((ip & subnet.mask) == subnet.ip) {
                if (!Core.settings.getBool("disable-connect-filter")) alreadyBlockedIps.add(address);
                return true;
            }
        return false;
    }

    public static void adminRequest(NetConnection con, AdminRequestCallPacket packet) {
        var admin = con.player;
        var target = packet.other;

        if (notAdmin(admin) || target == null || (target.admin && target != admin)) return;

        Events.fire(new AdminRequestEvent(admin, target, packet.action));

        switch (packet.action) {
            case kick -> MenuHandler.showKickInput(admin, target);
            case ban -> MenuHandler.showBanInput(admin, target);
            case trace -> {
                var trace = new TraceInfo(
                        target.ip(),
                        String.valueOf(Cache.get(target).id),
                        target.con.modclient,
                        target.con.mobile,
                        target.getInfo().timesJoined,
                        target.getInfo().timesKicked,
                        target.getInfo().ips.toArray(String.class),
                        target.getInfo().names.toArray(String.class)
                );

                Call.traceInfo(con, target, trace);
                Log.info("&lc@ &fi&lk[&lb@&fi&lk]&fb has requested trace info of @ &fi&lk[&lb@&fi&lk]&fb.", admin.plainName(), admin.uuid(), target.plainName(), target.uuid());
            }

            case wave -> {
                logic.skipWave();

                Log.info("&lc@ &fi&lk[&lb@&fi&lk]&fb has skipped the wave.", admin.plainName(), admin.uuid());
                Bundle.send("events.admin.wave", admin.coloredName());
            }

            case switchTeam -> {
                if (config.mode.isDefault && packet.params instanceof Team team) {
                    target.team(team);

                    Bundle.send(target, "commands.team.success", team.coloredName());
                    if (target != admin)
                        Bundle.send(admin, "commands.team.success.player", target.coloredName(), team.coloredName());
                }
            }
        }
    }
}