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

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.loginCooldowns;

public class LoginCommand {

    private static final float cooldownTime = 1200f;

    public static void run(final String[] args, final Player player) {
        if (player.admin()) {
            bundled(player, "commands.login.already");
            return;
        }

        Timekeeper vtime = loginCooldowns.get(player.uuid(), () -> new Timekeeper(cooldownTime));
        if (!vtime.get()) {
            bundled(player, "commands.login.cooldown", (int) (cooldownTime / 60f));
            return;
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.normalColor)
                .author("Log-in System", null, "https://icon-library.com/images/yes-icon/yes-icon-15.jpg")
                .title("Запрос на выдачу прав администратора.")
                .addField("Никнейм: ", player.name, true)
                .addField("UUID: ", player.uuid(), true)
                .footer("Нажми на кнопку чтобы подтвердить или отменить получение прав админа.", null)
                .build();

        Message message = BotHandler.adminChannel.createMessage(MessageCreateSpec.builder()
                .addEmbed(embed)
                .addComponent(ActionRow.of(Authme.confirm, Authme.deny))
                .build()).block();

        Authme.loginWaiting.put(message, player);

        vtime.reset();
        bundled(player, "commands.login.sent");
    }
}
