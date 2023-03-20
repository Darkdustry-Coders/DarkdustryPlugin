package darkdustry.commands;

import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Http;
import arc.util.Strings;
import darkdustry.discord.MessageContext;
import darkdustry.utils.Find;
import darkdustry.utils.PageIterator;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.MessageCreateFields.File;
import discord4j.rest.util.Color;
import mindustry.gen.Groups;
import mindustry.io.MapIO;

import static arc.Core.app;
import static arc.Core.graphics;
import static arc.util.Strings.stripColors;
import static darkdustry.PluginVars.config;
import static darkdustry.PluginVars.discordCommands;
import static darkdustry.discord.Bot.mapReviewerRole;
import static darkdustry.utils.Checks.*;
import static mindustry.Vars.*;

public class DiscordCommands {

    public static void load() {
        discordCommands = new CommandHandler(config.discordBotPrefix);

        register("help", "List of all commands.", (args, context) -> {
            var builder = new StringBuilder();
            discordCommands.getCommandList().each(command -> builder.append(discordCommands.prefix).append("**").append(command.text).append("**").append(!command.paramText.isEmpty() ? " " + command.paramText : "").append(" - ").append(command.description).append("\n"));
            context.info("All available commands:", builder.toString()).subscribe();
        });

        register("maps", "[page]", "List of all maps.", PageIterator::maps);
        register("players", "[page]", "List of all maps.", PageIterator::players);

        register("status", "Display server status.", (args, context) -> {
            context.reply(embed -> embed
                    .color(state.isPlaying() ? Color.MEDIUM_SEA_GREEN : Color.CINNABAR)
                    .title(state.isPlaying() ? "Server Running" : "Server Offline")
                    .description(Strings.format("Players: @\nUnits: @\nMap: @\nWave: @\nTPS: @\nRAM usage: @ MB", Groups.player.size(), Groups.unit.size(), stripColors(state.map.name()), state.wave, graphics.getFramesPerSecond(), app.getJavaHeap() / 1024 / 1024))).subscribe();
        });

        register("map", "<map>", "Map", (args, context) -> {
            var map = Find.map(args[0]);
            if (notFound(context, map)) return;

            context.info(embed -> embed
                    .title(stripColors(map.name()))
                    .addField("Author:", stripColors(map.author()), false)
                    .addField("Description:", stripColors(map.description()), false)
                    .footer(map.width + "x" + map.height, null)).withFiles(File.of(map.file.name(), map.file.read())).subscribe();
        });

        register("uploadmap", "Upload a map to the server.", mapReviewerRole, (args, context) -> {
            if (notMap(context)) return;

            context.message()
                    .getAttachments()
                    .stream()
                    .filter(attachment -> attachment.getFilename().endsWith(mapExtension))
                    .forEach(attachment -> Http.get(attachment.getUrl(), response -> {
                        var file = customMapDirectory.child(attachment.getFilename());
                        file.writeBytes(response.getResult());

                        var map = MapIO.createMap(file, true);
                        maps.reload();

                        context.success(embed -> embed
                                .title("Map Uploaded")
                                .addField("Name:", map.name(), false)
                                .addField("File:", file.name(), false)).subscribe();
                    }, error -> {
                        customMapDirectory.child(attachment.getFilename()).delete();
                        context.error("Invalid Attachment", "**@** is not a valid map.", attachment.getFilename()).subscribe();
                    }));
        });

        register("removemap", "<map>", "Remove a map from the server.", mapReviewerRole, (args, context) -> {
            var map = Find.map(args[0]);
            if (notFound(context, map)) return;

            maps.removeMap(map);
            maps.reload();

            context.success(embed -> embed
                    .title("Map Removed")
                    .addField("Name:", map.name(), false)
                    .addField("File:", map.file.name(), false)).subscribe();
        });
    }

    public static void register(String name, String description, CommandRunner<MessageContext> runner) {
        register(name, "", description, runner);
    }

    public static void register(String name, String description, Role role, CommandRunner<MessageContext> runner) {
        register(name, "", description, role, runner);
    }

    public static void register(String name, String params, String description, CommandRunner<MessageContext> runner) {
        discordCommands.register(name, params, description, runner);
    }

    public static void register(String name, String params, String description, Role role, CommandRunner<MessageContext> runner) {
        discordCommands.<MessageContext>register(name, params, description, (args, context) -> {
            if (noRole(context, role)) return;
            runner.accept(args, context);
        });
    }
}