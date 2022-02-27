package pandorum.commands.discord;

import arc.util.Log;
import arc.util.Strings;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.components.MapParser;
import pandorum.database.models.MapModel;
import pandorum.discord.Context;
import pandorum.util.Utils;

import java.awt.*;

import static pandorum.util.Search.findMap;

public class MapCommand {
    public static void run(final String[] args, final Context context) {
        Map map = findMap(args[0]);
        if (map == null) {
            context.err(":mag: Карта не найдена.", "Проверь, правильно ли введено название.");
            return;
        }

        try {
            MapModel.find(map, mapModel -> {
                byte[] data = MapParser.parseMap(map);
                context.channel.sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.yellow)
                        .setTitle(":map: " + map.name())
                        .setAuthor(map.author())
                        .setDescription(map.description())
                        .setFooter(map.width + "x" + map.height)
                        .setImage("attachment://" + map.name())
                        .addField(":mailbox_with_mail: Рейтинг:", Strings.format(":green_circle: @ | @ :red_circle:", mapModel.upVotes, mapModel.downVotes), true)
                        .addField(":clock1: Время игры:", Utils.formatDuration(mapModel.playTime * 1000L), true)
                        .addField(":100: Лучшая волна:", String.valueOf(mapModel.bestWave), true)
                        .addField(":checkered_flag: Сыграно игр:", String.valueOf(mapModel.gamesPlayed), true)
                        .build()
                ).addFile(map.file.file()).addFile(data, map.name()).queue();
            });
        } catch (Exception e) {
            Log.err(e);
        }
    }
}
