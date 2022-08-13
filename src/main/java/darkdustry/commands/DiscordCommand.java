package darkdustry.commands;

import arc.Events;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import darkdustry.components.Config.Gamemode;
import darkdustry.discord.MessageContext;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.awt.*;

import static arc.util.Strings.format;
import static arc.util.Strings.stripColors;
import static darkdustry.PluginVars.config;
import static darkdustry.discord.SlashCommands.registerCommand;
import static mindustry.Vars.state;
import static darkdustry.utils.Checks.*;

public class DiscordCommand extends Commands<MessageContext> {

    public DiscordCommand(CommandHandler handler) {
        // TODO тут не нужен хандлер, и вообще Commands нам не нужны
        super(handler);

        registerCommand("help", "Список всех команд.", context -> {

        });

        registerCommand("status", "Посмотреть состояние сервера.", context -> {

        });

        registerCommand("players", "Список всех игроков на сервере.", context -> {
            //if (Groups.player.isEmpty()) {
            //    context.info(":satellite: На сервере нет игроков.");
            //    return;
            //}

            int page = context.getOption("page") != null ? context.getOption("page").getAsInt() : 1, pages = Mathf.ceil(Groups.player.size() / 8f);
            if (invalidPage(context, page, pages)) return;

            StringBuilder result = new StringBuilder();
            Seq<Player> list = Groups.player.copy(new Seq<>());
            for (int i = 8 * (page - 1); i < Math.min(8 * page, list.size); i++) {
                Player player = list.get(i);
                result.append("**").append(i + 1).append(".** ").append(stripColors(player.name)).append(" (ID: ").append(player.id).append(")\n");
            }

            context.sendEmbed(new EmbedBuilder()
                    .setColor(Color.cyan)
                    .setTitle(format(":satellite: Всего игроков на сервере: @", list.size))
                    .setDescription(result.toString())
                    .setFooter(format("Страница @ / @", page, pages)).build());
        }).addOption(OptionType.INTEGER, "page", "Страница списка игроков.", false).queue();

        registerCommand("kick", "Выгнать игрока с сервера.", context -> {

        });

        registerCommand("ban", "Забанить игрока на сервере.", context -> {

        });

        if (config.mode == Gamemode.hexed) return;

        registerCommand("map", "Получить карту с сервера.", context -> {

        });

        registerCommand("maps", "Список всех карт сервера.", context -> {

        });

        registerCommand("addmap", "Добавить карту на сервер.", context -> {

        }).addOption(OptionType.ATTACHMENT, "map", "Файл карты, которую необходимо загрузить на сервер.").queue();

        registerCommand("removemap", "Удалить карту с сервера.", context -> {

        });

        registerCommand("gameover", "Принудительно завершить игру.", context -> {
            // if (notAdmin(context) || isMenu(context)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            context.success(":map: Игра успешно завершена.");
        }).queue();
    }
}
