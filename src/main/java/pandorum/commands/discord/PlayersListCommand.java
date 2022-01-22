package pandorum.commands.discord;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static pandorum.discord.Bot.*;

public class PlayersListCommand {
    public static void run(final String[] args, final Message message) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            err(message.getChannel().block(), "Ошибка.", "Страница должна быть числом.");
            return;
        }

        Seq<Player> playersList = Groups.player.copy(new Seq<>());
        if (playersList.isEmpty()) {
            err(message.getChannel().block(), "На сервере нет игроков.", "Список игроков пуст.");
            return;
        }

        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(playersList.size / 20f);

        if (--page >= pages || page < 0) {
            err(message.getChannel().block(), "Указана неверная страница списка игроков.", "Страница должна быть числом от 1 до @", pages);
            return;
        }

        StringBuilder players = new StringBuilder();
        for (int i = 20 * page; i < Math.min(20 * (page + 1), playersList.size); i++) {
            players.append("**").append(i + 1).append(".** ").append(Strings.stripColors(playersList.get(i).name)).append("\n");
        }

        sendEmbed(message.getChannel().block(), EmbedCreateSpec.builder()
                .color(normalColor)
                .title(Strings.format("Список игроков сервера (страница @ из @)", page + 1, pages))
                .addField(Strings.format("Всего игроков: @", playersList.size), players.toString(), false)
                .build()
        );
    }
}
