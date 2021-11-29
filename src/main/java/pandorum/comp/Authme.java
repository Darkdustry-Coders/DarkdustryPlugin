package pandorum.comp;

import arc.struct.ObjectMap;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.struct.Tuple2;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class Authme {

    public static final ObjectMap<Message, Tuple2<String, String>> loginWaiting = new ObjectMap<>();

    public static final Button confirm = Button.success("confirm", "Подтвердить");
    public static final Button deny = Button.danger("deny", "Отклонить");

    public static void confirm(String uuid) {
        Player player = Misc.findByID(uuid);
        if (player != null) {
            player.admin(true);
            netServer.admins.adminPlayer(player.uuid(), player.usid());
            bundled(player, "commands.login.success");
        }
    }

    public static void deny(String uuid) {
        Player player = Misc.findByID(uuid);
        if (player != null) bundled(player, "commands.login.ignore");
    }
}
