package rewrite.commands;

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
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import pandorum.components.MapParser;
import rewrite.components.Config.Gamemode;
import rewrite.discord.MessageContext;
import rewrite.utils.Find;

import java.awt.Color;
import java.util.Locale;

import static arc.Core.*;
import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.sendToChat;
import static rewrite.utils.Checks.*;
import static rewrite.utils.Utils.*;

public class DiscordCommands extends Commands<MessageContext> {

    public DiscordCommands(CommandHandler handler, Locale def) {
        super(handler, def);

        register("help", (args, context) -> {
            StringBuilder commands = new StringBuilder();
            for (Command command : discordCommands.getCommandList()) {
                commands.append(discordCommands.getPrefix()).append("**").append(command.text).append("**");
                if (!command.paramText.isEmpty()) commands.append(" *").append(command.paramText).append("*");
                commands.append(" - ").append(command.description).append("\n");
            }

            context.info(":newspaper: Доступные команды:", commands.toString());
        });

        register("ip", (args, context) -> {
            context.info(":desktop: " + stripAll(Config.serverName.string()), "IP: @:@", config.hubIp, Config.port.num());
        });

        register("players", (args, context) -> {
            if (notPage(context, args)) return;
            if (Groups.player.isEmpty()) {
                context.info(":satellite: На сервере нет игроков.");
                return;
            }

            int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Mathf.ceil(Groups.player.size() / 16f);
            if (notPage(context, page, pages)) return;

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

        register("status", (args, context) -> {
            if (isMenu(context)) return;
            context.channel.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.green)
                    .setTitle(":desktop: " + stripAll(Config.serverName.string()))
                    .addField("Игроков:", String.valueOf(Groups.player.size()), true)
                    .addField("Карта:", state.map.name(), true)
                    .addField("Волна:", String.valueOf(state.wave), true)
                    .addField("TPS:", String.valueOf(graphics.getFramesPerSecond()), true)
                    .addField("До следующей волны:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                    .setImage("attachment://minimap.png").build()).addFile(MapParser.parseTiles(world.tiles), "minimap.png").queue();
        });

        register("kick", (args, context) -> {
            if (notAdmin(context)) return;
            Player target = Find.player(args[0]);
            if (notFound(context, target)) return;

            kick(target, kickDuration, true, "kick.kicked");
            sendToChat("events.server.kick", target.name);
            context.info(":skull: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @", target.name, formatDuration(kickDuration));
        });

        register("ban", (args, context) -> {
            if (notAdmin(context)) return;
            Player target = Find.player(args[0]);
            if (notFound(context, target)) return;

            netServer.admins.banPlayer(target.uuid());
            kick(target, 0, true, "kick.banned");
            sendToChat("events.server.ban", target.name);
            context.info(":dagger: Игрок успешно забанен.", "@ больше не сможет зайти на сервер.", target.name);
        });

        if (config.mode.isDefault()) register("gameover", (args, context) -> {
            if (isMenu(context) || notAdmin(context)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            context.success(":map: Игра успешно завершена.");
        });

        if (config.mode == Gamemode.hexed) return;

        register("map", (args, context) -> {
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

        register("maps", (args, context) -> {
            if (notPage(context, args)) return;
            if (maps.customMaps().isEmpty()) {
                context.info(":map: На сервере нет карт.");
                return;
            }

            int page = args.length > 0 ? parseInt(args[0]) : 1, pages = Mathf.ceil(maps.customMaps().size / 16f);
            if (notPage(context, page, pages)) return;

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
    
        register("addmap", (args, context) -> {
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
    
        register("removemap", (args, context) -> {
            if (notAdmin(context)) return;
            Map map = Find.map(args[0]);
            if (notFound(context, map)) return;

            maps.removeMap(map);
            maps.reload();
            context.success(":dagger: Карта удалена с сервера.");
        });
    }
}
