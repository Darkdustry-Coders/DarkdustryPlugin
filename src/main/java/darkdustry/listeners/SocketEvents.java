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
import darkdustry.features.Authme;
import darkdustry.utils.Find;
import discord4j.core.spec.EmbedCreateFields.Footer;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import mindustry.gen.Groups;
import mindustry.io.MapIO;
import mindustry.net.Packets.KickReason;
import useful.Bundle;

import static arc.Core.*;
import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.*;
import static darkdustry.discord.DiscordConfig.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static discord4j.rest.util.Color.*;
import static mindustry.Vars.*;

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

            Socket.on(BanSendEvent.class, event -> Authme.sendBan(event.server, event.ban));
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

        Socket.on(AdminRequestConfirmEvent.class, event -> {
            if (event.server.equals(config.mode.name()))
                Authme.confirm(event.uuid);
        });

        Socket.on(AdminRequestDenyEvent.class, event -> {
            if (event.server.equals(config.mode.name()))
                Authme.deny(event.uuid);
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
                Socket.respond(request, new StatusResponse(
                        state.isPlaying(),
                        state.map.plainName(),
                        Groups.player.size(),
                        Groups.unit.size(),
                        state.wave,
                        graphics.getFramesPerSecond(),
                        app.getJavaHeap()
                ));
        });

        Socket.on(ExitRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            netServer.kickAll(KickReason.serverRestarting);
            app.exit();

            Socket.respond(request, new ExitResponse());
        });

        Socket.on(ArtvRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var map = request.mapName == null ? maps.getNextMap(state.rules.mode(), state.map) : Find.map(request.mapName);
            if (notFound(request, map)) return;

            Bundle.send("commands.artv.info", request.adminName);
            reloadWorld(() -> world.loadMap(map));

            Socket.respond(request, EmbedResponse.success("Map Changed", "**Name:** @", map.plainName()));
        });

        Socket.on(MapRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var map = Find.map(request.mapName);
            if (notFound(request, map)) return;

            Socket.respond(request, EmbedResponse.success(map.plainName(), "**Author:** @\n**Description:** @", map.plainAuthor(), map.plainDescription())
                    .withFooter(map.width + "x" + map.height)
                    .withFile(map.file));
        });

        Socket.on(UploadMapRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var file = customMapDirectory.child(request.file.name());
            file.writeBytes(request.file.readBytes());

            try {
                var map = MapIO.createMap(file, true);
                maps.reload();

                Socket.respond(request, EmbedResponse.success("Map Uploaded", "**Name:** @\n**File:** @", map.plainName(), file.name()));
            } catch (Exception error) {
                file.delete();
                Socket.respond(request, EmbedResponse.error("Invalid Map", "**@** is not a valid map.", file.name()));
            }
        });

        Socket.on(RemoveMapRequest.class, request -> {
            if (!request.server.equals(config.mode.name())) return;

            var map = Find.map(request.mapName);
            if (notFound(request, map) || notRemoved(request, map)) return;

            maps.removeMap(map);
            maps.reload();

            Socket.respond(request, EmbedResponse.success("Map Removed", "**Name:** @\n**File:** @", map.name(), map.file.name()));
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

    public record BanSendEvent(String server, Ban ban) {
    }

    public record AdminRequestEvent(String server, PlayerData data) {
    }

    public record AdminRequestConfirmEvent(String server, String uuid) {
    }

    public record AdminRequestDenyEvent(String server, String uuid) {
    }

    public static class ListRequest extends Request<ListResponse> {
        public final String type, server;
        public final int page;

        public ListRequest(String type, String server, int page) {
            this.server = server;
            this.type = type;
            this.page = page;
        }
    }

    public static class ListResponse extends Response {
        public final String content;
        public final int page, pages, total;

        public ListResponse(String content, int page, int pages, int total) {
            this.content = content;
            this.page = page;
            this.pages = pages;
            this.total = total;
        }
    }

    public static class StatusRequest extends Request<StatusResponse> {
        public final String server;

        public StatusRequest(String server) {
            this.server = server;
        }
    }

    public static class StatusResponse extends Response {
        public final boolean playing;
        public final String mapName;
        public final int players, units, wave, tps;
        public final long javaHeap;

        public StatusResponse(boolean playing, String mapName, int players, int units, int wave, int tps, long javaHeap) {
            this.playing = playing;
            this.mapName = mapName;
            this.players = players;
            this.units = units;
            this.wave = wave;
            this.tps = tps;
            this.javaHeap = javaHeap;
        }
    }

    public static class ExitRequest extends Request<ExitResponse> {
        public final String server;

        public ExitRequest(String server) {
            this.server = server;
        }
    }

    public static class ExitResponse extends Response {
    }

    public static class ArtvRequest extends Request<EmbedResponse> {
        public final String server, mapName, adminName;

        public ArtvRequest(String server, String mapName, String adminName) {
            this.server = server;
            this.mapName = mapName;
            this.adminName = adminName;
        }
    }

    public static class MapRequest extends Request<EmbedResponse> {
        public final String server, mapName;

        public MapRequest(String server, String mapName) {
            this.server = server;
            this.mapName = mapName;
        }
    }

    public static class UploadMapRequest extends Request<EmbedResponse> {
        public final String server;
        public final Fi file;

        public UploadMapRequest(String server, Fi file) {
            this.server = server;
            this.file = file;
        }
    }

    public static class RemoveMapRequest extends Request<EmbedResponse> {
        public final String server, mapName;

        public RemoveMapRequest(String server, String mapName) {
            this.server = server;
            this.mapName = mapName;
        }
    }

    public static class EmbedResponse extends Response {
        public final Color color;
        public final String title;
        public final String content;
        public @Nullable Footer footer;
        public @Nullable Fi file;

        public EmbedResponse(Color color, String title, String content) {
            this.color = color;
            this.title = title;
            this.content = content;
        }

        public static EmbedResponse success(String title, String content, Object... args) {
            return new EmbedResponse(MEDIUM_SEA_GREEN, title, format(content, args));
        }

        public static EmbedResponse info(String title, String content, Object... args) {
            return new EmbedResponse(SUMMER_SKY, title, format(content, args));
        }

        public static EmbedResponse error(String title, String content, Object... args) {
            return new EmbedResponse(CINNABAR, title, format(content, args));
        }

        public EmbedResponse withFooter(String footer) {
            this.footer = Footer.of(footer, null);
            return this;
        }

        public EmbedResponse withFile(Fi file) {
            this.file = file;
            return this;
        }
    }
}