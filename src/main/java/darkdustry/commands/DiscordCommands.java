package darkdustry.commands;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.util.Time;
import darkdustry.components.MapParser;
import darkdustry.components.Config.Gamemode;
import darkdustry.discord.Bot;
import darkdustry.discord.SlashContext;
import darkdustry.utils.Find;
import darkdustry.utils.PageIterator;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.net.Administration.Config;
import mindustry.net.Packets.KickReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

import java.awt.Color;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.sendToChat;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class DiscordCommands {

    public static final ObjectMap<String, Cons<SlashContext>> commands = new ObjectMap<>();

    public static void load() {
        register("status", "Посмотреть статус сервера.", context -> {
            if (isMenu(context)) return;

            context.sendEmbed(new EmbedBuilder()
                            .setColor(Color.green)
                            .setTitle(":desktop: " + stripAll(Config.serverName.string()))
                            .addField("Игроков:", String.valueOf(Groups.player.size()), true)
                            .addField("Карта:", state.map.name(), true)
                            .addField("Волна:", String.valueOf(state.wave), true)
                            .addField("TPS:", String.valueOf(graphics.getFramesPerSecond()), true)
                            .addField("Потребление ОЗУ:", Core.app.getJavaHeap() / 1024 / 1024 + " MB", true)
                            .addField("Сервер онлайн уже:", formatDuration(Time.timeSinceMillis(serverLoadTime)), true)
                            .addField("Время игры на текущей карте:", formatDuration(Time.timeSinceMillis(mapLoadTime)), true)
                            .addField("До следующей волны:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                            .setImage("attachment://minimap.png").build())
                    .addFile(MapParser.parseTiles(world.tiles), "minimap.png").queue();
        }).queue();

        register("players", "Список всех игроков на сервере.", PageIterator::players).addOption(OptionType.INTEGER, "page", "Страница списка игроков.", false).queue();

        register("kick", "Выгнать игрока с сервера.", context -> {
            if (notAdmin(context)) return;
            Player target = Find.player(context.getOption("name").getAsString());
            if (notFound(context, target)) return;

            kick(target, kickDuration, true, "kick.kicked");
            sendToChat("events.server.kick", target.name);
            context.info(":skull: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @", target.name, formatDuration(kickDuration)).queue();
        }).addOption(OptionType.STRING, "name", "Имя игрока, которого нужно выгнать.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
                .queue();

        register("ban", "Забанить игрока на сервере.", context -> {
            if (notAdmin(context)) return;
            Player target = Find.player(context.getOption("name").getAsString());
            if (notFound(context, target)) return;

            netServer.admins.banPlayer(target.uuid());
            kick(target, 0, true, "kick.banned");
            sendToChat("events.server.ban", target.name);
            context.info(":dagger: Игрок успешно забанен.", "@ больше не сможет зайти на сервер.", target.name).queue();
        }).addOption(OptionType.STRING, "name", "Имя игрока, которого нужно забанить.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .queue();

        register("restart", "Перезапустить сервер.", context -> {
            if (notAdmin(context)) return;

            // Сервер перезапустится только после отправки сообщения. Так надежнее.
            context.info(":gear: Сервер перезапускается...").queue(hook -> {
                netServer.kickAll(KickReason.serverRestarting);
                Bot.exit();
                Core.app.exit();
            });
        }).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).queue();

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

            context.sendEmbed(embed.build()).addFile(map.file.file()).addFile(MapParser.parseMap(map), "map.png").queue();
        }).addOption(OptionType.STRING, "map", "Название карты, которую вы хотите получить.", true).queue();

        register("maps", "Список всех карт сервера.", PageIterator::maps).addOption(OptionType.INTEGER, "page", "Страница списка карт.", false).queue();

        register("addmap", "Добавить карту на сервер.", context -> {
            if (notAdmin(context) || notMap(context)) return;

            Attachment attachment = context.getOption("map").getAsAttachment();
            attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
                Fi mapFile = new Fi(file);
                if (notMap(context, mapFile)) return;

                maps.reload();
                context.success(":map: Карта добавлена на сервер.").queue();
            });
        }).addOption(OptionType.ATTACHMENT, "map", "Файл карты, которую необходимо загрузить на сервер.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .queue();

        register("removemap", "Удалить карту с сервера.", context -> {
            if (notAdmin(context)) return;
            Map map = Find.map(context.getOption("map").getAsString());
            if (notFound(context, map)) return;

            maps.removeMap(map);
            maps.reload();
            context.success(":dagger: Карта удалена с сервера.").queue();
        }).addOption(OptionType.STRING, "map", "Название карты, которую необходимо удалить с сервера.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .queue();

        register("gameover", "Принудительно завершить игру.", context -> {
            if (notAdmin(context) || isMenu(context)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            context.success(":map: Игра успешно завершена.").queue();
        }).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).queue();
    }

    public static CommandCreateAction register(String name, String description, Cons<SlashContext> cons) {
        commands.put(name, cons);
        return botGuild.upsertCommand(name, description);
    }
}
