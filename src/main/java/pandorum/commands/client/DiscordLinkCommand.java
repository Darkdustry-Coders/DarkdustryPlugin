package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Log;
import mindustry.gen.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.Date;

import static pandorum.PluginVars.authWaiting;
import static pandorum.discord.Bot.botGuild;
import static pandorum.features.Authme.discord;

public class DiscordLinkCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        // bundled(player, "commands.discord.link", discordServerUrl);

        // TODO локализация команд

        String id = args[0];
        var member = botGuild.getMemberById(id);

        if(member == null) {
            player.sendMessage("[scarlet]Пользователь не найден, либо не является участником сервера.");
            return;
        }

        authWaiting.put(id, player.uuid());

        User user = member.getUser();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("**Привязка аккаунта к mindustry:**")
                .setDescription("Если вы ничего не запрашивали - игнорируйте данное сообщение.")
                .addField("**Никнейм:**", player.name(), true)
                .addField("**UUID:**", player.uuid(), true)
                .setTimestamp(new Date().toInstant());

        try {
            user.openPrivateChannel().complete().sendMessage(new MessageBuilder().setEmbeds(embed.build()).setActionRows(ActionRow.of(discord)).build()).queue();
            player.sendMessage("[orange]Ожидается подтверждение.");
        } catch (Exception e) {
            player.sendMessage("[scarlet]Не удалось отправить сообщение с подтверждением. Убедитесь, что пользователь может получать сообщения от участников сервера.");
            Log.err("Ошибка при попытке привязать discord аккаунт: ", e);
        }
    }
}
