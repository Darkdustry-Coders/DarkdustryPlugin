package pandorum.commands.discord;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;

import static pandorum.discord.Bot.err;
import static pandorum.discord.Bot.info;

public class PlayersListCommand {
    public static void run(final String[] args, final Message message) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            err(message.getChannel(), ":interrobang: ошибка.", "Страница должна быть числом.");
            return;
        }

        Seq<Player> playersList = Groups.player.copy(new Seq<>());
        if (playersList.isEmpty()) {
            info(message.getChannel(), ":satellite: на сервере нет игроков.", "Список игроков пуст.");
            return;
        }

        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(playersList.size / 16f);

        if (--page >= pages || page < 0) {
            err(message.getChannel(), ":interrobang: указана неверная страница списка игроков.", "Страница должна быть числом от 1 до @", pages);
            return;
        }

        StringBuilder players = new StringBuilder();
        for (int i = 16 * page; i < Math.min(16 * (page + 1), playersList.size); i++) {
            Player player = playersList.get(i);
            players.append("**").append(i + 1).append(".** ").append(Strings.stripColors(player.name)).append("\n");
        }

        message.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.blue)
                .setTitle(Strings.format(":satellite: список игроков сервера (страница @ из @)", page + 1, pages))
                .addField(Strings.format("Всего игроков @", playersList.size), players.toString(), false)
                .build()).queue();
    }
}
