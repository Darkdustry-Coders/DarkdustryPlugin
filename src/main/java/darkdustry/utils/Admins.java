package darkdustry.utils;

import arc.util.*;
import darkdustry.components.Database;
import darkdustry.components.Database.Ban;
import darkdustry.features.Authme;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.NetConnection;
import useful.*;

import java.util.Date;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class Admins {

    // region kick

    public static KickBuilder kick(NetConnection con, String locale, String key, Object... values) {
        return Bundle.kick(con, locale, key, values).add("kick.disclaimer", discordServerUrl);
    }

    public static KickBuilder kick(NetConnection con, String locale, long duration, String key, Object... values) {
        return Bundle.kick(con, locale, key, values)
                .add("kick.duration", formatDuration(duration, locale))
                .add("kick.disclaimer", discordServerUrl);
    }

    public static KickBuilder kickReason(NetConnection con, String locale, long duration, String reason, String key, Object... values) {
        return Bundle.kick(con, locale, key, values)
                .add("kick.duration", formatDuration(duration, locale))
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

        Log.info("&lc@ &fi&lk[&lb@&fi&lk]&fb has kicked @ &fi&lk[&lb@&fi&lk]&fb for @.", admin.plainName(), admin.uuid(), target.plainName(), target.uuid(), reason);
        Bundle.send("events.admin.kick", admin.coloredName(), target.coloredName(), reason);
    }

    public static void ban(Player target, Player admin, long duration, String reason) {
        ban(Ban.builder()
                .uuid(target.uuid())
                .ip(target.ip())
                .player(target.plainName())
                .admin(admin.plainName())
                .reason(reason)
                .unbanDate(new Date(Time.millis() + duration))
                .build());

        kickReason(target, duration, reason, "kick.banned-by-admin", admin.coloredName()).kick();

        Log.info("&lc@ &fi&lk[&lb@&fi&lk]&fb has banned @ &fi&lk[&lb@&fi&lk]&fb for @.", admin.plainName(), admin.uuid(), target.plainName(), target.uuid(), reason);
        Bundle.send("events.admin.ban", admin.coloredName(), target.coloredName(), reason);
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
                .player(info.plainLastName())
                .admin(admin)
                .reason(reason)
                .unbanDate(new Date(Time.millis() + duration))
                .build());

        var target = Find.playerByUUID(info.id);
        if (target == null) return;

        kickReason(target, duration, reason, "kick.banned-by-admin", admin).kick();
        Bundle.send("events.admin.ban", admin, target.coloredName(), reason);
    }

    // endregion
    // region bans

    public static void ban(Ban ban) {
        ban.generateID();

        Authme.sendBan(ban);
        Database.addBan(ban);
    }

    public static void checkKicked(NetConnection con, String locale) {
        long kickTime = netServer.admins.getKickTime(con.uuid, con.address);
        if (kickTime < Time.millis()) return;

        kick(con, locale, kickTime - Time.millis(), "kick.recently-kicked").kick();
    }

    public static void checkBanned(NetConnection con, String locale) {
        var ban = Database.getBan(con.uuid, con.address);
        if (ban == null || ban.expired()) return;

        kickReason(con, locale, ban.remaining(), ban.reason, "kick.ban", ban.admin).kick();
    }

    // endregion
}