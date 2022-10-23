package darkdustry.commands;

import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.*;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;

import static arc.Core.*;
import static darkdustry.PluginVars.config;
import static darkdustry.components.Config.Gamemode.hexed;
import static darkdustry.components.MapParser.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.discord.Bot.Palette.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.stripAll;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.serverName;
import static net.dv8tion.jda.api.Permission.VIEW_AUDIT_LOGS;
import static net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;

public class DiscordCommands {

    public static final ObjectMap<String, Cons<SlashCommandInteractionEvent>> commands = new ObjectMap<>();
    public static final Seq<SlashCommandData> datas = new Seq<>();

    public static void load() {
        register("status", "Display server status.", event -> {
            if (notHosting(event)) return;

            var embed = embed(info, stripAll(serverName.string()),
                    """
                            Players: @
                            Map: @
                            Wave: @
                            TPS: @
                            RAM consumption: @ MB
                            """, Groups.player.size(), state.map.name(), state.wave,
                    graphics.getFramesPerSecond(), app.getJavaHeap() / 1024 / 1024)
                    .setImage("attachment://minimap.png");

            event.replyEmbeds(embed.build()).queue(hook ->
                    hook.editOriginalAttachments(fromData(renderMinimap(), "minimap.png")).queue());
        });

        register("maps", "List of all maps.", PageIterator::maps)
                .addOption(INTEGER, "page", "Maps list page.");

        register("players", "List of all players.", PageIterator::players)
                .addOption(INTEGER, "page", "Players list page.");

        register("restart", "Restart the server.", event -> event.replyEmbeds(embed(info, ":arrows_counterclockwise:  Сервер перезапускается...").build()).queue(hook -> DarkdustryPlugin.exit()))
                .setDefaultPermissions(DISABLED);

        if (config.mode == hexed) return;

        register("map", "Get a map from the server.", event -> {
            var map = Find.map(event.getOption("map").getAsString());
            if (notFound(event, map)) return;

            var embed = embed(info, map.name(), map.description())
                    .setAuthor(map.author())
                    .setFooter(map.width + "x" + map.height)
                    .setImage("attachment://map.png");

            event.replyEmbeds(embed.build()).queue(hook ->
                    hook.editOriginalAttachments(fromData(map.file.file()), fromData(renderMap(map), "map.png")).queue());
        }).addOption(STRING, "map", "Name of the map.", true);

        register("addmap", "Add a map to the server.", event -> {
            if (notMap(event)) return;

            var attachment = event.getOption("map").getAsAttachment();
            attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
                if (notMap(event, new Fi(file))) return;

                maps.reload();

                event.replyEmbeds(embed(success, ":map: Map uploaded to the server.", "Map file: @", file.getName()).build()).queue();
            });
        }).setDefaultPermissions(enabledFor(VIEW_AUDIT_LOGS))
                .addOption(ATTACHMENT, "map", "Map file to be uploaded to the server.", true);

        register("removemap", "Remove a map from the server.", event -> {
            var map = Find.map(event.getOption("map").getAsString());
            if (notFound(event, map)) return;

            maps.removeMap(map);
            maps.reload();

            event.replyEmbeds(embed(success, ":map: Map removed from the server.", "Map name: @", map.name()).build()).queue();
        }).setDefaultPermissions(enabledFor(VIEW_AUDIT_LOGS))
                .addOption(STRING, "map", "Name of the map to be removed from the server.", true);

        register("gameover", "Force a gameover.", event -> {
            if (notHosting(event)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            event.replyEmbeds(embed(success, ":arrows_counterclockwise: Game over.").build()).queue();
        }).setDefaultPermissions(enabledFor(VIEW_AUDIT_LOGS));
    }

    private static SlashCommandData register(String name, String description, Cons<SlashCommandInteractionEvent> cons) {
        commands.put(name, cons);
        return datas.add(Commands.slash(name, description)).peek();
    }
}