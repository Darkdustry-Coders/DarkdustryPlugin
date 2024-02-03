package darkdustry.commands;

import arc.util.*;
import darkdustry.database.Database;
import darkdustry.discord.MessageContext;
import darkdustry.features.net.Socket;
import darkdustry.listeners.SocketEvents.*;
import darkdustry.utils.*;
import useful.Bundle;

import java.time.Duration;

import static darkdustry.PluginVars.*;
import static darkdustry.config.DiscordConfig.*;
import static darkdustry.utils.Checks.*;
import static mindustry.Vars.*;

public class DiscordCommands {

    public static void load() {
        discordHandler = new CommandHandler(discordConfig.prefix);

        discordHandler.<MessageContext>register("help", "List of all commands.", (args, context) -> {
            var builder = new StringBuilder();
            discordHandler.getCommandList().each(command -> builder.append(discordHandler.prefix).append("**").append(command.text).append("**").append(command.paramText.isEmpty() ? "" : " " + command.paramText).append(" - ").append(command.description).append("\n"));

            context.info("All available commands:", builder.toString()).subscribe();
        });

        discordHandler.<MessageContext>register("maps", "<server>", "List of all maps of the server.", PageIterator::maps);
        discordHandler.<MessageContext>register("players", "<server>", "List of all players of the server.", PageIterator::players);

        discordHandler.<MessageContext>register("status", "<server>", "Display server status.", (args, context) -> {
            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new StatusRequest(server), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("exit", "<server>", "Exit the server application.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs)) return;

            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new ExitRequest(server), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("artv", "<server> [map...]", "Force map change.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs)) return;

            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new ArtvRequest(server, args.length > 1 ? args[1] : null, context.member().getDisplayName()), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("map", "<server> <map...>", "Map", (args, context) -> {
            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new MapRequest(server, args[1]), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("uploadmap", "<server>", "Upload a map to the server.", (args, context) -> {
            if (noRole(context, discordConfig.mapReviewerRoleIDs) || notMap(context)) return;

            var server = args[0];
            if (notFound(context, server)) return;

            context.message()
                    .getAttachments()
                    .stream()
                    .filter(attachment -> attachment.getFilename().endsWith(mapExtension))
                    .forEach(attachment -> Http.get(attachment.getUrl(), response -> {
                        var file = tmpDirectory.child(attachment.getFilename());
                        file.writeBytes(response.getResult());

                        Socket.request(new UploadMapRequest(server, file.absolutePath()), context::reply, context::timeout);
                    }));
        });

        discordHandler.<MessageContext>register("removemap", "<server> <map...>", "Remove a map from the server.", (args, context) -> {
            if (noRole(context, discordConfig.mapReviewerRoleIDs)) return;

            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new RemoveMapRequest(server, args[1]), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("kick", "<server> <player> <duration> [reason...]", "Kick a player.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs)) return;

            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new KickRequest(server, args[1], args[2], args.length > 3 ? args[3] : "Not Specified", context.member().getDisplayName()), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("pardon", "<server> <player...>", "Pardon a player.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs)) return;

            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new PardonRequest(server, args[1]), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("ban", "<server> <player> <duration> [reason...]", "Ban a player.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs)) return;

            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new BanRequest(server, args[1], args[2], args.length > 3 ? args[3] : "Not Specified", context.member().getDisplayName()), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("unban", "<server> <player...>", "Unban a player.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs)) return;

            var server = args[0];
            if (notFound(context, server)) return;

            Socket.request(new UnbanRequest(server, args[1]), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("stats", "<player...>", "Look up a player stats.", (args, context) -> {
            var data = Find.playerData(args[0]);
            if (notFound(context, data)) return;

            context.info(embed -> embed
                    .title("Player Stats")
                    .addField("Player:", data.plainName(), false)
                    .addField("ID:", String.valueOf(data.id), false)
                    .addField("Rank:", data.rank.name(), false)
                    .addField("Blocks placed:", String.valueOf(data.blocksPlaced), false)
                    .addField("Blocks broken:", String.valueOf(data.blocksBroken), false)
                    .addField("Games played:", String.valueOf(data.gamesPlayed), false)
                    .addField("Waves survived:", String.valueOf(data.wavesSurvived), false)
                    .addField("Wins:", Strings.format("""
                            - Attack: @
                            - Castle: @
                            - Forts: @
                            - Hexed: @
                            - MS:GO: @
                            - PvP: @
                            """, data.attackWins, data.castleWins, data.fortsWins, data.hexedWins, data.msgoWins, data.pvpWins), false)
                    .addField("Total playtime:", Bundle.formatDuration(Duration.ofMinutes(data.playTime)), false)
            ).subscribe();
        });

        discordHandler.<MessageContext>register("setrank", "<player> <rank>", "Set a player's rank.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs)) return;

            var data = Find.playerData(args[0]);
            if (notFound(context, data)) return;

            var rank = Find.rank(args[1]);
            if (notFound(context, rank)) return;

            data.rank = rank;
            Database.savePlayerData(data);

            Socket.send(new SetRankSyncEvent(data.uuid, rank));
            context.success(embed -> embed
                    .title("Rank Changed")
                    .addField("Player:", data.plainName(), false)
                    .addField("Rank:", rank.name(), false)).subscribe();
        });
    }
}
