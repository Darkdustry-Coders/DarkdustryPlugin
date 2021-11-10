package pandorum.events.handlers;

import arc.Events;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.Vars;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;

public class MenuHandler {
    public static int welcomeMenu,
            despwMenu,
            artvMenu;

    public static void init() {
        welcomeMenu = Menus.registerMenu((player, option) -> {
            if (option == 1) {
                PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
                    playerInfo.hellomsg = false;
                    playerInfo.save();
                    bundled(player, "events.hellomsg.disabled");
                });
            }
        });

        despwMenu = Menus.registerMenu((player, option) -> {
            if (option == 1 || option == -1) return;

            switch (option) {
                case 0 -> Groups.unit.each(Unitc::kill);
                case 2 -> Groups.unit.each(Unitc::isPlayer, Unitc::kill);
                case 3 -> Groups.unit.each(u -> u.team == Team.sharded, Unitc::kill);
                case 4 -> Groups.unit.each(u -> u.team == Team.crux, Unitc::kill);
                default -> {
                    player.clearUnit();
                    bundled(player, "commands.admin.despw.suicide");
                    return;
                }
            }

            bundled(player, "commands.admin.despw.success");

            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(BotMain.errorColor)
                    .author("Despawn", null, "https://d1nhio0ox7pgb.cloudfront.net/_img/g_collection_png/standard/512x512/knife.png")
                    .title("Все юниты убиты!")
                    .addField("Админом:", Strings.stripColors(player.name), false)
                    .build();

            BotHandler.sendEmbed(embed);
        });

        artvMenu = Menus.registerMenu((player, option) -> {
            if (option == 0) {
                Events.fire(new GameOverEvent(Vars.state.rules.waveTeam));
                sendToChat("commands.admin.artv.info");

                EmbedCreateSpec embed = EmbedCreateSpec.builder()
                        .color(BotMain.errorColor)
                        .author("Gameover", null, "https://w7.pngwing.com/pngs/679/718/png-transparent-globe-world-font-awesome-icon-globe-miscellaneous-map-earth-thumbnail.png")
                        .title("Игрок принудительно завершена!")
                        .addField("Админом:", Strings.stripColors(player.name), false)
                        .build();

                BotHandler.sendEmbed(embed);
            }
        });
    }
}
