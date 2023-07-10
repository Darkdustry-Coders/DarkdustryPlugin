package darkdustry.commands;

import arc.util.*;
import darkdustry.components.*;
import darkdustry.discord.MessageContext;
import darkdustry.features.Ranks;
import darkdustry.utils.*;
import discord4j.core.spec.MessageCreateFields.File;
import discord4j.rest.util.Color;
import mindustry.gen.Groups;
import mindustry.io.MapIO;
import mindustry.net.Packets.KickReason;
import useful.Bundle;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class DiscordCommands {

    public static void load() {
        discordCommands = new CommandHandler(config.discordBotPrefix);

        discordCommands.<MessageContext>register("help", "List of all commands.", (args, context) -> {
            var builder = new StringBuilder();
            discordCommands.getCommandList().each(command -> builder.append(discordCommands.prefix).append("**").append(command.text).append("**").append(!command.paramText.isEmpty() ? " " + command.paramText : "").append(" - ").append(command.description).append("\n"));
            context.info("All available commands:", builder.toString()).subscribe();
        });

        discordCommands.<MessageContext>register("maps", "[page]", "List of all maps.", PageIterator::maps);
        discordCommands.<MessageContext>register("players", "[page]", "List of all maps.", PageIterator::players);

        discordCommands.<MessageContext>register("status", "Display server status.", (args, context) -> context.reply(embed -> embed
                .color(state.isPlaying() ? Color.MEDIUM_SEA_GREEN : Color.CINNABAR)
                .title(state.isPlaying() ? "Server Running" : "Server Offline")
                .addField("Players:", String.valueOf(settings.getInt("totalPlayers", Groups.player.size())), false)
                .addField("Units:", String.valueOf(Groups.unit.size()), false)
                .addField("Map:", state.map.plainName(), false)
                .addField("Wave:", String.valueOf(state.wave), false)
                .addField("TPS:", String.valueOf(graphics.getFramesPerSecond()), false)
                .addField("RAM usage:", app.getJavaHeap() / 1024 / 1024 + " MB", false)).subscribe());

        discordCommands.<MessageContext>register("exit", "Exit the server application.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            context.success(embed -> embed.title("Shutting Down Server")).subscribe(message -> {
                netServer.kickAll(KickReason.serverRestarting);
                app.exit();
            });
        });

        discordCommands.<MessageContext>register("map", "<map...>", "Map", (args, context) -> {
            var map = Find.map(args[0]);
            if (notFound(context, map)) return;

            context.info(embed -> embed
                    .title(map.plainName())
                    .addField("Author:", map.plainAuthor(), false)
                    .addField("Description:", map.plainDescription(), false)
                    .footer(map.width + "x" + map.height, null)).withFiles(File.of(map.file.name(), map.file.read())).subscribe();
        });

        discordCommands.<MessageContext>register("uploadmap", "Upload a map to the server.", (args, context) -> {
            if (noRole(context, mapReviewerRole) || notMap(context)) return;

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

        discordCommands.<MessageContext>register("removemap", "<map...>", "Remove a map from the server.", (args, context) -> {
            if (noRole(context, mapReviewerRole)) return;

            var map = Find.map(args[0]);
            if (notFound(context, map)) return;

            maps.removeMap(map);
            maps.reload();

            context.success(embed -> embed
                    .title("Map Removed")
                    .addField("Name:", map.name(), false)
                    .addField("File:", map.file.name(), false)).subscribe();
        });

        discordCommands.<MessageContext>register("kick", "<player> <duration> [reason...]", "Kick a player.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var target = Find.player(args[0]);
            if (notFound(context, target)) return;

            var duration = parseDuration(args[1]);
            if (invalidDuration(context, duration)) return;

            var reason = args.length > 2 ? args[2] : "Not Specified";
            Admins.kick(target, "@" + context.member().getDisplayName(), duration.toMillis(), reason);

            context.success(embed -> embed.title("Player Kicked")
                    .addField("Name:", target.plainName(), false)
                    .addField("Duration:", Bundle.formatDuration(duration), false)
                    .addField("Reason:", reason, false)).subscribe();
        });

        discordCommands.<MessageContext>register("pardon", "<player...>", "Pardon a player.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var info = Find.playerInfo(args[0]);
            if (notFound(context, info)) return;

            info.lastKicked = 0L;
            info.ips.each(netServer.admins.kickedIPs::remove);

            context.success(embed -> embed.title("Player Pardoned").addField("Name:", info.plainLastName(), false)).subscribe();
        });

        discordCommands.<MessageContext>register("ban", "<player> <duration> [reason...]", "Ban a player.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var info = Find.playerInfo(args[0]);
            if (notFound(context, info)) return;

            var duration = parseDuration(args[1]);
            if (invalidDuration(context, duration)) return;

            var reason = args.length > 2 ? args[2] : "Not Specified";
            Admins.ban(info, "@" + context.member().getDisplayName(), duration.toMillis(), reason);

            context.success(embed -> embed.title("Player Banned")
                    .addField("Name:", info.plainLastName(), false)
                    .addField("Duration:", Bundle.formatDuration(duration), false)
                    .addField("Reason:", reason, false)).subscribe();
        });

        discordCommands.<MessageContext>register("unban", "<player...>", "Unban a player.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var ban = Database.removeBan(args[0]);
            if (notUnbanned(context, ban)) return;

            context.success(embed -> embed.title("Player Unbanned").addField("Name:", ban.player, false)).subscribe();
        });

        discordCommands.<MessageContext>register("stats", "<player...>", "Look up a player stats.", (args, context) -> {
            var data = Find.playerData(args[0]);
            if (notFound(context, data)) return;

            context.info(embed -> embed
                    .title("Player Stats")
                    .addField("Name:", data.plainName(), false)
                    .addField("ID:", String.valueOf(data.id), false)
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

        discordCommands.<MessageContext>register("setrank", "<player> <rank>", "Set a player's rank.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var data = Find.playerData(args[0]);
            if (notFound(context, data)) return;

            var rank = Find.rank(args[1]);
            if (notFound(context, rank)) return;

            data.rank = rank;

            var target = Find.playerByUUID(data.uuid);
            if (target != null) {
                Cache.put(target, data);
                Ranks.name(target, data);
            }

            Database.savePlayerData(data);
            context.success(embed -> embed
                    .title("Rank Changed")
                    .addField("Name:", data.plainName(), false)
                    .addField("Rank:", rank.name(), false)).subscribe();
        });
    }
}