package pandorum.events;

import arc.Events;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;

public class MenuListener {
    public static void init() {
        // Приветственное сообщение (0)
        Menus.registerMenu((player, option) -> {
            if (option == 1) {
                PlayerModel.find(
                    PlayerModel.class,
                    new BasicDBObject("UUID", player.uuid()),
                    playerInfo -> playerInfo.hellomsg = false
                );

                bundled(player, "events.hellomsg.disabled");
            }
        });

        // Команда /despw (1)
        Menus.registerMenu((player, option) -> {
            if (option == 1) return;

            int unitCount = Groups.unit.size();
            switch (option) {
                case 0 -> Groups.unit.each(Unitc::kill);
                case 2 -> Groups.unit.each(Unitc::isPlayer, Unitc::kill);
                case 3 -> Groups.unit.each(u -> u.team == Team.sharded, Unitc::kill);
                case 4 -> Groups.unit.each(u -> u.team == Team.crux, Unitc::kill);
                case 5 -> {
                    player.clearUnit();
                    bundled(player, "commands.admin.despw.suicide");
                    return;
                }
            }

            int amount = unitCount - Groups.unit.size();
            bundled(player, "commands.admin.despw.success", amount);

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.errorColor)
                    .setTitle("Юниты убиты.")
                    .addField("Админ: ", Strings.stripColors(player.name), false)
                    .addField("Количество: ", Integer.toString(amount), false);

            BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
        });

        // Команда /artv (2)
        Menus.registerMenu((player, option) -> {
            if (option == 0) {
                Events.fire(new EventType.GameOverEvent(Team.crux));
                sendToChat("commands.admin.artv.info");

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(BotMain.errorColor)
                        .setTitle("Игра принудительно завершена.")
                        .addField("Админ: ", Strings.stripColors(player.name), false);

                BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
            }
        });

        //Информация о игроке (3)
        Menus.registerMenu((player, option) -> {
            //Пока что не делает ничего
        });
    }
}
