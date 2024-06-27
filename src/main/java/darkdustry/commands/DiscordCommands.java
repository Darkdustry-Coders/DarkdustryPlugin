package darkdustry.commands;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import darkdustry.database.Database;
import darkdustry.database.models.ServerConfig;
import darkdustry.discord.DiscordBot;
import darkdustry.discord.MessageContext;
import darkdustry.features.net.Socket;
import darkdustry.listeners.SocketEvents.*;
import darkdustry.utils.*;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Mono;
import useful.Bundle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static darkdustry.PluginVars.*;
import static darkdustry.config.DiscordConfig.*;
import static darkdustry.utils.Checks.*;
import static mindustry.Vars.*;

public class DiscordCommands {
    public static void load() {
        discordHandler = new CommandHandler(discordConfig.prefix);

        discordHandler.<MessageContext>register("help", "List of all commands.", (args, context) -> {
            var builder = new StringBuilder();
            discordHandler.getCommandList()
                    .each(command -> builder.append(discordHandler.prefix).append("**").append(command.text)
                            .append("**").append(command.paramText.isEmpty() ? "" : " " + command.paramText)
                            .append(" - ").append(command.description).append("\n"));

            context.info("All available commands:", builder.toString()).subscribe();
        });

        discordHandler.<MessageContext>register("maps", "<server>", "List of all maps of the server.",
                PageIterator::maps);
        discordHandler.<MessageContext>register("players", "<server>", "List of all players of the server.",
                PageIterator::players);

        discordHandler.<MessageContext>register("status", "<server>", "Display server status.", (args, context) -> {
            var server = args[0];
            if (notFound(context, server))
                return;

            Socket.request(new StatusRequest(server), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("exit", "<server>", "Exit the server application.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs))
                return;

            var server = args[0];
            if (notFound(context, server))
                return;

            Socket.request(new ExitRequest(server), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("artv", "<server> [map...]", "Force map change.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs))
                return;

            var server = args[0];
            if (notFound(context, server))
                return;

            Socket.request(new ArtvRequest(server, args.length > 1 ? args[1] : null, context.member().getDisplayName()),
                    context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("map", "<server> <map...>", "Map", (args, context) -> {
            var server = args[0];
            if (notFound(context, server))
                return;

            Socket.request(new MapRequest(server, args[1]), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("uploadmap", "<server>", "Upload a map to the server.",
                (args, context) -> {
                    if (noRole(context, discordConfig.mapReviewerRoleIDs) || notMap(context))
                        return;

                    var server = args[0];
                    if (notFound(context, server))
                        return;

                    context.message()
                            .getAttachments()
                            .stream()
                            .filter(attachment -> attachment.getFilename().endsWith(mapExtension))
                            .forEach(attachment -> Http.get(attachment.getUrl(), response -> {
                                var file = tmpDirectory.child(attachment.getFilename());
                                file.writeBytes(response.getResult());

                                Socket.request(new UploadMapRequest(server, file.absolutePath()), context::reply,
                                        context::timeout);
                            }));
                });

        discordHandler.<MessageContext>register("removemap", "<server> <map...>", "Remove a map from the server.",
                (args, context) -> {
                    if (noRole(context, discordConfig.mapReviewerRoleIDs))
                        return;

                    var server = args[0];
                    if (notFound(context, server))
                        return;

                    Socket.request(new RemoveMapRequest(server, args[1]), context::reply, context::timeout);
                });

        discordHandler.<MessageContext>register("kick", "<server> <player> <duration> [reason...]", "Kick a player.",
                (args, context) -> {
                    if (noRole(context, discordConfig.adminRoleIDs))
                        return;

                    var server = args[0];
                    if (notFound(context, server))
                        return;

                    Socket.request(new KickRequest(server, args[1], args[2],
                            args.length > 3 ? args[3] : "Not Specified", context.member().getDisplayName()),
                            context::reply, context::timeout);
                });

        discordHandler.<MessageContext>register("pardon", "<server> <player...>", "Pardon a player.",
                (args, context) -> {
                    if (noRole(context, discordConfig.adminRoleIDs))
                        return;

                    var server = args[0];
                    if (notFound(context, server))
                        return;

                    Socket.request(new PardonRequest(server, args[1]), context::reply, context::timeout);
                });

        discordHandler.<MessageContext>register("ban", "<server> <player> <duration> [reason...]", "Ban a player.",
                (args, context) -> {
                    if (noRole(context, discordConfig.adminRoleIDs))
                        return;

                    var server = args[0];
                    if (notFound(context, server))
                        return;

                    Socket.request(new BanRequest(server, args[1], args[2], args.length > 3 ? args[3] : "Not Specified",
                            context.member().getDisplayName()), context::reply, context::timeout);
                });

        discordHandler.<MessageContext>register("unban", "<server> <player...>", "Unban a player.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs))
                return;

            var server = args[0];
            if (notFound(context, server))
                return;

            Socket.request(new UnbanRequest(server, args[1]), context::reply, context::timeout);
        });

        discordHandler.<MessageContext>register("stats", "<player...>", "Look up a player stats.", (args, context) -> {
            var data = Find.playerData(args[0]);
            if (notFound(context, data))
                return;

            context.info(embed -> embed
                    .title("Player Stats")
                    .addField("Player:", data.plainName(), false)
                    .addField("ID:", String.valueOf(data.id), false)
                    .addField("Rank:", data.rank.name(), false)
                    .addField("Blocks placed:", String.valueOf(data.blocksPlaced), false)
                    .addField("Blocks broken:", String.valueOf(data.blocksBroken), false)
                    .addField("Games played:", String.valueOf(data.gamesPlayed), false)
                    .addField("Waves survived:", String.valueOf(data.wavesSurvived), false)
                    .addField("Wins:",
                            Strings.format("""
                                    - Attack: @
                                    - Castle: @
                                    - Forts: @
                                    - Hexed: @
                                    - MS:GO: @
                                    - PvP: @
                                    - SPvP: @
                                    """, data.attackWins, data.castleWins,
                                    data.fortsOvas != 0 ? "(1vas: " + data.fortsOvas + ")" : data.fortsWins,
                                    data.hexedWins, data.msgoWins, data.pvpWins, data.spvpWins),
                            false)
                    .addField("Total playtime:", Bundle.formatDuration(Duration.ofMinutes(data.playTime)), false))
                    .subscribe();
        });

        discordHandler.<MessageContext>register("setrank", "<player> <rank>", "Set a player's rank.",
                (args, context) -> {
                    if (noRole(context, discordConfig.adminRoleIDs))
                        return;

                    var data = Find.playerData(args[0]);
                    if (notFound(context, data))
                        return;

                    var rank = Find.rank(args[1]);
                    if (notFound(context, rank))
                        return;

                    data.rank = rank;
                    Database.savePlayerData(data);

                    Socket.send(new SetRankSyncEvent(data.uuid, rank));
                    context.success(embed -> embed
                            .title("Rank Changed")
                            .addField("Player:", data.plainName(), false)
                            .addField("Rank:", rank.name(), false)).subscribe();
                });

        discordHandler.<MessageContext>register("link", "<code>", "Link your Mindustry account.", (args, context) -> {
            var author = context.message().getAuthor();
            if (author.isPresent()) {
                var data = Database.getPlayerData(author.get().getId());
                if (data != null) {
                    context.error(embed -> embed
                            .title("Account already linked")
                            .description("This user is already connected to an account")).subscribe();
                    return;
                }
                data = Database.getPlayerDataByCode(args[0]);
                if (data != null && data.discordId.isEmpty()) {
                    data.discordAttachCode = "";
                    data.discordId = author.get().getId().asBigInteger().toString();
                    Database.savePlayerData(data);
                    Socket.send(new DiscordLinkedEvent(data.uuid, author.get().getUsername(),
                            author.get().getId().asBigInteger().toString()));
                    context.success(embed -> embed
                            .title("Account linked successfully")).subscribe();
                    context.member().addRole(Snowflake.of(discordConfig.verifiedRoleID)).subscribe(v -> {
                    }, e -> {
                    });
                } else {
                    context.error(embed -> embed
                            .title("Code not found")
                            .description("This code doesn't correspond to any account")).subscribe();
                }
            }
        });

        discordHandler.<MessageContext>register("admins", "List all admins", (args, context) -> {
            var roles = discordConfig.adminRoleIDs
                    .map(x -> DiscordBot.gateway.getRoleById(context.member().getGuildId(), Snowflake.of(x)));
            Mono.zipDelayError(roles, Seq::with).map(x -> x.map(y -> (Role) y)).subscribe(
                    x -> {
                        x.sort(Comparator.comparingInt(Role::getRawPosition));
                        var added = new Seq<Snowflake>();
                        var list = new ObjectMap<Snowflake, Seq<Snowflake>>();

                        x.each(role -> {
                            var admins = new Seq<Snowflake>();
                            DiscordBot.admins
                                    .each(
                                            (id, roles2) -> {
                                                if (!added.contains(id) &&
                                                        roles2.contains(role.getId()))
                                                    admins.add(id);
                                            });
                            list.put(role.getId(), admins);
                        });

                        var actualList = new StringBuilder();

                        list.each((role, admins) -> {
                            actualList.append("<&").append(role.asLong()).append(">:\n");
                            admins.each(admin -> {
                                actualList.append("- <@").append(admin.asLong()).append(">\n");
                            });
                        });

                        context.success("List of Mindustry admins", actualList.toString()).subscribe();
                    });
        });

        discordHandler.<MessageContext>register("config", "[prop] [value...]", "Configure servers.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs))
                return;

            var options = ServerConfig.options();

            if (args.length < 1) {
                var builder = new StringBuilder();
                var first = new boolean[] {true};

                options.each(x -> {
                    if (!first[0]) {
                        builder.append('\n');

                    } else first[0] = false;
                    builder.append(x.key()).append(": ").append(x.get());
                });

                context.info("Server settings", builder.toString()).subscribe();
                return;
            }
            var property$ = args[0];
            var namespace$ = "global";
            if (property$.contains("#")) {
                final var index = property$.indexOf("#");
                namespace$ = property$.substring(0, index);
                property$ = property$.substring(index + 1);
            }
            final var property = property$;
            final var namespace = namespace$;

            if (args.length < 2) {
                var option = options.find(x -> x.key().equalsIgnoreCase(property));
                if (option == null) context.error(property, "No such property was found.").subscribe();
                else {
                    context.info(
                            property,
                            "Value: " + option.get() + "\n\n" + option.description())
                            .subscribe();
                }
                return;
            }
            var value = args[1];

            for (var possibleOption : options) {
                var opts = possibleOption.inPlace();
                if (options.isEmpty()) opts = Seq.with(possibleOption);
                else opts.add(possibleOption);

                for (var actualOption : opts) {
                    if (actualOption.key().equalsIgnoreCase(property)) {
                        @Nullable var error = actualOption.set(value, namespace);
                        if (error == null) {
                            context.success(property, "New value: " + actualOption.get()).subscribe();
                            // Just in case
                            Timer.schedule(() -> Socket.send(new ReconfigureEvent(property)), 0.5f);
                        }
                        else {
                            context.error(property, error).subscribe();
                        }
                    }
                }
            }
        });
    }
}
