package pandorum.comp.admin;
import arc.util.Log;
import arc.util.Time;
import io.socket.client.Socket;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.PandorumPlugin;
import pandorum.comp.Bundle;

import java.util.Objects;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;
import static pandorum.PandorumPlugin.socket;

public class Authme {
    public static void init() {
        socket.on(Socket.EVENT_CONNECT, (connection_args) -> {
            Log.info("Подключено к сокету.");

            socket.on("registerResponse", args -> {
                int action = (int)args[0];
                String uuid = (String)args[1];
                PandorumPlugin.waiting.remove(uuid);
                ResponseType responseCode = ResponseType.getByValue(action);

                switch (responseCode) {
                    case ACCEPT -> addAdmin(uuid);
                    case BAN -> kickUser(uuid);
                    case SKIP -> ignoreRequest(uuid);
                }
            });
        });
    }

    private static void addAdmin(String uuid) {
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));
        if (Objects.isNull(player)) return;
        netServer.admins.adminPlayer(player.uuid(), player.usid());
        player.admin(true);
        bundled(player, "commands.login.success");
    }

    private static void ignoreRequest(String uuid) {
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));
        PandorumPlugin.loginCooldowns.put(uuid, Time.millis());
        if (Objects.isNull(player)) return;
        bundled(player, "commands.login.ignore");
    }

    private static void kickUser(String uuid) {
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));
        if (Objects.isNull(player)) return;
        player.con.kick(Bundle.format("commands.login.kick", findLocale(player.locale)), 60 * 1000L * 15);
    }
}
