package pandorum.commands.client;

import arc.util.Timekeeper;
import mindustry.gen.Player;

import static pandorum.PluginVars.loginCooldownTime;
import static pandorum.PluginVars.loginCooldowns;
import static pandorum.components.Authme.sendConfirmation;
import static pandorum.util.Utils.bundled;
import static pandorum.util.Utils.secondsToMinutes;

public class LoginCommand {
    public static void run(final String[] args, final Player player) {
        if (player.admin) {
            bundled(player, "commands.login.already-admin");
            return;
        }

        Timekeeper vtime = loginCooldowns.get(player.uuid(), () -> new Timekeeper(loginCooldownTime));
        if (!vtime.get()) {
            bundled(player, "commands.login.cooldown", secondsToMinutes(loginCooldownTime));
            return;
        }

        sendConfirmation(player);
        bundled(player, "commands.login.sent");
        vtime.reset();
    }
}
