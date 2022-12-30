package darkdustry.commands;

import arc.Events;
import arc.files.Fi;
import arc.util.CommandHandler;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.*;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.Gamemode.*;
import static darkdustry.components.MapParser.*;
import static darkdustry.discord.Bot.Context;
import static darkdustry.discord.Bot.Palette.info;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.stripAll;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.serverName;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;

public class DiscordCommands {

    public static void load() {
        discordCommands = new CommandHandler(config.discordBotPrefix);

        discordCommands.<Context>register("help", "List of all commands", (args, context) -> {
            var builder = new StringBuilder();
            discordCommands.getCommandList().each(command -> builder.append(discordCommands.prefix).append("**").append(command.text).append("**").append(!command.paramText.isEmpty() ? " *" + command.paramText + "*" : "").append(" — ").append(command.description).append("\n"));
            context.info(":newspaper: All available commands:", builder.toString()).queue();
        });

        discordCommands.<Context>register("status", "Display server status.", (args, context) -> {
            if (notHosting(context)) return;

            context.info(stripAll(":satellite: " + serverName.string()), "Players: @\nUnits: @\nMap: @\nWave: @\nTPS: @\nRAM usage: @ MB", fromData(renderMinimap(), "minimap.png"), Groups.player.size(), Groups.unit.size(), state.map.name(), state.wave, graphics.getFramesPerSecond(), app.getJavaHeap() / 1024 / 1024).queue();
        });

        discordCommands.<Context>register("restart", "Restart the server.", (args, context) -> {
            if (notAdmin(context)) return;

            context.info(":arrows_counterclockwise: Server is restarting...").queue(message -> DarkdustryPlugin.exit());
        });

        discordCommands.<Context>register("players", "[page]", "List of all players.", PageIterator::players);

        if (config.mode == hexed || config.mode == industry) return;

        discordCommands.<Context>register("maps", "[page]", "List of all maps.", PageIterator::maps);

        discordCommands.<Context>register("map", "<map...>", "Get a map from the server.", (args, context) -> {
            var map = Find.map(args[0]);
            if (notFound(context, map, args[0])) return;

            var embed = new EmbedBuilder()
                    .setColor(info)
                    .setTitle(map.name())
                    .setDescription(map.tags.get("description", ""))
                    .setAuthor(map.tags.get("author", ""))
                    .setFooter(map.width + "x" + map.height)
                    .setImage("attachment://map.png");

            context.message().replyEmbeds(embed.build()).addFiles(fromData(map.file.readBytes(), map.file.name()), fromData(renderMap(map), "map.png")).queue();
        });

        discordCommands.<Context>register("uploadmap", "Upload a map to the server.", (args, context) -> {
            if (notAdmin(context) || notMap(context)) return;

            var attachment = context.message().getAttachments().get(0);
            attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenApply(Fi::new).thenAccept(fi -> {
                if (notMap(context, fi)) return;

                maps.reload();

                context.success(":map: Map **@** uploaded to the server.", fi.name()).queue();
            });
        });

        discordCommands.<Context>register("removemap", "<map...>", "Remove a map from the server.", (args, context) -> {
            if (notAdmin(context)) return;

            var map = Find.map(args[0]);
            if (notFound(context, map, args[0])) return;

            maps.removeMap(map);
            maps.reload();

            context.success(":map: Map **@** removed from the server.", map.name()).queue();
        });

        discordCommands.<Context>register("gameover", "Force a gameover.", (args, context) -> {
            if (notAdmin(context) || notHosting(context)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            context.info(":arrows_counterclockwise: Game over.").queue();
        });
    }
}