package darkdustry.commands;

import arc.Events;
import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import darkdustry.components.MapParser;
import darkdustry.components.Config.Gamemode;
import darkdustry.discord.MessageContext;
import darkdustry.utils.Find;

import java.awt.Color;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.sendToChat;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;

public class DiscordCommands extends Commands<MessageContext> {

    public DiscordCommands(CommandHandler handler) {
        super(handler);

        register("help", "Список всех команд.", (args, context) -> {
            StringBuilder commands = new StringBuilder();
            for (Command command : discordCommands.getCommandList()) {
                commands.append(discordCommands.getPrefix()).append("**").append(command.text).append("**");
                if (!command.paramText.isEmpty()) commands.append(" *").append(command.paramText).append("*");
                commands.append(" - ").append(command.description).append("\n");
            }

            context.info(":newspaper: Доступные команды:", commands.toString());
        });

        register("players", "[страница]", "Список всех игроков на сервере.", (args, context) -> {
            if (invalidPage(context, args)) return;
            if (Groups.player.isEmpty()) {
                context.info(":satellite: На сервере нет игроков.");
                return;
            }

            int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Mathf.ceil(Groups.player.size() / 16f);
            if (invalidPage(context, page, pages)) return;

            StringBuilder result = new StringBuilder();
            Seq<Player> list = Groups.player.copy(new Seq<>());
            for (int i = 16 * (page - 1); i < Math.min(16 * page, list.size); i++) {
                Player player = list.get(i);
                result.append("**").append(i + 1).append(".** ").append(stripColors(player.name)).append(" (ID: ").append(player.id).append(")\n");
            }

            context.sendEmbed(new EmbedBuilder()
                    .setColor(Color.cyan)
                    .setTitle(format(":satellite: Всего игроков на сервере: @", list.size))
                    .setDescription(result.toString())
                    .setFooter(format("Страница @ / @", page, pages)).build());
        });

        register("kick", "<ID/никнейм...>", "Выгнать игрока с сервера.", (args, context) -> {
            if (notAdmin(context)) return;
            Player target = Find.player(args[0]);
            if (notFound(context, target)) return;

            kick(target, kickDuration, true, "kick.kicked");
            sendToChat("events.server.kick", target.name);
            context.info(":skull: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @", target.name, formatDuration(kickDuration));
        });

        register("ban", "<ID/никнейм...>", "Забанить игрока на сервере.", (args, context) -> {
            if (notAdmin(context)) return;
            Player target = Find.player(args[0]);
            if (notFound(context, target)) return;

            netServer.admins.banPlayer(target.uuid());
            kick(target, 0, true, "kick.banned");
            sendToChat("events.server.ban", target.name);
            context.info(":dagger: Игрок успешно забанен.", "@ больше не сможет зайти на сервер.", target.name);
        });

        if (config.mode == Gamemode.hexed) return;

        register("gameover", "Принудительно завершить игру.", (args, context) -> {
            if (isMenu(context) || notAdmin(context)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            context.success(":map: Игра успешно завершена.");
        });

        register("map", "<название...>", "Получить карту с сервера.", (args, context) -> {
            Map map = Find.map(args[0]);
            if (notFound(context, map)) return;

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.yellow)
                    .setTitle(":map: " + map.name())
                    .setFooter(map.width + "x" + map.height)
                    .setImage("attachment://map.png");

            if (!map.author().equals("unknown")) embed.setAuthor(map.author());
            if (!map.description().equals("unknown")) embed.setDescription(map.description());

            context.channel.sendMessageEmbeds(embed.build()).addFile(map.file.file()).addFile(MapParser.parseMap(map), "map.png").queue();
        });

        register("maps", "[страница]", "Список всех карт сервера.", (args, context) -> {
            if (invalidPage(context, args)) return;
            if (maps.customMaps().isEmpty()) {
                context.info(":map: На сервере нет карт.");
                return;
            }

            int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Mathf.ceil(maps.customMaps().size / 16f);
            if (invalidPage(context, page, pages)) return;

            StringBuilder result = new StringBuilder();
            Seq<Map> list = maps.customMaps();
            for (int i = 16 * (page - 1); i < Math.min(16 * page, list.size); i++)
                result.append("**").append(i).append(".** ").append(stripColors(list.get(i).name())).append("\n");

            context.sendEmbed(new EmbedBuilder()
                    .setColor(Color.cyan)
                    .setTitle(format(":map: Всего карт на сервере: @", list.size))
                    .setDescription(result.toString())
                    .setFooter(format("Страница @ / @", page, pages)).build());
        });

        register("addmap", "Добавить карту на сервер.", (args, context) -> {
            if (notAdmin(context) || notMap(context)) return;

            Attachment attachment = context.message.getAttachments().get(0);
            attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
                Fi mapFile = new Fi(file);
                if (notMap(context, mapFile)) return;

                maps.reload();
                context.success(":map: Карта добавлена на сервер.");
            }).exceptionally(e -> {
                context.err(":no_entry_sign: Файл поврежден или не является картой!");
                return null;
            });
        });

        register("removemap", "<название...>", "Удалить карту с сервера.", (args, context) -> {
            if (notAdmin(context)) return;
            Map map = Find.map(args[0]);
            if (notFound(context, map)) return;

            maps.removeMap(map);
            maps.reload();
            context.success(":dagger: Карта удалена с сервера.");
        });
    }
}
