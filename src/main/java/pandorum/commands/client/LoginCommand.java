package pandorum.commands.client;

import arc.util.Time;
import mindustry.gen.Player;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.*;

public class LoginCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (waiting.contains(player.uuid())) {
            bundled(player, "commands.login.waiting");
            return;
        }

        if (player.admin()) {
            bundled(player, "commands.login.already");
            return;
        }

        if (loginCooldowns.containsKey(player.uuid())) {
            if (Time.timeSinceMillis(loginCooldowns.get(player.uuid())) < 1000 * 60 * 15L) return;
            loginCooldowns.remove(player.uuid());
        }

        waiting.add(player.uuid());
        socket.emit("registerAsAdmin", player.uuid(), player.name());
        bundled(player, "commands.login.sent");
    }
}
