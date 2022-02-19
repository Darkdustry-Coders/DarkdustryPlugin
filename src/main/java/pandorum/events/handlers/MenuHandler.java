package pandorum.events.handlers;

import arc.Events;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import pandorum.util.Utils;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class MenuHandler {

    public static int welcomeMenu, despwMenu, artvMenu, mapRateMenu;

    public static void init() {
        welcomeMenu = Menus.registerMenu((player, option) -> {
            if (option == 1) {
                playersInfo.find(player, playerModel -> {
                    playerModel.welcomeMessage = false;
                    playerModel.save();
                    Utils.bundled(player, "events.welcome.disabled");
                });
            }
        });

        despwMenu = Menus.registerMenu((player, option) -> {
            switch (option) {
                case 0 -> {
                    Groups.unit.each(Unitc::kill);
                    Utils.bundled(player, "commands.admin.despw.success.all");
                }
                case 2 -> {
                    Groups.unit.each(Unitc::isPlayer, Unitc::kill);
                    Utils.bundled(player, "commands.admin.despw.success.players");
                }
                case 3 -> {
                    Groups.unit.each(unit -> unit.team == state.rules.defaultTeam, Unitc::kill);
                    Utils.bundled(player, "commands.admin.despw.success.team", Utils.colorizedTeam(state.rules.defaultTeam));
                }
                case 4 -> {
                    Groups.unit.each(unit -> unit.team == state.rules.waveTeam, Unitc::kill);
                    Utils.bundled(player, "commands.admin.despw.success.team", Utils.colorizedTeam(state.rules.waveTeam));
                }
                case 5 -> {
                    if (!player.dead()) player.unit().kill();
                    Utils.bundled(player, "commands.admin.despw.success.suicide");
                }
            }
        });

        artvMenu = Menus.registerMenu((player, option) -> {
            if (option == 0) {
                Events.fire(new GameOverEvent(state.rules.waveTeam));
                Utils.sendToChat("commands.admin.artv.info", player.coloredName());
            }
        });

        mapRateMenu = Menus.registerMenu((player, option) -> {
            if ((option == 0 || option == 1)) {
                if (mapRateVotes.contains(player.uuid())) {
                    Utils.bundled(player, "commands.map.already-voted");
                    return;
                }

                if (!canVote) {
                    Utils.bundled(player, "commands.can-not-vote");
                    return;
                }

                mapsInfo.find(state.map, mapModel -> {
                    if (option == 0) {
                        mapModel.upVotes++;
                        mapModel.save();
                        mapRateVotes.add(player.uuid());
                        Utils.bundled(player, "commands.map.upvoted");
                    } else {
                        mapModel.downVotes++;
                        mapModel.save();
                        mapRateVotes.add(player.uuid());
                        Utils.bundled(player, "commands.map.downvoted");
                    }
                });
            }
        });
    }
}
