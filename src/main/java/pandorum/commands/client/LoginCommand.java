package pandorum.commands.client;

import arc.util.Timekeeper;
import mindustry.gen.Player;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static pandorum.Misc.bundled;
import static pandorum.PandorumPlugin.loginCooldowns;

public class LoginCommand implements ClientCommand {

    private static final float cooldownTime = 1000f;

    public static void run(final String[] args, final Player player) {
        if (player.admin()) {
            bundled(player, "commands.login.already");
            return;
        }

        Timekeeper vtime = loginCooldowns.get(player.uuid(), () -> new Timekeeper(cooldownTime));
        if (!vtime.get()) {
            bundled(player, "commands.login.cooldown", cooldownTime);
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.normalColor)
                .setTitle("Запрос на выдачу прав администратора.")
                .addField("Никнейм: ", player.name, true)
                .addField("UUID: ", player.uuid(), true)
                .setFooter("Нажми на реакцию чтобы подтвердить или отменить получение прав админа.");

        if (BotMain.bot != null && BotHandler.adminChannel != null) {
            Message message = new MessageBuilder().setEmbed(embed).addComponents(
                    ActionRow.of(
                            Button.success("confirm", "Подтвердить"),
                            Button.danger("deny", "Отклонить")
                    )
            ).send(BotHandler.adminChannel).join();

            BotHandler.waiting.put(message, player.uuid());

            vtime.reset();
            bundled(player, "commands.login.sent");
        }
    }
}
