package pandorum.features.antigrief;

import arc.math.geom.Position;
import mindustry.game.EventType.BuildSelectEvent;
import mindustry.game.EventType.DepositEvent;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.world.Block;
import pandorum.components.Icons;
import pandorum.mongo.models.PlayerModel;
import pandorum.util.Utils;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;
import static pandorum.util.Utils.bundled;

public class Alerts {

    public static boolean enabled() {
        return defaultModes.contains(config.mode);
    }

    public static void buildAlert(BuildSelectEvent event) {
        if (!enabled() || event.breaking || event.builder == null || event.builder.buildPlan() == null) return;

        if (isDangerousBuild(event.builder.buildPlan().block) && isNearCore(event.team, event.tile) && interval.get(1, alertsTimer)) {
            String name = Utils.notNullElse(event.builder.getControllerName(), Icons.get(event.builder.type.name));
            Utils.eachPlayerInTeam(event.team, player -> PlayerModel.find(player, playerModel -> {
                if (playerModel.alerts) {
                    bundled(player, "events.alert", name, Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
                }
            }));
        }
    }

    public static void depositAlert(DepositEvent event) {
        if (!enabled() || !state.rules.reactorExplosions) return;

        if (isDangerousDeposit(event.tile.block, event.item) && isNearCore(event.player.team(), event.tile)) {
            String name = event.player.coloredName();
            Utils.eachPlayerInTeam(event.player.team(), player -> PlayerModel.find(player, playerModel -> {
                if (playerModel.alerts) {
                    bundled(player, "events.withdraw-thorium", name, Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
                }
            }));
        }
    }

    public static boolean isDangerousBuild(Block block) {
        return dangerousBuildBlocks.containsKey(block) && dangerousBuildBlocks.get(block).get();
    }

    public static boolean isDangerousDeposit(Block block, Item item) {
        return dangerousDepositBlocks.containsKey(block) && dangerousDepositBlocks.get(block) == item;
    }

    public static boolean isNearCore(Team team, Position position) {
        return team.cores().contains(core -> core.dst(position) < alertsDistance);
    }
}
