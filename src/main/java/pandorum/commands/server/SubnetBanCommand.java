package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;

import static mindustry.Vars.netServer;

public class SubnetBanCommand implements Cons<String[]> {
    public void get(String[] args) {
        if (args.length == 0) {
            Seq<String> subnetBans = netServer.admins.getSubnetBans();
            if (subnetBans.isEmpty()) {
                Log.info("Забаненных сабнетов не найдено.");
            } else {
                Log.info("Забаненные сабнеты: (@)", subnetBans.size);
                subnetBans.each(subnetBan -> Log.info("  - @**", subnetBan));
            }
        } else if (args.length == 1) {
            Log.err("Не хватает последнего аргумента. Необходимо указать сабнет, который нужно забанить или разбанить.");
        } else {
            switch (args[0].toLowerCase()) {
                case "add" -> {
                    if (netServer.admins.getSubnetBans().contains(args[1])) {
                        Log.err("Этот сабнет уже забанен.");
                        return;
                    }

                    netServer.admins.addSubnetBan(args[1]);
                    Log.info("Сабнет @** забанен.", args[1]);
                }

                case "remove" -> {
                    if (!netServer.admins.getSubnetBans().contains(args[1])) {
                        Log.err("Этот сабнет не забанен.");
                        return;
                    }

                    netServer.admins.removeSubnetBan(args[1]);
                    Log.info("Сабнет @** разбанен.", args[1]);
                }

                default -> Log.err("Второй параметр должен быть или 'add' или 'remove'.");
            }
        }
    }
}
