package pandorum.commands.client;

import static pandorum.Misc.bundled;

import com.mongodb.BasicDBObject;

import org.bson.Document;

import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import pandorum.models.PlayerModel;

public class AlertCommand {
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(
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
