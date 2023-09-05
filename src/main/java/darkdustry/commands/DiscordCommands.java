package darkdustry.commands;

import arc.util.*;
import darkdustry.components.*;
import darkdustry.discord.MessageContext;
import darkdustry.features.Ranks;
import darkdustry.listeners.SocketEvents.*;
import darkdustry.utils.*;
import useful.Bundle;

import java.time.Duration;

import static darkdustry.PluginVars.*;
import static darkdustry.discord.DiscordBot.*;
import static darkdustry.discord.DiscordConfig.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static discord4j.rest.util.Color.*;
import static mindustry.Vars.*;

public class DiscordCommands {

    public static void load() {
        discordCommands = new CommandHandler(discordConfig.prefix);

        discordCommands.<MessageContext>register("help", "List of all commands.", (args, context) -> {
            var builder = new StringBuilder();
            discordCommands.getCommandList().each(command -> builder.append(discordCommands.prefix).append("**").append(command.text).append("**").append(!command.paramText.isEmpty() ? " " + command.paramText : "").append(" - ").append(command.description).append("\n"));
            context.info("All available commands:", builder.toString()).subscribe();
        });

        discordCommands.<MessageContext>register("maps", "<server>", "List of all maps of the server.", PageIterator::maps);
        discordCommands.<MessageContext>register("players", "<server>", "List of all maps of the server.", PageIterator::players);

        discordCommands.<MessageContext>register("status", "<server>", "Display server status.", (args, context) -> {
            var server = args[0];
            if (notFoundServer(context, server)) return;

            Socket.request(new StatusRequest(server), response -> context.reply(embed -> embed
                    .color(response.playing ? MEDIUM_SEA_GREEN : CINNABAR)
                    .title(response.playing ? "Server Running" : "Server Offline")
                    .addField("Players:", String.valueOf(response.players), false)
                    .addField("Units:", String.valueOf(response.units), false)
                    .addField("Map:", response.mapName, false)
                    .addField("Wave:", String.valueOf(response.wave), false)
                    .addField("TPS:", String.valueOf(response.tps), false)
                    .addField("RAM usage:", response.javaHeap / 1024 / 1024 + " MB", false)
            ).subscribe(), context::timeout);
        });

        discordCommands.<MessageContext>register("exit", "<server>", "Exit the server application.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var server = args[0];
            if (notFoundServer(context, server)) return;

            Socket.request(new ExitRequest(server), response -> context.success(embed -> embed.title("Server Exited")).subscribe(), context::timeout);
        });

        discordCommands.<MessageContext>register("artv", "<server> [map...]", "Force map change.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var server = args[0];
            if (notFoundServer(context, server)) return;

            Socket.request(new ArtvRequest(server, args.length > 1 ? args[1] : null, context.member().getDisplayName()), context::reply, context::timeout);
        });

        discordCommands.<MessageContext>register("map", "<server> <map...>", "Map", (args, context) -> {
            var server = args[0];
            if (notFoundServer(context, server)) return;

            Socket.request(new MapRequest(server, args[1]), context::reply, context::timeout);
        });

        discordCommands.<MessageContext>register("uploadmap", "<server>", "Upload a map to the server.", (args, context) -> {
            if (noRole(context, mapReviewerRole) || notMap(context)) return;

            var server = args[0];
            if (notFoundServer(context, server)) return;

            context.message()
                    .getAttachments()
                    .stream()
                    .filter(attachment -> attachment.getFilename().endsWith(mapExtension))
                    .forEach(attachment -> Http.get(attachment.getUrl(), response -> {
                        var file = tmpDirectory.child(attachment.getFilename());
                        file.writeBytes(response.getResult());

                        Socket.request(new UploadMapRequest(server, file), context::reply, context::timeout);
                    }));
        });

        discordCommands.<MessageContext>register("removemap", "<server> <map...>", "Remove a map from the server.", (args, context) -> {
            if (noRole(context, mapReviewerRole)) return;

            var server = args[0];
            if (notFoundServer(context, server)) return;

            Socket.request(new RemoveMapRequest(server, args[1]), context::reply, context::timeout);
        });

        discordCommands.<MessageContext>register("kick", "<player> <duration> [reason...]", "Kick a player.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var target = Find.player(args[0]);
            if (notFound(context, target)) return;

            var duration = parseDuration(args[1]);
            if (invalidDuration(context, duration)) return;

            var reason = args.length > 2 ? args[2] : "Not Specified";
            Admins.kick(target, context.member().getDisplayName(), duration.toMillis(), reason);

            context.success(embed -> embed.title("Player Kicked")
                    .addField("Name:", target.plainName(), false)
                    .addField("Duration:", Bundle.formatDuration(duration), false)
                    .addField("Reason:", reason, false)).subscribe();
        });

        discordCommands.<MessageContext>register("pardon", "<player...>", "Pardon a player.", (args, context) -> {
            if (noRole(context, adminRole)) return;

            var info = Find.playerInfo(args[0]);
            if (notFound(context, info) || notKicked(context, info)) return;

            info.lastKicked = 0L;
            netServer.admins.kickedIPs.remove(info.lastIP);
            netServer.admins.dosBlacklist.remove(info.lastIP);

            context.success(embed -> embed.title("Player Pardoned").addField("Name:", info.plainLastName(), false)).subscribe();
        });

        discordCommands.

                <MessageContext>register("ban", "<player> <duration> [reason...]", "Ban a player.", (args, context) ->

        {
            if (noRole(context, adminRole)) return;

            var info = Find.playerInfo(args[0]);
            if (notFound(context, info)) return;

            var duration = parseDuration(args[1]);
            if (invalidDuration(context, duration)) return;

            var reason = args.length > 2 ? args[2] : "Not Specified";
            Admins.ban(info, context.member().getDisplayName(), duration.toMillis(), reason);

            context.success(embed -> embed.title("Player Banned")
                    .addField("Name:", info.plainLastName(), false)
                    .addField("Duration:", Bundle.formatDuration(duration), false)
                    .addField("Reason:", reason, false)).subscribe();
        });

        discordCommands.

                <MessageContext>register("unban", "<player...>", "Unban a player.", (args, context) ->

        {
            if (noRole(context, adminRole)) return;

            var ban = Database.removeBan(args[0]);
            if (notBanned(context, ban)) return;

            context.success(embed -> embed.title("Player Unbanned").addField("Name:", ban.player, false)).subscribe();
        });

        discordCommands.

                <MessageContext>register("stats", "<player...>", "Look up a player stats.", (args, context) ->

        {
            var data = Find.playerData(args[0]);
            if (notFound(context, data)) return;

            context.info(embed -> embed
                    .title("Player Stats")
                    .addField("Name:", data.plainName(), false)
                    .addField("ID:", String.valueOf(data.id), false)
                    .addField("Rank:", data.rank.name(), false)
                    .addField("Playtime:", Bundle.formatDuration(Duration.ofMinutes(data.playTime)), false)
                    .addField("Blocks placed:", String.valueOf(data.blocksPlaced), false)
                    .addField("Blocks broken:", String.valueOf(data.blocksBroken), false)
                    .addField("Waves survived:", String.valueOf(data.wavesSurvived), false)
                    .addField("Games played:", String.valueOf(data.gamesPlayed), false)
                    .addField("Attack wins:", String.valueOf(data.attackWins), false)
                    .addField("PvP wins:", String.valueOf(data.pvpWins), false)
                    .addField("Hexed wins:", String.valueOf(data.hexedWins), false)
            ).subscribe();
        });

        discordCommands.

                <MessageContext>register("setrank", "<player> <rank>", "Set a player's rank.", (args, context) ->

        {
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