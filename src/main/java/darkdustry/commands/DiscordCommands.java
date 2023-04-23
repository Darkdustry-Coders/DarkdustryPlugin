package darkdustry.commands;

import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Http;
import arc.util.Time;
import darkdustry.DarkdustryPlugin;
import darkdustry.components.Cache;
import darkdustry.components.Database;
import darkdustry.discord.MessageContext;
import darkdustry.features.Ranks;
import darkdustry.utils.Admins;
import darkdustry.utils.Find;
import darkdustry.utils.PageIterator;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.MessageCreateFields.File;
import discord4j.rest.util.Color;
import mindustry.gen.Groups;
import mindustry.io.MapIO;
import useful.Bundle;

import static arc.Core.app;
import static arc.Core.graphics;
import static arc.util.Strings.parseInt;
import static arc.util.Strings.stripColors;
import static darkdustry.PluginVars.config;
import static darkdustry.PluginVars.discordCommands;
import static darkdustry.discord.Bot.adminRole;
import static darkdustry.discord.Bot.mapReviewerRole;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.formatDiscordDate;
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

        register("status", "Display server status.", (args, context) -> context.reply(embed -> embed
                .color(state.isPlaying() ? Color.MEDIUM_SEA_GREEN : Color.CINNABAR)
                .title(state.isPlaying() ? "Server Running" : "Server Offline")
                .addField("Players:", String.valueOf(Groups.player.size()), false)
                .addField("Units:", String.valueOf(Groups.unit.size()), false)
                .addField("Map:", stripColors(state.map.name()), false)
                .addField("Wave:", String.valueOf(state.wave), false)
                .addField("TPS:", String.valueOf(graphics.getFramesPerSecond()), false)
                .addField("RAM usage:", app.getJavaHeap() / 1024 / 1024 + " MB", false)).subscribe());

        register("exit", "Exit the server application.", adminRole, (args, context) -> context.success(embed -> embed.title("Shutting Down Server")).subscribe(message -> DarkdustryPlugin.exit()));

        register("map", "<map...>", "Map", (args, context) -> {
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

        register("removemap", "<map...>", "Remove a map from the server.", mapReviewerRole, (args, context) -> {
            var map = Find.map(args[0]);
            if (notFound(context, map)) return;

            maps.removeMap(map);
            maps.reload();

            context.success(embed -> embed
                    .title("Map Removed")
                    .addField("Name:", map.name(), false)
                    .addField("File:", map.file.name(), false)).subscribe();
        });

        register("ban", "<duration> <ID/username/uuid/ip...>", "Ban a player.", adminRole, (args, context) -> {
            int days = parseInt(args[0]);
            if (invalidDuration(context, days, 0, 365)) return;

            var info = Find.playerInfo(args[1]);
            if (notFound(context, info)) return;

            long duration = days * 24 * 60 * 60 * 1000L;
            Admins.ban(info.id, info.lastIP, duration);

            var target = Find.playerByUuid(info.id);
            if (target != null) {
                Admins.kick(target, duration, true, "kick.ban");
                Bundle.send("events.server.ban", target.coloredName());
            }

            context.success(embed -> embed
                    .title("Player Banned")
                    .addField("Name:", info.plainLastName(), false)
                    .addField("UUID:", info.id, false)
                    .addField("IP:", info.lastIP, false)
                    .addField("Unban date:", duration > 0 ? formatDiscordDate(Time.millis() + duration) : "permanent", false)).subscribe();
        });

        register("unban", "<uuid/ip...>", "Unban a player.", adminRole, (args, context) -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(context, info)) return;

            netServer.admins.unbanPlayerID(info.id);
            info.ips.each(netServer.admins::unbanPlayerIP);

            info.lastKicked = 0L;
            info.ips.each(netServer.admins.kickedIPs::remove);

            context.success(embed -> embed
                    .title("Player Unbanned")
                    .addField("Name:", info.plainLastName(), false)
                    .addField("UUID:", info.id, false)
                    .addField("IP:", info.lastIP, false)).subscribe();
        });

        register("stats", "<ID/username/uuid/ip...>", "Look up a player stats.", (args, context) -> {
            var info = Find.playerInfo(args[0]);
            if (notFound(context, info)) return;

            var data = Database.getPlayerData(info.id);
            context.info(embed -> embed
                    .title("Player Stats")
                    .addField("Name:", data.name, false)
                    .addField("Rank:", data.rank.name(), false)
                    .addField("Playtime:", data.playTime + " minutes", false)
                    .addField("Blocks placed:", String.valueOf(data.blocksPlaced), false)
                    .addField("Blocks broken:", String.valueOf(data.blocksBroken), false)
                    .addField("Waves survived:", String.valueOf(data.wavesSurvived), false)
                    .addField("Games played:", String.valueOf(data.gamesPlayed), false)
                    .addField("Attack wins:", String.valueOf(data.attackWins), false)
                    .addField("PvP wins:", String.valueOf(data.pvpWins), false)
                    .addField("Hexed wins:", String.valueOf(data.hexedWins), false)
            ).subscribe();
        });

        register("setrank", "<rank> <ID/username/uuid/ip...>", "Set a player's rank.", adminRole, (args, context) -> {
            var rank = Find.rank(args[0]);
            if (notFound(context, rank)) return;

            var info = Find.playerInfo(args[1]);
            if (notFound(context, info)) return;

            var data = Database.getPlayerData(info.id);
            data.rank = rank;

            var target = Find.playerByUuid(info.id);
            if (target != null) {
                Cache.put(target, data);
                Ranks.name(target, data);
            }

            Database.savePlayerData(data);
            context.success(embed -> embed
                    .title("Rank Changed")
                    .addField("Name:", info.plainLastName(), false)
                    .addField("UUID:", info.id, false)
                    .addField("Rank:", rank.name(), false)).subscribe();
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