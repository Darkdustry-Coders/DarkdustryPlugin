package pandorum.commands.discord;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.Context;

import java.awt.*;

public class PlayersListCommand {
    public static void run(final String[] args, final Context context) {
        if (args.length > 0 && !Strings.canParseInt(args[0])) {
            context.err(":interrobang: Ошибка.", "Страница должна быть числом.");
            return;
        }

        Seq<Player> playersList = Groups.player.copy(new Seq<>());
        if (playersList.isEmpty()) {
            context.info(":satellite: На сервере нет игроков.", "Список игроков пуст.");
            return;
        }

        int page = args.length > 0 ? Strings.parseInt(args[0]) : 1;
        int pages = Mathf.ceil(playersList.size / 16f);

        if (--page >= pages || page < 0) {
            context.err(":interrobang: Указана неверная страница списка игроков.", "Страница должна быть числом от 1 до @", pages);
            return;
        }

        StringBuilder players = new StringBuilder();
        for (int i = 16 * page; i < Math.min(16 * (page + 1), playersList.size); i++) {
            Player player = playersList.get(i);
            players.append("**").append(i + 1).append(".** ").append(Strings.stripColors(player.name)).append("\n");
        }

        context.sendEmbed(new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle(Strings.format(":satellite: Список игроков сервера (страница @ из @)", page + 1, pages))
                .addField(Strings.format("Всего игроков @", playersList.size), players.toString(), false)
                .build());
    }
}
