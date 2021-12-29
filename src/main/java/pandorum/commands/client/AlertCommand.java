package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;

public class AlertCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(player.uuid(), playerInfo -> {
            playerInfo.alerts = !playerInfo.alerts;
            playerInfo.save();

            bundled(player, playerInfo.alerts ? "commands.alert.on" : "commands.alert.off");
        });
    }
}
