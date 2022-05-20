package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Groups;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.components.MapParser;
import pandorum.discord.Context;

import java.awt.*;

import static mindustry.Vars.state;
import static mindustry.Vars.world;
import static pandorum.PluginVars.mapPlayTime;
import static pandorum.PluginVars.serverUpTime;
import static pandorum.util.Utils.formatDuration;
import static pandorum.util.Utils.stripAll;

public class StatusCommand implements CommandRunner<Context> {
    public void accept(String[] args, Context context) {
        if (state.isMenu()) {
            context.err(":gear: Сервер отключен.", ":thinking: Почему?");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.green)
                .setTitle(":desktop: " + stripAll(Config.name.string()))
                .addField("Игроков:", String.valueOf(Groups.player.size()), true)
                .addField("Карта:", state.map.name(), true)
                .addField("Волна:", String.valueOf(state.wave), true)
                .addField("До следующей волны:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                .addField("Сервер онлайн уже:", formatDuration(serverUpTime * 1000L), true)
                .addField("Время игры на карте:", formatDuration(mapPlayTime * 1000L), true);

        byte[] image = MapParser.parseTiles(world.tiles);
        if (image.length > 0) {
            embed.setImage("attachment://minimap.png");
        }

        context.channel.sendMessageEmbeds(embed.build()).addFile(image, "minimap.png").queue();
    }
}
