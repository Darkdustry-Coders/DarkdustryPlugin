package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.components.MapParser;
import pandorum.discord.Context;

import java.awt.*;

import static pandorum.util.Search.findMap;

public class MapCommand implements CommandRunner<Context> {
    public void accept(String[] args, Context context) {
        Map map = findMap(args[0]);
        if (map == null) {
            context.err(":mag: Карта не найдена.", "Проверь, правильно ли введено название.");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.yellow)
                .setTitle(":map: " + map.name())
                .setFooter(map.width + "x" + map.height)
                .setImage("attachment://map.png");

        if (!map.author().equals("unknown")) embed.setAuthor(map.author());
        if (!map.description().equals("unknown")) embed.setDescription(map.description());

        byte[] image = MapParser.parseMap(map);
        context.channel.sendMessageEmbeds(embed.build()).addFile(map.file.file()).addFile(image, "map.png").queue();
    }
}
