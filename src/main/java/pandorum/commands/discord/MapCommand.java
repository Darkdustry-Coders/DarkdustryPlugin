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

            byte[] image = MapParser.parseMap(map);
            if (image.length > 0) {
                embed.setImage("attachment://map.png");
            }

            context.channel.sendMessageEmbeds(embed.build()).addFile(map.file.file()).addFile(image, "map.png").addFile(new byte[0], "map1.png").queue();
        });
    }
}
