package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.data.PlayerData;

import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;
import static pandorum.util.Utils.bundled;

public class AlertCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        PlayerData data = getPlayerData(player.uuid());
        data.alertsEnabled = !data.alertsEnabled;
        setPlayerData(player.uuid(), data);
        bundled(player, data.alertsEnabled ? "commands.alert.on" : "commands.alert.off");
    }
}
