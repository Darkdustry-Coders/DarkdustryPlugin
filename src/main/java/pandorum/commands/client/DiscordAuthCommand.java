package pandorum.commands.client;

import arc.util.CommandHandler;
import mindustry.gen.Player;
import mindustry.net.Administration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.awt.*;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.authWaiting;
import static pandorum.discord.Bot.botGuild;
import static pandorum.features.Authme.auth;

public class DiscordAuthCommand implements CommandHandler.CommandRunner<Player> {

    @Override
    public void accept(String[] args, Player player) {
        Administration.PlayerInfo info = netServer.admins.getInfo(player.uuid());

        User user = botGuild.getMemberById(args[0]).getUser();

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.yellow)
                .setTitle("**Запрос подтверждения:**")
                .setDescription("Если вы ничего не запрашивали - игнорируйте данное сообщение.")
                .addField("Никнейм:", info.lastName, true)
                .addField("UUID:", info.id, true);

        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(new MessageBuilder().setActionRows(ActionRow.of(auth)).build())).queue(
                dmMessage -> player.sendMessage("[sky]Подтверждение отправлено!"),
                response -> player.sendMessage("[scarlet]Невозможно отправить подтверждение. []Убедитесь, что вы разрешили личные сообщения от участников сервера.")
        );

        authWaiting.put(user.getId(), player.uuid());
    }
}
