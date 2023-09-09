package darkdustry.listeners;

import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.*;
import com.ospx.sock.EventBus.*;
import darkdustry.commands.DiscordCommands;
import darkdustry.components.*;
import darkdustry.components.Database.*;
import darkdustry.discord.DiscordBot;
import darkdustry.features.*;
import darkdustry.features.Ranks.Rank;
import darkdustry.utils.*;
import discord4j.core.spec.EmbedCreateFields.Field;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.*;
import mindustry.gen.Groups;
import mindustry.io.MapIO;
import mindustry.net.Packets.KickReason;
import useful.Bundle;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.*;
import static darkdustry.discord.DiscordConfig.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static discord4j.rest.util.Color.*;
import static mindustry.Vars.*;
import static mindustry.server.ServerControl.*;

public class SocketEvents {

    public static void load() {
        if (config.mode.isSockServer) {
            DiscordBot.connect();
            DiscordCommands.load();

            Socket.on(ServerMessageEvent.class, event -> {
                var channel = discordConfig.serverToChannel.get(event.server);
                if (channel == null) return;

                DiscordBot.sendMessage(channel, "`" + event.name + ": " + event.message + "`");
            });

            Socket.on(ServerMessageEmbedEvent.class, event -> {
                var channel = discordConfig.serverToChannel.get(event.server);
                if (channel == null) return;

                DiscordBot.sendMessageEmbed(channel, EmbedCreateSpec.builder().title(event.title).color(event.color).build());
            });

            Socket.on(BanSyncEvent.class, event -> Authme.sendBan(event.server, event.ban));
            Socket.on(AdminRequestEvent.class, event -> Authme.sendAdminRequest(event.server, event.data));
        }

        Socket.on(DiscordMessageEvent.class, event -> {
            if (!event.server.equals(config.mode.name())) return;

            if (event.role == null || event.color == null) {
                Log.info("[Discord] @: @", event.name, event.message);
                Bundle.send("discord.chat", event.name, event.message);
            } else {
                Log.info("[Discord] @ | @: @", event.role, event.name, event.message);
                Bundle.send("discord.chat.role", event.color, event.role, event.name, event.message);
            }
        });

        Socket.on(BanSyncEvent.class, event -> Groups.player.each(
                player -> player.uuid().equals(event.ban.uuid) || player.ip().equals(event.ban.ip),
                player -> {
                    Admins.kickReason(player, event.ban.remaining(), event.ban.reason, "kick.banned-by-admin", event.ban.admin).kick();
                    Bundle.send("events.admin.ban", event.ban.admin, player.coloredName(), event.ban.reason);
                }));

        Socket.on(AdminRequestConfirmEvent.class, event -> {
            if (event.server.equals(config.mode.name()))
                Authme.confirm(event.uuid);
        });

        Socket.on(AdminRequestDenyEvent.class, event -> {
            if (event.server.equals(config.mode.name()))
                Authme.deny(event.uuid);
        });

        Socket.on(SetRankSyncEvent.class, event -> {
            var player = Find.playerByUUID(event.uuid);
            if (player == null) return;

            var data = Cache.get(player);
            data.rank = event.rank;

            Ranks.name(player, data);
        });

        Socket.on(ListRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            switch (request.type) {
                case "maps" -> {
                    var maps = availableMaps();

                    int page = request.page, pages = Math.max(1, Mathf.ceil((float) maps.size / maxPerPage));
                    if (page < 1 || page > pages) return;

                    Socket.respond(request, new ListResponse(formatList(maps, page, (builder, index, map) -> builder.append("**").append(index).append(".** ").append(map.plainName()).append("\n").append("Author: ").append(map.plainAuthor()).append("\n").append(map.width).append("x").append(map.height).append("\n")), page, pages, maps.size));
                }

                case "players" -> {
                    var players = Groups.player.copy(new Seq<>());

                    int page = request.page, pages = Math.max(1, Mathf.ceil((float) players.size / maxPerPage));
                    if (page < 1 || page > pages) return;

                    Socket.respond(request, new ListResponse(formatList(players, page, (builder, index, player) -> builder.append("**").append(index).append(".** ").append(player.plainName()).append("\nID: ").append(Cache.get(player).id).append("\nLanguage: ").append(player.locale).append("\n")), page, pages, players.size));
                }

                default -> throw new IllegalStateException();
            }
        });

        Socket.on(StatusRequest.class, request -> {
            if (request.server.equals(config.mode.name()))
                Socket.respond(request, state.isPlaying() ?
                        EmbedResponse.success("Server Running")
                                .withField("Players:", String.valueOf(Groups.player.size()))
                                .withField("Units:", String.valueOf(Groups.unit.size()))
                                .withField("Map:", state.map.plainName())
                                .withField("Wave:", String.valueOf(state.wave))
                                .withField("TPS:", String.valueOf(graphics.getFramesPerSecond()))
                                .withField("RAM usage:", app.getJavaHeap() / 1024 / 1024 + " MB") :
                        EmbedResponse.error("Server Offline")
                                .withField("TPS:", String.valueOf(graphics.getFramesPerSecond()))
                                .withField("RAM usage:", app.getJavaHeap() / 1024 / 1024 + " MB")
                );
        });

        Socket.on(ExitRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            netServer.kickAll(KickReason.serverRestarting);
            app.exit();

            Socket.respond(request, EmbedResponse.success("Server Exited"));
        });

        Socket.on(ArtvRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var map = request.map == null ? maps.getNextMap(state.rules.mode(), state.map) : Find.map(request.map);
            if (notFound(request, map)) return;

            Bundle.send("commands.artv.info", request.admin);
            instance.play(false, () -> world.loadMap(map));

            Socket.respond(request, EmbedResponse.success("Map Changed").withField("Name:", map.plainName()));
        });

        Socket.on(MapRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var map = Find.map(request.map);
            if (notFound(request, map)) return;

            Socket.respond(request, EmbedResponse.success(map.plainName())
                    .withField("Author:", map.plainAuthor())
                    .withField("Description:", map.plainDescription())
                    .withFooter("@x@", map.width, map.height)
                    .withFile(map.file));
        });

        Socket.on(UploadMapRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var file = customMapDirectory.child(request.file.name());
            file.writeBytes(request.file.readBytes());

            try {
                var map = MapIO.createMap(file, true);
                maps.reload();

                Socket.respond(request, EmbedResponse.success("Map Uploaded")
                        .withField("Name:", map.name())
                        .withField("File:", file.name()));
            } catch (Exception error) {
                file.delete();
                Socket.respond(request, EmbedResponse.error("Invalid Map").withContent("**@** is not a valid map.", file.name()));
            }
        });

        Socket.on(RemoveMapRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var map = Find.map(request.map);
            if (notFound(request, map) || notRemoved(request, map)) return;

            maps.removeMap(map);
            maps.reload();

            Socket.respond(request, EmbedResponse.success("Map Removed")
                    .withField("Name:", map.name())
                    .withField("File:", map.file.name()));
        });

        Socket.on(KickRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var target = Find.player(request.player);
            if (notFound(request, target)) return;

            var duration = parseDuration(request.duration);
            if (invalidDuration(request, duration)) return;

            Admins.kick(target, request.admin, duration.toMillis(), request.reason);
            Socket.respond(request, EmbedResponse.success("Player Kicked")
                    .withField("Name:", target.plainName())
                    .withField("Duration:", Bundle.formatDuration(duration))
                    .withField("Reason:", request.reason));
        });

        Socket.on(PardonRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var info = Find.playerInfo(request.player);
            if (notFound(request, info) || notKicked(request, info)) return;

            info.lastKicked = 0L;
            netServer.admins.kickedIPs.remove(info.lastIP);
            netServer.admins.dosBlacklist.remove(info.lastIP);

            Socket.respond(request, EmbedResponse.success("Player Pardoned").withField("Name:", info.plainLastName()));
        });

        Socket.on(BanRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var info = Find.playerInfo(request.player);
            if (notFound(request, info)) return;

            var duration = parseDuration(request.duration);
            if (invalidDuration(request, duration)) return;

            Admins.ban(info, request.admin, duration.toMillis(), request.reason);
            Socket.respond(request, EmbedResponse.success("Player Banned")
                    .withField("Name:", info.plainLastName())
                    .withField("Duration:", Bundle.formatDuration(duration))
                    .withField("Reason:", request.reason));
        });

        Socket.on(UnbanRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var ban = Database.removeBan(request.player);
            if (notBanned(request, ban)) return;

            Socket.respond(request, EmbedResponse.success("Player Unbanned").withField("Name:", ban.player));
        });
    }

    public record DiscordMessageEvent(String server, String role, String color, String name, String message) {
        public DiscordMessageEvent(String server, String name, String message) {
            this(server, null, null, name, message);
        }
    }

    public record ServerMessageEvent(String server, String name, String message) {
    }

    public record ServerMessageEmbedEvent(String server, String title, Color color) {
    }

    public record BanSyncEvent(String server, Ban ban) {
    }

    public record AdminRequestEvent(String server, PlayerData data) {
    }

    public record AdminRequestConfirmEvent(String server, String uuid) {
    }

    public record AdminRequestDenyEvent(String server, String uuid) {
    }

    public record SetRankSyncEvent(String uuid, Rank rank) {
    }

    @AllArgsConstructor
    public static class ListRequest extends Request<ListResponse> {
        public final String type, server;
        public final int page;
    }

    @AllArgsConstructor
    public static class ListResponse extends Response {
        public final String content;
        public final int page, pages, total;
    }

    @AllArgsConstructor
    public static class StatusRequest extends Request<EmbedResponse> {
        public final String server;
    }

    @AllArgsConstructor
    public static class ExitRequest extends Request<EmbedResponse> {
        public final String server;
    }

    @AllArgsConstructor
    public static class ArtvRequest extends Request<EmbedResponse> {
        public final String server, map, admin;
    }

    @AllArgsConstructor
    public static class MapRequest extends Request<EmbedResponse> {
        public final String server, map;
    }

    @AllArgsConstructor
    public static class UploadMapRequest extends Request<EmbedResponse> {
        public final String server;
        public final Fi file;
    }

    @AllArgsConstructor
    public static class RemoveMapRequest extends Request<EmbedResponse> {
        public final String server, map;
    }

    @AllArgsConstructor
    public static class KickRequest extends Request<EmbedResponse> {
        public final String server, player, duration, reason, admin;
    }

    @AllArgsConstructor
    public static class PardonRequest extends Request<EmbedResponse> {
        public final String server, player;
    }

    @AllArgsConstructor
    public static class BanRequest extends Request<EmbedResponse> {
        public final String server, player, duration, reason, admin;
    }

    @AllArgsConstructor
    public static class UnbanRequest extends Request<EmbedResponse> {
        public final String server, player;
    }

    @RequiredArgsConstructor
    public static class EmbedResponse extends Response {
        public final Color color;
        public final String title;
        public final Seq<Field> fields = new Seq<>();

        public @Nullable String content;
        public @Nullable String footer;
        public @Nullable Fi file;

        public static EmbedResponse success(String title) {
            return new EmbedResponse(MEDIUM_SEA_GREEN, title);
        }

        public static EmbedResponse error(String title) {
            return new EmbedResponse(CINNABAR, title);
        }

        public EmbedResponse withField(String name, String value) {
            this.fields.add(Field.of(name, value, false));
            return this;
        }

        public EmbedResponse withContent(String content, Object... args) {
            this.content = Strings.format(content, args);
            return this;
        }

        public EmbedResponse withFooter(String footer, Object... args) {
            this.footer = Strings.format(footer, args);
            ;
            return this;
        }

        public EmbedResponse withFile(Fi file) {
            this.file = file;
            return this;
        }
    }
}