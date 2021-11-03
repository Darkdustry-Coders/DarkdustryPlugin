package pandorum.events;

import arc.Events;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.Vars;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import pandorum.comp.Bundle;
import pandorum.comp.Ranks;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.models.PlayerModel;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.*;
import static pandorum.Misc.findLocale;

public class MenuListener {
    public static int welcomeMenu,
            despwMenu,
            artvMenu,
            infoMenu;

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
            if (option == 1) return;

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

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(BotMain.errorColor)
                    .setTitle("Юниты убиты.")
                    .addField("Админ: ", Strings.stripColors(player.name), false);

            BotHandler.botChannel.sendMessage(embed).join();
        });

        artvMenu = Menus.registerMenu((player, option) -> {
            if (option == 0) {
                Events.fire(new GameOverEvent(Vars.state.rules.waveTeam));
                sendToChat("commands.admin.artv.info");

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(BotMain.errorColor)
                        .setTitle("Игра принудительно завершена.")
                        .addField("Админ: ", Strings.stripColors(player.name), false);

                BotHandler.botChannel.sendMessage(embed).join();
            }
        });

        infoMenu = Menus.registerMenu((player, option) -> {
            if (option == 1) {
                String[][] options = {{Bundle.format("events.menu.close", findLocale(player.locale))}};
                PlayerModel.find(
                        new BasicDBObject("UUID", player.uuid()),
                        playerInfo -> Call.menu(
                                player.con,
                                infoMenu,
                                Bundle.format(
                                        "commands.info.header",
                                        findLocale(player.locale),
                                        player.coloredName()
                                ),
                                Bundle.format(
                                        "commands.info.content",
                                        findLocale(player.locale),
                                        Ranks.ranks.get(playerInfo.rank).tag,
                                        Ranks.ranks.get(playerInfo.rank).name,
                                        TimeUnit.MILLISECONDS.toMinutes(playerInfo.playTime),
                                        playerInfo.buildingsBuilt,
                                        playerInfo.buildingsDeconstructed,
                                        playerInfo.maxWave,
                                        playerInfo.gamesPlayed,
                                        playerInfo.hellomsg ? "on" : "off",
                                        playerInfo.alerts ? "on" : "off",
                                        playerInfo.locale
                                ),
                                options
                        )
                );
            }
        });
    }
}
