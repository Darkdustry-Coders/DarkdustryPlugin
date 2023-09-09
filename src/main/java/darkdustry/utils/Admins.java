package darkdustry.utils;

import arc.util.*;
import darkdustry.components.*;
import darkdustry.components.Database.Ban;
import darkdustry.listeners.SocketEvents.BanSyncEvent;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.NetConnection;
import useful.*;

import java.util.Date;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.*;
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
                .player(target.plainName())
                .admin(admin.plainName())
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
                .player(info.plainLastName())
                .admin(admin)
                .reason(reason)
                .unbanDate(new Date(Time.millis() + duration))
                .build());
    }

    // endregion
    // region bans

    public static void ban(Ban ban) {
        ban.generateID();
        Database.addBan(ban);

        // Отправляем бан по сокету, чтобы его увидели другие сервера
        Socket.send(new BanSyncEvent(config.mode.name(), ban));
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