package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.mongo.models.PlayerModel;

import static pandorum.util.Utils.bundled;

public class AlertCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        PlayerModel.find(player, playerModel -> {
            playerModel.alerts = !playerModel.alerts;
            playerModel.save();

            bundled(player, playerModel.alerts ? "commands.alert.on" : "commands.alert.off");
        });
    }
}
