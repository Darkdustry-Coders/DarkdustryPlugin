package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;

import static mindustry.Vars.netServer;

public class UnbanCommand implements Cons<String[]> {
    public void get(String[] args) {
        if (netServer.admins.unbanPlayerID(args[0]) || netServer.admins.unbanPlayerIP(args[0])) {
            Log.info("Игрок '@' успешно разбанен.", args[0]);
        } else {
            Log.err("Игрок не был забанен или его не существует!");
        }
    }
}
