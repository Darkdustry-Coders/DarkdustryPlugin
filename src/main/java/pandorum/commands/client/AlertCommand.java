package pandorum.commands.client;

import com.mongodb.BasicDBObject;
import mindustry.gen.Player;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;

public class AlertCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(
            PlayerModel.class,
            new BasicDBObject("UUID", player.uuid()),
            playerInfo -> {
                playerInfo.alerts = !playerInfo.alerts;
                playerInfo.save();

                bundled(
                    player,
                    "commands.alert."
                    + (playerInfo.alerts ? "on" : "off")
                );
            }
        );
    }
}
