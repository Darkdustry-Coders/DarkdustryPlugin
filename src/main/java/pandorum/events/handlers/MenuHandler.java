package pandorum.events.handlers;

import arc.Events;
import com.mongodb.BasicDBObject;
import mindustry.Vars;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;

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
        });

        artvMenu = Menus.registerMenu((player, option) -> {
            if (option == 0) {
                Events.fire(new GameOverEvent(Vars.state.rules.waveTeam));
                sendToChat("commands.admin.artv.info");
            }
        });
    }
}
