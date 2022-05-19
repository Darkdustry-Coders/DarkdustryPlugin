package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;
import pandorum.features.Ranks;

import static mindustry.Vars.netServer;

public class RemoveAdmins implements Cons<String[]> {
    public void get(String[] args) {
        Seq<PlayerInfo> admins = netServer.admins.getAdmins();
        try {
            for(PlayerInfo i:admins) {
                netServer.admins.unAdminPlayer(i.id);
                Ranks.setRank(i.id, Ranks.player);

                Log.info("Админ '@' был удален. UUID:'@', IP:'@'.", i.lastName, i.id, i.lastIP);
            }
        } catch (Exception e) {
            Log.err("Не удалось выполнить команду!");
            Log.err(e);
        }
    }
}
