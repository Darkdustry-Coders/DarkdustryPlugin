package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;
import pandorum.data.PlayerData;

import static pandorum.PluginVars.datas;
import static pandorum.util.Utils.bundled;

public class AlertCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        PlayerData data = datas.get(player.uuid());
        data.alertsEnabled = !data.alertsEnabled;
        bundled(player, data.alertsEnabled ? "commands.alert.on" : "commands.alert.off");
    }
}
