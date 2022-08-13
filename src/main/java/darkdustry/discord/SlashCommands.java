package darkdustry.discord;

import arc.Events;
import arc.func.Cons;
import arc.struct.ObjectMap;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import org.jetbrains.annotations.NotNull;
import darkdustry.components.MapParser;
import darkdustry.components.Config.Gamemode;

import java.awt.Color;

import static arc.Core.*;
import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Utils.*;

public class SlashCommands extends ListenerAdapter {

    public static final ObjectMap<String, Cons<SlashContext>> commands = new ObjectMap<>();

    public static void load() {

        registerCommand("status", "Посмотреть статус сервера.", context -> {
            // if (isMenu(context)) return;
            context.event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.green)
                    .setTitle(":desktop: " + stripAll(Config.serverName.string()))
                    .addField("Игроков:", String.valueOf(Groups.player.size()), true)
                    .addField("Карта:", state.map.name(), true)
                    .addField("Волна:", String.valueOf(state.wave), true)
                    .addField("TPS:", String.valueOf(graphics.getFramesPerSecond()), true)
                    .addField("До следующей волны:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                    .setImage("attachment://minimap.png").build()).addFile(MapParser.parseTiles(world.tiles), "minimap.png").queue();
        }).queue();

        if (config.mode == Gamemode.hexed) return;


    }

    public static CommandCreateAction registerCommand(String name, String description, Cons<SlashContext> cons) {
        commands.put(name, cons);
        return botGuild.upsertCommand(name, description);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        commands.get(event.getName()).get(new SlashContext(event));
    }
}
