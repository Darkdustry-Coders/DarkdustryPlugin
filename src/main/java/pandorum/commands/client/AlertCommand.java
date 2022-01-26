package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import static pandorum.util.Utils.bundled;

public class AlertCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(player.uuid(), playerModel -> {
            playerModel.alerts = !playerModel.alerts;
            playerModel.save();

            bundled(player, playerModel.alerts ? "commands.alert.on" : "commands.alert.off");
        });
    }
}
