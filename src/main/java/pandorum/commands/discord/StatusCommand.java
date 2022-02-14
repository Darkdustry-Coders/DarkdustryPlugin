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
            context.err(":connection_refused: Сервер оффлайн.", ":thonk: Почему.");
            return;
        }

        context.sendEmbed(new EmbedBuilder()
                .setColor(Color.green)
                .setTitle(Strings.format(":desktop: @", stripAll(Config.name.string())))
                .addField("Игроков:", String.valueOf(Groups.player.size()), true)
                .addField("Карта:", Strings.stripColors(state.map.name()), true)
                .addField("Волна:", String.valueOf(state.wave), true)
                .addField("До следующей волны:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                .addField("Сервер онлайн уже:", formatDuration(serverUpTime * 1000L), true)
                .addField("Время игры на карте:", formatDuration(mapPlayTime * 1000L), true)
                .build());
    }
}
