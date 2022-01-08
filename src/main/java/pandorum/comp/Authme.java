package pandorum.comp;

import arc.util.Strings;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import pandorum.discord.BotHandler;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;
import static pandorum.PluginVars.loginWaiting;

public class Authme {

    public static final Button confirm = Button.success("confirm", "Подтвердить");
    public static final Button deny = Button.danger("deny", "Отклонить");
    public static final Button check = Button.primary("check", "Посмотреть информацию");

    public static void confirm(Message message, ButtonInteractionEvent event) {
        BotHandler.text(message.getChannel().block(), "Запрос игрока **@** был подтвержден **@**", Strings.stripColors(loginWaiting.get(message).getInfo().lastName), event.getInteraction().getUser().getUsername());
        Player player = loginWaiting.remove(message);
        if (player != null) {
            netServer.admins.adminPlayer(player.uuid(), player.usid());
            player.admin(true);
            bundled(player, "commands.login.success");
        }
        message.delete().block();
    }

    public static void deny(Message message, ButtonInteractionEvent event) {
        BotHandler.text(message.getChannel().block(), "Запрос игрока **@** был отклонен **@**", Strings.stripColors(loginWaiting.get(message).getInfo().lastName), event.getInteraction().getUser().getUsername());
        Player player = loginWaiting.remove(message);
        if (player != null) {
            bundled(player, "commands.login.ignore");
        }
        message.delete().block();
    }

    public static void check(Message message, ButtonInteractionEvent event) {
        PlayerInfo info = loginWaiting.get(message).getInfo();
        event.reply(Strings.format("> Информация об игроке **@**\n\nUUID: @\nIP: @\n\nВошел на сервер: @ раз.\nБыл выгнан с сервера: @ раз\n\nВсе IP адреса: @\n\nВсе никнеймы: @", info.lastName, info.id, info.lastIP, info.timesJoined, info.timesKicked, info.ips, info.names)).withEphemeral(true).block();
    }
}
