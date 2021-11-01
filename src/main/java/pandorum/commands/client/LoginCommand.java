package pandorum.commands.client;

import arc.util.Timekeeper;
import mindustry.gen.Player;
import net.dv8tion.jda.api.EmbedBuilder;
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
                .addField("Никнейм: ", player.name, false)
                .addField("UUID: ", player.uuid(), false)
                .setDescription("Нажми на реакцию чтобы подтвердить или отменить получение прав админа.");

        BotHandler.adminChannel.sendMessageEmbeds(embed.build()).queue(message -> {
            BotHandler.waiting.put(message.getIdLong(), player.uuid());
            message.addReaction(BotHandler.guild.getEmotesByName("white_check_mark", false).get(0));
            message.addReaction(BotHandler.guild.getEmotesByName("x", false).get(0));
        });

        vtime.reset();
        bundled(player, "commands.login.sent");
    }
}
