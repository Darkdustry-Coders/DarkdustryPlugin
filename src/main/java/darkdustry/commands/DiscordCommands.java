package darkdustry.commands;

import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.*;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;

import static arc.Core.*;
import static arc.util.Time.timeSinceMillis;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.Gamemode.hexed;
import static darkdustry.components.MapParser.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Administration.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static java.util.Objects.requireNonNull;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.serverName;
import static net.dv8tion.jda.api.Permission.*;
import static net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;

public class DiscordCommands {

    public static final ObjectMap<String, Cons<SlashCommandInteractionEvent>> commands = new ObjectMap<>();
    public static final Seq<SlashCommandData> datas = new Seq<>();

    public static void load() {
        register("status", "Посмотреть статус сервера.", event -> {
            if (notHosting(event)) return;

            var embed = info(":satellite: " + stripAll(serverName.string()),
                    """
                            Игроков: @
                            Карта: @
                            Волна: @
                            TPS: @
                            Потребление ОЗУ: @ МБ
                            Время работы сервера: @
                            Время игры на текущей карте: @
                            """, Groups.player.size(), state.map.name(), state.wave,
                    graphics.getFramesPerSecond(), app.getJavaHeap() / 1024 / 1024,
                    formatDuration(timeSinceMillis(serverLoadTime)), formatDuration(timeSinceMillis(mapLoadTime)))
                    .setImage("attachment://minimap.png");

            event.replyEmbeds(embed.build()).queue(hook ->
                    hook.editOriginalAttachments(fromData(renderMinimap(), "minimap.png")).queue());
        });

        register("players", "Список всех игроков на сервере.", PageIterator::players)
                .addOption(INTEGER, "page", "Страница списка игроков.");

        register("kick", "Выгнать игрока с сервера.", event -> {
            var target = Find.player(requireNonNull(event.getOption("name")).getAsString());
            if (notFound(event, target)) return;

            kick(target, "@" + requireNonNull(event.getMember()).getEffectiveName());

            event.replyEmbeds(info(":knife: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @", target.plainName(), formatDuration(kickDuration)).build()).queue();
        }).setDefaultPermissions(enabledFor(KICK_MEMBERS))
                .addOption(STRING, "name", "Имя игрока, которого нужно выгнать.", true);

        register("ban", "Забанить игрока на сервере.", event -> {
            var target = Find.player(requireNonNull(event.getOption("name")).getAsString());
            if (notFound(event, target)) return;

            ban(target, "@" + requireNonNull(event.getMember()).getEffectiveName());

            event.replyEmbeds(info(":dagger: Игрок успешно заблокирован.", "@ больше не сможет зайти на сервер.", target.plainName()).build()).queue();
        }).setDefaultPermissions(enabledFor(BAN_MEMBERS))
                .addOption(STRING, "name", "Имя игрока, которого нужно забанить.", true);

        register("restart", "Перезапустить сервер.", event -> event.replyEmbeds(info(":arrows_counterclockwise:  Сервер перезапускается...").build()).queue(hook -> DarkdustryPlugin.exit()))
                .setDefaultPermissions(DISABLED);

        if (config.mode == hexed) return;

        register("map", "Получить карту с сервера.", event -> {
            var map = Find.map(requireNonNull(event.getOption("map")).getAsString());
            if (notFound(event, map)) return;

            var embed = info(":map: " + map.name())
                    .setAuthor(map.tags.get("author"))
                    .setDescription(map.tags.get("description"))
                    .setFooter(map.width + "x" + map.height)
                    .setImage("attachment://map.png");

            event.replyEmbeds(embed.build()).queue(hook ->
                    hook.editOriginalAttachments(fromData(map.file.file()), fromData(renderMap(map), "map.png")).queue());
        }).addOption(STRING, "map", "Название карты, которую вы хотите получить.", true);

        register("maps", "Список всех карт сервера.", PageIterator::maps)
                .addOption(INTEGER, "page", "Страница списка карт.");

        register("addmap", "Добавить карту на сервер.", event -> {
            if (notMap(event)) return;

            var attachment = requireNonNull(event.getOption("map")).getAsAttachment();
            attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
                if (notMap(event, new Fi(file))) return;

                maps.reload();

                event.replyEmbeds(success(":map: Карта добавлена на сервер.", "Файл карты: @", file.getName()).build()).queue();
            });
        }).setDefaultPermissions(enabledFor(VIEW_AUDIT_LOGS))
                .addOption(ATTACHMENT, "map", "Файл карты, которую необходимо загрузить на сервер.", true);

        register("removemap", "Удалить карту с сервера.", event -> {
            var map = Find.map(requireNonNull(event.getOption("map")).getAsString());
            if (notFound(event, map)) return;

            maps.removeMap(map);
            maps.reload();

            event.replyEmbeds(success(":map: Карта удалена с сервера.", "Название карты: @", map.name()).build()).queue();
        }).setDefaultPermissions(enabledFor(VIEW_AUDIT_LOGS))
                .addOption(STRING, "map", "Название карты, которую необходимо удалить с сервера.", true);

        register("gameover", "Принудительно завершить игру.", event -> {
            if (notHosting(event)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            event.replyEmbeds(success(":arrows_counterclockwise:  Игра успешно завершена.").build()).queue();
        }).setDefaultPermissions(enabledFor(VIEW_AUDIT_LOGS));
    }

    public static SlashCommandData register(String name, String description, Cons<SlashCommandInteractionEvent> cons) {
        commands.put(name, cons);
        return datas.add(Commands.slash(name, description)).peek();
    }
}