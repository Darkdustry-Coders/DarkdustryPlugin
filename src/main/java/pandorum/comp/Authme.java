package pandorum.comp;

import discord4j.core.object.component.Button;
import mindustry.gen.Player;
import pandorum.Misc;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class Authme {

    public static Button confirm = Button.success("confirm", "Подтвердить");
    public static Button deny = Button.danger("deny", "Отклонить");

    public static void confirm(String uuid) {
        Player player = Misc.findByID(uuid);
        if (player != null) {
            netServer.admins.adminPlayer(player.uuid(), player.usid());
            player.admin(true);
            bundled(player, "commands.login.success");
        }
    }

    public static void deny(String uuid) {
        Player player = Misc.findByID(uuid);
        if (player != null) bundled(player, "commands.login.ignore");
    }
}
