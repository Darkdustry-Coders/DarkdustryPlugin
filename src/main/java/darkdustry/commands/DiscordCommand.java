package darkdustry.commands;

import arc.Events;
import arc.func.Cons;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import darkdustry.components.MapParser;
import darkdustry.components.Config.Gamemode;
import darkdustry.discord.SlashContext;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

import java.awt.Color;

import static arc.Core.*;
import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class DiscordCommand {

    public static final ObjectMap<String, Cons<SlashContext>> commands = new ObjectMap<>();

    public static void load() {
        registerCommand("status", "Посмотреть статус сервера.", context -> {
            if (isMenu(context)) return;
            context.event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.green)
                    .setTitle(":desktop: " + stripAll(Config.serverName.string()))
                    .addField("Игроков:", String.valueOf(Groups.player.size()), true)
                    .addField("Карта:", state.map.name(), true)
                    .addField("Волна:", String.valueOf(state.wave), true)
                    .addField("TPS:", String.valueOf(graphics.getFramesPerSecond()), true)
                    .addField("До следующей волны:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                    .setImage("attachment://minimap.png").build()).addFile(MapParser.parseTiles(world.tiles), "minimap.png").queue();
        }).queue();

        registerCommand("players", "Список всех игроков на сервере.", context -> {
            // if (Groups.player.isEmpty()) {
            // context.info(":satellite: На сервере нет игроков.");
            // return;
            // }

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
            if (notAdmin(context) || isMenu(context)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            context.success(":map: Игра успешно завершена.");
        }).queue();
    }

    public static CommandCreateAction registerCommand(String name, String description, Cons<SlashContext> cons) {
        commands.put(name, cons);
        return botGuild.upsertCommand(name, description);
    }
}
