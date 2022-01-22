package pandorum.commands.discord;

import arc.math.Mathf;
import arc.util.Strings;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.gen.Groups;

import static pandorum.discord.Bot.*;

public class PlayersListCommand {
    public static void run(final String[] args, final Message message) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            err(message.getChannel().block(), "Ошибка.", "Страница должна быть числом.");
            return;
        }

        if (Groups.player.isEmpty()) {
            err(message.getChannel().block(), "На сервере нет игроков.", "Список игроков пуст.");
            return;
        }

        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(Groups.player.size() / 20f);

        if (--page >= pages || page < 0) {
            err(message.getChannel().block(), "Указана неверная страница списка карт.", "Страница должна быть числом от 1 до @", pages);
            return;
        }

        StringBuilder players = new StringBuilder();
        for (int i = 20 * page; i < Math.min(20 * (page + 1), Groups.player.size()); i++) {
            players.append("**").append(i + 1).append(".** ").append(Strings.stripColors(Groups.player.index(i).name)).append("\n");
        }

        sendEmbed(message.getChannel().block(), EmbedCreateSpec.builder()
                .color(normalColor)
                .title(Strings.format("Список игроков сервера (страница @ из @)", page + 1, pages))
                .addField("Игроки:", players.toString(), false)
                .build()
        );
    }
}
