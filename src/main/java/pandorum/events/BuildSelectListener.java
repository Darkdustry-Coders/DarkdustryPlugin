package pandorum.events;

import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.PandorumPlugin;
import pandorum.comp.Config;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;

public class BuildSelectListener {
    public static void call(final EventType.BuildSelectEvent event) {
        if (PandorumPlugin.config.mode == Config.Gamemode.hexed || PandorumPlugin.config.mode == Config.Gamemode.hub || PandorumPlugin.config.mode == Config.Gamemode.castle) return;

        if (!event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() && event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)) {
            Player builder = event.builder.getPlayer();

            if (PandorumPlugin.interval.get(0, 750f)) {
                Groups.player.each(p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
                    if (playerInfo.alerts) bundled(p, "events.alert", builder.coloredName(), event.tile.x, event.tile.y);
                }));

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(BotMain.errorColor)
                        .setAuthor("Анти-гриф система")
                        .setTitle("Игрок строит ториевый реактор близко к ядру!")
                        .addField("Никнейм: ", Strings.stripColors(builder.name), false);

                BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
}
