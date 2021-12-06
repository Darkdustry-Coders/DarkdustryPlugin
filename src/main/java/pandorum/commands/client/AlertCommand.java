package pandorum.commands.client;

import com.mongodb.BasicDBObject;
import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.gamemodes.RequireSimpleGamemode;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;

public class AlertCommand {
    @RequireSimpleGamemode
    @ClientCommand(name = "alert", args = "", description = "Enable/disable alerts.", admin = false)
    public static void run(final String[] args, final Player player) {
        PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
            playerInfo.alerts = !playerInfo.alerts;
            playerInfo.save();

            bundled(player, playerInfo.alerts ? "commands.alert.on" : "commands.alert.off");
        });
    }
}
