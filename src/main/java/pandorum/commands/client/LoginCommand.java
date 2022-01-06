package pandorum.commands.client;

import arc.util.Timekeeper;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import mindustry.gen.Player;
import pandorum.comp.Authme;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.bundled;
import static pandorum.PluginVars.loginCooldownTime;
import static pandorum.PluginVars.loginCooldowns;

public class LoginCommand {
    public static void run(final String[] args, final Player player) {
        if (player.admin) {
            bundled(player, "commands.login.already");
            return;
        }

        Timekeeper vtime = loginCooldowns.get(player.uuid(), () -> new Timekeeper(loginCooldownTime));
        if (!vtime.get()) {
            bundled(player, "commands.login.cooldown", TimeUnit.SECONDS.toMinutes(loginCooldownTime));
            return;
        }

        Message message = BotHandler.adminChannel.createMessage(MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .color(BotMain.normalColor)
                        .author("Log-in System", null, "https://icon-library.com/images/yes-icon/yes-icon-15.jpg")
                        .title("Запрос на выдачу прав администратора.")
                        .addField("Никнейм:", player.name, true)
                        .addField("UUID:", player.uuid(), true)
                        .footer("Нажми на кнопку чтобы подтвердить или отменить получение прав админа.", null)
                        .build()
                ).addComponent(ActionRow.of(Authme.confirm, Authme.deny, Authme.check)).build()).block();

        Authme.loginWaiting.put(message, player);
        bundled(player, "commands.login.sent");
        vtime.reset();
    }
}
