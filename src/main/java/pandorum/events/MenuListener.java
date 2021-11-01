package pandorum.events;

import arc.Events;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;

public class MenuListener {
    public static int welcomeMenu,
            despwMenu,
            artvMenu,
            infoMenu;

    public static void init() {
        // Приветственное сообщение (0)
        welcomeMenu = Menus.registerMenu((player, option) -> {
            if (option == 1) {
                PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
                    playerInfo.hellomsg = false;
                    playerInfo.save();
                    bundled(player, "events.hellomsg.disabled");
                });
            }
        });

        // Команда /despw (1)
        despwMenu = Menus.registerMenu((player, option) -> {
            if (option == 1) return;

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

            bundled(player, "commands.admin.despw.success");

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.errorColor)
                    .setTitle("Юниты убиты.")
                    .addField("Админ: ", Strings.stripColors(player.name), false);

            BotHandler.botChannel.sendMessage(embed).join();
        });

        // Команда /artv (2)
        artvMenu = Menus.registerMenu((player, option) -> {
            if (option == 0) {
                Events.fire(new GameOverEvent(Team.crux));
                sendToChat("commands.admin.artv.info");

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(BotMain.errorColor)
                        .setTitle("Игра принудительно завершена.")
                        .addField("Админ: ", Strings.stripColors(player.name), false);

                BotHandler.botChannel.sendMessage(embed).join();
            }
        });

        //Информация о игроке (3)
        infoMenu = Menus.registerMenu((player, option) -> {});
    }
}
