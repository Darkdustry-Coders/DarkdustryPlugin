package darkdustry.commands;

import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import darkdustry.components.MapParser;
import darkdustry.components.Config.Gamemode;
import darkdustry.discord.SlashContext;
import darkdustry.utils.Find;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

import java.awt.Color;

import static arc.Core.*;
import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.sendToChat;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class DiscordCommand {

    public static final ObjectMap<String, Cons<SlashContext>> commands = new ObjectMap<>();

    public static void load() {
        register("status", "Посмотреть статус сервера.", context -> {
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

        register("players", "Список всех игроков на сервере.", context -> {
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

        register("kick", "Выгнать игрока с сервера.", context -> {
            if (notAdmin(context)) return;
            Player target = Find.player(context.getOption("nickname").getAsString());
            if (notFound(context, target)) return;

            kick(target, kickDuration, true, "kick.kicked");
            sendToChat("events.server.kick", target.name);
            context.info(":skull: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @", target.name, formatDuration(kickDuration));
        }).addOption(OptionType.STRING, "nickname", "Имя игрока, которого нужно выгнать.", true);

        register("ban", "Забанить игрока на сервере.", context -> {
            if (notAdmin(context)) return;
            Player target = Find.player(context.getOption("nickname").getAsString());
            if (notFound(context, target)) return;

            netServer.admins.banPlayer(target.uuid());
            kick(target, 0, true, "kick.banned");
            sendToChat("events.server.ban", target.name);
            context.info(":dagger: Игрок успешно забанен.", "@ больше не сможет зайти на сервер.", target.name);
        }).addOption(OptionType.STRING, "nickname", "Имя игрока, которого нужно забанить.", true);

        if (config.mode == Gamemode.hexed) return;

        register("map", "Получить карту с сервера.", context -> {
            Map map = Find.map(context.getOption("map").getAsString());
            if (notFound(context, map)) return;

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.yellow)
                    .setTitle(":map: " + map.name())
                    .setFooter(map.width + "x" + map.height)
                    .setImage("attachment://map.png");

            if (!map.author().equals("unknown")) embed.setAuthor(map.author());
            if (!map.description().equals("unknown")) embed.setDescription(map.description());

            context.channel.sendMessageEmbeds(embed.build()).addFile(map.file.file()).addFile(MapParser.parseMap(map), "map.png").queue();
        }).addOption(OptionType.STRING, "map", "Название карты, которую вы хотите получить.", true);

        register("maps", "Список всех карт сервера.", context -> {

        });

        register("addmap", "Добавить карту на сервер.", context -> {
            if (notAdmin(context) || notMap(context)) return;

            Attachment attachment = context.getOption("map").getAsAttachment();
            attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
                Fi mapFile = new Fi(file);
                if (notMap(context, mapFile)) return;

                maps.reload();
                context.success(":map: Карта добавлена на сервер.");
            }).exceptionally(e -> {
                context.error(":no_entry_sign: Файл поврежден или не является картой!");
                return null;
            });
        }).addOption(OptionType.ATTACHMENT, "map", "Файл карты, которую необходимо загрузить на сервер.", true).queue();

        register("removemap", "Удалить карту с сервера.", context -> {
            if (notAdmin(context)) return;
            Map map = Find.map(context.getOption("nickname").getAsString());
            if (notFound(context, map)) return;

            maps.removeMap(map);
            maps.reload();
            context.success(":dagger: Карта удалена с сервера.");
        }).addOption(OptionType.STRING, "map", "Название карты, которую необходимо удалить с сервера.", true);

        register("gameover", "Принудительно завершить игру.", context -> {
            if (notAdmin(context) || isMenu(context)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            context.success(":map: Игра успешно завершена.");
        }).queue();
    }

    public static CommandCreateAction register(String name, String description, Cons<SlashContext> cons) {
        commands.put(name, cons);
        return botGuild.upsertCommand(name, description);
    }
}
