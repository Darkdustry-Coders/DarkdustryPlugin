package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.database.databridges.PlayerInfo;

import static pandorum.util.Utils.bundled;

public class AlertCommand {
    public static void run(final String[] args, final Player player) {
        PlayerInfo.find(player, playerModel -> {
            playerModel.alerts = !playerModel.alerts;
            PlayerInfo.save(playerModel);

            bundled(player, playerModel.alerts ? "commands.alert.on" : "commands.alert.off");
        });
    }
}
