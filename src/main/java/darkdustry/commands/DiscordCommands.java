package darkdustry.commands;

import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.*;
import darkdustry.components.Config.Gamemode;
import darkdustry.discord.Bot;
import darkdustry.discord.SlashContext;
import darkdustry.utils.Find;
import darkdustry.utils.PageIterator;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.net.Administration.Config;
import mindustry.net.Packets.KickReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.awt.Color;

import static arc.Core.*;
import static arc.util.Time.timeSinceMillis;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.components.MapParser.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;
import static net.dv8tion.jda.api.Permission.*;
import static net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;
import static net.dv8tion.jda.api.utils.FileUpload.*;

public class DiscordCommands {

    public static final ObjectMap<String, Cons<SlashContext>> commands = new ObjectMap<>();
    public static final Seq<SlashCommandData> datas = new Seq<>();

    public static void load() {
        register("status", "Посмотреть статус сервера.", context -> {
            if (isMenu(context)) return;

            context.sendEmbed(new EmbedBuilder()
                            .setColor(Color.green)
                            .setTitle(":desktop: " + stripAll(Config.serverName.string()))
                            .addField("Игроков:", Integer.toString(Groups.player.size()), true)
                            .addField("Юнитов:", Integer.toString(Groups.unit.size()), true)
                            .addField("Карта:", state.map.name(), true)
                            .addField("Волна:", Integer.toString(state.wave), true)
                            .addField("TPS:", Integer.toString(graphics.getFramesPerSecond()), true)
                            .addField("Потребление ОЗУ:", app.getJavaHeap() / 1024 / 1024 + " MB", true)
                            .addField("Время работы сервера:", formatDuration(timeSinceMillis(serverLoadTime)), true)
                            .addField("Время игры на текущей карте:", formatDuration(timeSinceMillis(mapLoadTime)), true)
                            .addField("До следующей волны:", formatDuration((long) (state.wavetime / 60 * 1000)), true)
                            .setImage("attachment://minimap.png").build()
                    ).addFiles(fromData(renderMinimap(), "minimap.png"))
                    .queue();
        });

        register("players", "Список всех игроков на сервере.", PageIterator::players).addOption(INTEGER, "page", "Страница списка игроков.");

        register("kick", "Выгнать игрока с сервера.", context -> {
            if (notAdmin(context)) return;
            var target = Find.player(context.getOption("name").getAsString());
            if (notFound(context, target)) return;

            kick(target, kickDuration, true, "kick.kicked");
            sendToChat("events.server.kick", target.name);
            context.info(":skull: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @", target.name, formatDuration(kickDuration)).queue();
        }).addOption(STRING, "name", "Имя игрока, которого нужно выгнать.", true).setDefaultPermissions(enabledFor(KICK_MEMBERS));

        register("ban", "Забанить игрока на сервере.", context -> {
            if (notAdmin(context)) return;
            var target = Find.player(context.getOption("name").getAsString());
            if (notFound(context, target)) return;

            netServer.admins.banPlayer(target.uuid());
            kick(target, 0, true, "kick.banned");
            sendToChat("events.server.ban", target.name);
            context.info(":dagger: Игрок успешно забанен.", "@ больше не сможет зайти на сервер.", target.name).queue();
        }).addOption(STRING, "name", "Имя игрока, которого нужно забанить.", true).setDefaultPermissions(enabledFor(BAN_MEMBERS));

        register("restart", "Перезапустить сервер.", context -> {
            if (notAdmin(context)) return;

            // Сервер перезапустится только после отправки сообщения
            context.info(":gear: Сервер перезапускается...").queue(hook -> {
                netServer.kickAll(KickReason.serverRestarting);
                app.post(Bot::exit);
                app.exit();
            });
        }).setDefaultPermissions(enabledFor(ADMINISTRATOR));

        if (config.mode == Gamemode.hexed) return;

        register("map", "Получить карту с сервера.", context -> {
            var map = Find.map(context.getOption("map").getAsString());
            if (notFound(context, map)) return;

            context.sendEmbed(new EmbedBuilder()
                            .setColor(Color.yellow)
                            .setTitle(":map: " + map.name())
                            .setAuthor(map.tags.get("author"))
                            .setDescription(map.tags.get("description"))
                            .setFooter(map.width + "x" + map.height)
                            .setImage("attachment://map.png")
                            .build()
                    ).addFiles(fromData(map.file.file()))
                    .addFiles(fromData(renderMap(map), "map.png"))
                    .queue();
        }).addOption(STRING, "map", "Название карты, которую вы хотите получить.", true);

        register("maps", "Список всех карт сервера.", PageIterator::maps).addOption(INTEGER, "page", "Страница списка карт.");

        register("addmap", "Добавить карту на сервер.", context -> {
            if (notAdmin(context) || notMap(context)) return;

            var attachment = context.getOption("map").getAsAttachment();
            attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
                if (notMap(context, new Fi(file))) return;

                maps.reload();
                context.success(":map: Карта добавлена на сервер.").queue();
            });
        }).addOption(ATTACHMENT, "map", "Файл карты, которую необходимо загрузить на сервер.", true).setDefaultPermissions(enabledFor(ADMINISTRATOR));

        register("removemap", "Удалить карту с сервера.", context -> {
            if (notAdmin(context)) return;
            var map = Find.map(context.getOption("map").getAsString());
            if (notFound(context, map)) return;

            maps.removeMap(map);
            maps.reload();
            context.success(":dagger: Карта удалена с сервера.").queue();
        }).addOption(STRING, "map", "Название карты, которую необходимо удалить с сервера.", true).setDefaultPermissions(enabledFor(ADMINISTRATOR));

        register("gameover", "Принудительно завершить игру.", context -> {
            if (notAdmin(context) || isMenu(context)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            context.success(":map: Игра успешно завершена.").queue();
        }).setDefaultPermissions(enabledFor(ADMINISTRATOR));

        // Регистрируем все команды одним запросом
        jda.updateCommands().addCommands(datas.toArray(CommandData.class)).queue();
    }

    public static SlashCommandData register(String name, String description, Cons<SlashContext> cons) {
        commands.put(name, cons);
        return datas.add(new CommandDataImpl(name, description)).peek();
    }
}
