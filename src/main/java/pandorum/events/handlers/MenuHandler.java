package pandorum.events.handlers;

import arc.Events;
import com.mongodb.BasicDBObject;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import pandorum.models.PlayerModel;

import static mindustry.Vars.state;
import static pandorum.Misc.*;

public class MenuHandler {
    public static int welcomeMenu, despwMenu, artvMenu;

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
            switch (option) {
                case 0 -> {
                    Groups.unit.each(Unitc::kill);
                    bundled(player, "commands.admin.despw.success.all");
                }
                case 2 -> {
                    Groups.unit.each(Unitc::isPlayer, Unitc::kill);
                    bundled(player, "commands.admin.despw.success.players");
                }
                case 3 -> {
                    Groups.unit.each(u -> u.team == state.rules.defaultTeam, Unitc::kill);
                    bundled(player, "commands.admin.despw.success.team", colorizedTeam(state.rules.defaultTeam));
                }
                case 4 -> {
                    Groups.unit.each(u -> u.team == state.rules.waveTeam, Unitc::kill);
                    bundled(player, "commands.admin.despw.success.team", colorizedTeam(state.rules.waveTeam));
                }
                case 5 -> {
                    if (player.unit() != null) player.unit().kill();
                    bundled(player, "commands.admin.despw.success.suicide");
                }
            }
        });

        artvMenu = Menus.registerMenu((player, option) -> {
            if (option == 0) {
                Events.fire(new GameOverEvent(state.rules.waveTeam));
                sendToChat("commands.admin.artv.info");
            }
        });
    }
}
