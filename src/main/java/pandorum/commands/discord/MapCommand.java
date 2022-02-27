package pandorum.commands.discord;

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

        MapModel.find(map, mapModel -> {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.yellow)
                    .setTitle(":map: " + map.name())
                    .setFooter(map.width + "x" + map.height)
                    .setImage("attachment://map.png")
                    .addField(":mailbox_with_mail: Рейтинг:", ":green_circle: " + mapModel.upVotes + " | " + mapModel.downVotes + " :red_circle:", true)
                    .addField(":clock1: Время игры:", Utils.formatDuration(mapModel.playTime * 1000L), true)
                    .addField(":100: Лучшая волна:", String.valueOf(mapModel.bestWave), true)
                    .addField(":checkered_flag: Сыграно игр:", String.valueOf(mapModel.gamesPlayed), true);

            if (!map.author().equalsIgnoreCase("unknown")) {
                embed.setAuthor(map.author());
            }

            if (!map.description().equalsIgnoreCase("unknown")) {
                embed.setDescription(map.description());
            }

            context.channel.sendMessageEmbeds(embed.build()).addFile(map.file.file()).addFile(MapParser.parseMap(map), "map.png").queue();
        });
    }
}
