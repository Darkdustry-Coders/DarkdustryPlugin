package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import arc.util.Strings;
import mindustry.net.Administration;
import pandorum.data.PlayerData;

import static mindustry.Vars.netServer;
import static pandorum.data.Database.setPlayerData;

public class SetDataCommand implements Cons<String[]> {
    public void get(String[] args) {
        Administration.PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
        if (info == null) {
            Log.err("Игрок не найден. Шиза?");
            return;
        }

        PlayerData data = new PlayerData();
        data.playTime = Strings.parseInt(args[1], 0);
        data.buildingsBuilt = Strings.parseInt(args[2], 0);
        data.gamesPlayed = Strings.parseInt(args[3], 0);
        setPlayerData(info.id, data);
    }
}
