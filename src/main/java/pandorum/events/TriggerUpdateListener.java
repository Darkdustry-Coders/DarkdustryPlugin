package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.entities.Activity;
import pandorum.PandorumPlugin;
import pandorum.comp.Ranks;
import pandorum.comp.effects.Effects;
import pandorum.models.PlayerModel;

import static pandorum.discord.BotMain.jda;

public class TriggerUpdateListener {
    public static void update() {
        Groups.player.each(p -> p.unit().moving(), Effects::onMove);
        if (PandorumPlugin.interval.get(1, 60f)) {
            Groups.player.each(player -> Ranks.getRank(player, rank -> player.name(rank.tag + "[#" + player.color.toString().toUpperCase() + "]" + player.getInfo().lastName)));
            Groups.player.each(player -> PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
                playerInfo.playTime += 1000;
                playerInfo.save();
            }));

            if (PandorumPlugin.interval.get(2, 300f)) {
                jda.getPresence().setActivity(Activity.streaming(Groups.player.size() + (Groups.player.size() % 10 == 1 && Groups.player.size() != 11 ? " игрок на сервере." : " игроков на сервере."), null));
            }
        }
    }
}
