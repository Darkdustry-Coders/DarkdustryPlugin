package pandorum.commands.discord;

import arc.util.Strings;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.database.models.MapModel;
import pandorum.discord.Context;
import pandorum.util.Utils;

import java.awt.*;

import static pandorum.util.Search.findMap;

public class MapCommand {
    public static void run(final String[] args, final Context context) {
        Map map = findMap(args[0]);
        if (map == null) {
            context.err(":mag: Map not found.", "Check if the name is correct.");
            return;
        }

        MapModel.find(map, mapModel -> {
            context.sendEmbedWithFile(new EmbedBuilder()
                    .setColor(Color.yellow)
                    .setAuthor(Strings.stripColors(map.author()))
                    .setTitle(Strings.format(":map: @", Strings.stripColors(map.name())))
                    .setDescription(Strings.stripColors(map.description()))
                    .addField(":mailbox_with_mail: Votes:", Strings.format(":green_circle: @ | @ :red_circle:", mapModel.upVotes, mapModel.downVotes), true)
                    .addField(":clock1: Playtime:", Strings.format("@ минут", Utils.secondsToMinutes(mapModel.playTime)), true)
                    .addField(":100: Best wave:", String.valueOf(mapModel.bestWave), true)
                    .addField(":checkered_flag: Games played:", String.valueOf(mapModel.gamesPlayed), true)
                    .setFooter(Strings.format("@x@", map.width, map.height))
                    .build(), map.file
            );
        });
    }
}
