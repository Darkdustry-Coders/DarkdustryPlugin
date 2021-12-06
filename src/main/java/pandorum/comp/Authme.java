package pandorum.comp;

import arc.struct.ObjectMap;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import mindustry.gen.Player;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class Authme {

    public static final ObjectMap<Message, Player> loginWaiting = new ObjectMap<>();

    public static final Button confirm = Button.success("confirm", "Подтвердить");
    public static final Button deny = Button.danger("deny", "Отклонить");

    public static void confirm(Player player) {
        if (player != null) {
            netServer.admins.adminPlayer(player.uuid(), player.usid());
            player.admin(true);
            bundled(player, "commands.login.success");
        }
    }

    public static void deny(Player player) {
        if (player != null) bundled(player, "commands.login.ignore");
    }
}
