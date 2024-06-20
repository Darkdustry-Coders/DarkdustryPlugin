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

        discordHandler.<MessageContext>register("config", "[prop] [value]", "Configure servers.", (args, context) -> {
            if (noRole(context, discordConfig.adminRoleIDs))
                return;

            var config = ServerConfig.get();

            if (args.length < 1) {
                context.info(
                        "Server settings",
                        "graylist-enabled: " + config.graylistEnabled + "\n" +
                                "graylist-mobile: " + config.graylistMobile + "\n" +
                                "graylist-hosting: " + config.graylistHosting + "\n" +
                                "graylist-proxy: " + config.graylistProxy + "\n" +
                                "graylist-isps: " + config.graylistISPs + "\n" +
                                "graylist-ips: " + config.graylistIPs + "\n")
                        .subscribe();
                return;
            }
            var property = args[0];

            if (property.contains("#")) {
                var arr = property.split("#");
                config = ServerConfig.get(arr[0]);
                property = arr[1];
            }

            if (args.length < 2) {
                switch (property) {
                    case "graylist-enabled" -> context.info(
                            property,
                            "Value: " + config.graylistEnabled + "\n\n" +
                                    "Enable enforcement of Discord integration " +
                                    "for specific users.")
                            .subscribe();
                    case "graylist-mobile" -> context.info(
                            property,
                            "Value: " + config.graylistMobile + "\n\n" +
                                    "Force all hotspot users to attach a Discord " +
                                    "account.")
                            .subscribe();
                    case "graylist-hosting" -> context.info(
                            property,
                            "Value: " + config.graylistHosting + "\n\n" +
                                    "Force all users connecting from hosting " +
                                    "companies IPs to attach a Discord account.")
                            .subscribe();
                    case "graylist-proxy" -> context.info(
                            property,
                            "Value: " + config.graylistProxy + "\n\n" +
                                    "Require users utilizing a proxy to attach a " +
                                    "Discord account.")
                            .subscribe();
                    case "graylist-isps" -> context.info(
                            property,
                            "Value: " + config.graylistProxy + "\n\n" +
                                    "ISPs that will be graylisted on the server")
                            .subscribe();
                    case "graylist-ips" -> context.info(
                            property,
                            "Value: " + config.graylistProxy + "\n\n" +
                                    "Graylisted IPs. Matches all IPs starting with " +
                                    "a value.")
                            .subscribe();
                    default -> context.error(property, "No such property was found.").subscribe();
                }
                return;
            }
            var value = args[1];

            class Values {
                public static final String boolError = "Invalid property value.\n\nPossible values: y/t/yes/true / n/f/no/false";

                /** returns '0' if false, '1' if true, '-1' if invalid */
                public static byte bool(String val) {
                    val = val.toLowerCase();

                    if (val.equals("y") || val.equals("t") || val.equals("yes") || val.equals("true"))
                        return 1;
                    if (val.equals("n") || val.equals("f") || val.equals("no") || val.equals("false"))
                        return 0;
                    return -1;
                }

                public static String isp(String val) {
                    // Adding or removing multiple ISPs is allowed :)
                    return val.replaceAll("[^\\w;]", "");
                }
            }

            switch (property) {
                case "graylist-enabled" -> {
                    var val = Values.bool(value);
                    if (val == -1) {
                        context.error(property, Values.boolError).subscribe();
                        return;
                    }
                    config.graylistEnabled = val == 1;
                    context.success(property, "New value: " + config.graylistEnabled).subscribe();
                }
                case "graylist-mobile" -> {
                    var val = Values.bool(value);
                    if (val == -1) {
                        context.error(property, Values.boolError).subscribe();
                        return;
                    }
                    config.graylistMobile = val == 1;
                    context.success(property, "New value: " + config.graylistMobile).subscribe();
                }
                case "graylist-hosting" -> {
                    var val = Values.bool(value);
                    if (val == -1) {
                        context.error(property, Values.boolError).subscribe();
                        return;
                    }
                    config.graylistHosting = val == 1;
                    context.success(property, "New value: " + config.graylistHosting).subscribe();
                }
                case "graylist-proxy" -> {
                    var val = Values.bool(value);
                    if (val == -1) {
                        context.error(property, Values.boolError).subscribe();
                        return;
                    }
                    config.graylistProxy = val == 1;
                    context.success(property, "New value: " + config.graylistProxy).subscribe();
                }
                case "graylist-isps" -> {
                    context.error(property, "Use `graylist-isps-add` and `graylist-isps-remove` instead").subscribe();
                }
                case "graylist-ips" -> {
                    context.error(property, "Use `graylist-ips-add` and `graylist-ips-remove` instead").subscribe();
                }
                case "graylist-isps-add" -> {
                    var val = Values.isp(value);
                    var list = new Seq<>(config.graylistISPs.split(";"));
                    new Seq<>(val.split(";")).each(e -> !list.contains(e), e -> list.add(e));
                    config.graylistISPs = list.toString(";");
                    context.success(property, "New value: " + config.graylistISPs).subscribe();
                }
                case "graylist-isps-remove" -> {
                    var val = Values.isp(value);
                    var list = new Seq<>(config.graylistISPs.split(";"));
                    list.removeAll(new Seq<>(val.split(";")));
                    config.graylistISPs = list.toString(";");
                    context.success(property, "New value: " + config.graylistISPs).subscribe();
                }
                case "graylist-ips-add" -> {
                    var list = new Seq<>(config.graylistIPs.split(";"));
                    new Seq<>(value.split(";")).each(e -> !list.contains(e), e -> list.add(e));
                    config.graylistIPs = list.toString(";");
                    context.success(property, "New value: " + config.graylistIPs).subscribe();
                }
                case "graylist-ips-remove" -> {
                    var list = new Seq<>(config.graylistIPs.split(";"));
                    list.removeAll(new Seq<>(value.split(";")));
                    config.graylistIPs = list.toString(";");
                    context.success(property, "New value: " + config.graylistIPs).subscribe();
                }
                default -> {
                    context.error(property, "No such property was found.").subscribe();
                    return;
                }
            }

            config.save();
            Socket.send(new ReconfigureEvent(property));
        });
    }
}
