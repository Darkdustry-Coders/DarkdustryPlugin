package pandorum.commands.discord;

import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.Context;

import java.awt.*;

import static mindustry.Vars.state;
import static pandorum.PluginVars.mapPlayTime;
import static pandorum.PluginVars.serverUpTime;
import static pandorum.util.Utils.formatDuration;
import static pandorum.util.Utils.stripAll;

public class StatusCommand {
    public static void run(final String[] args, final Context context) {
        if (state.isMenu()) {
            context.err(":x: The server is down.", "Why.");
            return;
        }

        context.sendEmbed(new EmbedBuilder()
                .setColor(Color.green)
                .setTitle(Strings.format(":desktop: @", stripAll(Config.name.string())))
                .addField("Players:", String.valueOf(Groups.player.size()), true)
                .addField("Map:", Strings.stripColors(state.map.name()), true)
                .addField("Wave:", String.valueOf(state.wave), true)
                .addField("Next wave in:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                .addField("Server Uptime:", formatDuration(serverUpTime * 1000L), true)
                .addField("Map Playtime:", formatDuration(mapPlayTime * 1000L), true)
                .build());
    }
}
