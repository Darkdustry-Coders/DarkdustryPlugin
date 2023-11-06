package darkdustry.utils;

import arc.struct.ObjectIntMap;
import arc.util.*;
import darkdustry.database.Database;
import darkdustry.database.models.Ban;
import darkdustry.features.net.Socket;
import darkdustry.listeners.SocketEvents.*;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.NetConnection;
import useful.*;

import java.util.Date;

import static darkdustry.PluginVars.*;
import static darkdustry.config.Config.*;
import static mindustry.Vars.*;

public class Admins {

    // region kick

    public static KickBuilder kick(NetConnection con, String locale, String key, Object... values) {
        return Bundle.kick(con, locale, key, values).add("kick.disclaimer", discordServerUrl);
    }

    public static KickBuilder kick(NetConnection con, String locale, long duration, String key, Object... values) {
        return Bundle.kick(con, locale, key, values)
                .add("kick.duration", Bundle.formatDuration(locale, duration))
                .add("kick.disclaimer", discordServerUrl);
    }

    public static KickBuilder kickReason(NetConnection con, String locale, long duration, String reason, String key, Object... values) {
        return Bundle.kick(con, locale, key, values)
                .add("kick.duration", Bundle.formatDuration(locale, duration))
                .add("kick.reason", reason)
                .add("kick.disclaimer", discordServerUrl);
    }

    public static KickBuilder kickReason(Player player, long duration, String reason, String key, Object... values) {
        return kickReason(player.con, player.locale, duration, reason, key, values);
    }

    // endregion
    // region admin

    public static void kick(Player target, Player admin, long duration, String reason) {
        kickReason(target, duration, reason, "kick.kicked-by-admin", admin.coloredName()).kick(duration);
        Bundle.send("events.admin.kick", admin.coloredName(), target.coloredName(), reason);

        Log.info("&lc@ &fi&lk[&lb@&fi&lk]&fb has kicked @ &fi&lk[&lb@&fi&lk]&fb for @.", admin.plainName(), admin.uuid(), target.plainName(), target.uuid(), reason);
    }

    public static void ban(Player target, Player admin, long duration, String reason) {
        ban(Ban.builder()
                .uuid(target.uuid())
                .ip(target.ip())
                .playerName(target.plainName())
                .adminName(admin.plainName())
                .reason(reason)
                .unbanDate(new Date(Time.millis() + duration))
                .build());

        Log.info("&lc@ &fi&lk[&lb@&fi&lk]&fb has banned @ &fi&lk[&lb@&fi&lk]&fb for @.", admin.plainName(), admin.uuid(), target.plainName(), target.uuid(), reason);
    }

    // endregion
    // region console

    public static void kick(Player target, String admin, long duration, String reason) {
        kickReason(target, duration, reason, "kick.kicked-by-admin", admin).kick(duration);
        Bundle.send("events.admin.kick", admin, target.coloredName(), reason);
    }

    public static void ban(PlayerInfo info, String admin, long duration, String reason) {
        ban(Ban.builder()
                .uuid(info.id)
                .ip(info.lastIP)
                .playerName(info.plainLastName())
                .adminName(admin)
                .reason(reason)
                .unbanDate(new Date(Time.millis() + duration))
                .build());
    }

    // endregion
    // region actions

    public static void ban(Ban ban) {
        ban.generateID();
        ban.generatePlayerID();

        Socket.send(new BanEvent(config.mode.name(), Database.addBan(ban)));
    }

    public static void voteKick(Player initiator, Player target, ObjectIntMap<Player> votes, String reason) {
        var votesFor = new StringBuilder();
        var votesAgainst = new StringBuilder();

        votes.forEach(entry -> (switch (entry.value) {
            case 1 -> votesFor;
            case -1 -> votesAgainst;

            default -> throw new IllegalStateException();
        }).append("[scarlet]- ").append(entry.key.coloredName()).append("[accent] [").append(Database.getPlayerData(entry.key).id).append("]\n"));

        if (votesFor.isEmpty())
            votesFor.append("[scarlet]- [gray]<none>").append("\n");

        if (votesAgainst.isEmpty())
            votesAgainst.append("[scarlet]- [gray]<none>").append("\n");

        kickReason(target, kickDuration, reason, "kick.vote-kicked", initiator.coloredName(), votesFor, votesAgainst).kick(kickDuration);
        Socket.send(new VoteKickEvent(
                config.mode.name(),
                target.plainName() + " [" + Database.getPlayerData(target).id + "]",
                initiator.plainName() + " [" + Database.getPlayerData(initiator).id + "]",
                reason,
                Strings.stripColors(votesFor),
                Strings.stripColors(votesAgainst)
        ));
    }

    public static void checkKicked(NetConnection con, String locale) {
        long kickTime = netServer.admins.getKickTime(con.uuid, con.address);
        if (kickTime < Time.millis()) return;

        kick(con, locale, kickTime - Time.millis(), "kick.recently-kicked").kick();
    }

    public static void checkBanned(NetConnection con, String locale) {
        var ban = Database.getBan(con.uuid, con.address);
        if (ban == null || ban.expired()) return;

        kickReason(con, locale, ban.remaining(), ban.reason, "kick.ban", ban.adminName).kick();
    }

    // endregion
}