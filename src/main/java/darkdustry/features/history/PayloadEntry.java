package darkdustry.features.history;

import darkdustry.database.Database;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import useful.Bundle;

import javax.annotation.Nullable;

import static mindustry.Vars.content;
import static mindustry.Vars.netServer;

public class PayloadEntry implements HistoryEntry {
    private final boolean pickup;
    private final short blockId;
    private final @Nullable String uuid;
    private final @Nullable UnitType unit;

    public PayloadEntry(EventType.PayloadDropEvent event) {
        pickup = true;
        blockId = (short) event.build.id;
        unit = event.carrier.isPlayer() ? null : event.carrier.type;
        uuid = unit == null ? event.carrier.getPlayer().uuid() : null;
    }

    public PayloadEntry(EventType.PickupEvent event) {
        pickup = false;
        blockId = (short) event.build.id;
        unit = event.carrier.isPlayer() ? null : event.carrier.type;
        uuid = unit == null ? event.carrier.getPlayer().uuid() : null;
    }

    @Override
    public String getMessage(Player player) {
        var block = content.block(blockId);

        if (uuid == null) {
            assert unit != null;
            return pickup
                    ? Bundle.format("history.payload.pickup.unit", player, unit.emoji(), unit.name, block.emoji(), block.name)
                    : Bundle.format("history.payload.deploy.unit", player, unit.emoji(), unit.name, block.emoji(), block.name);
        }
        else {
            var info = netServer.admins.getInfo(uuid);
            var data = Database.getPlayerDataOrCreate(uuid);
            return pickup
                    ? Bundle.format("history.payload.pickup.player", player, info.lastName, "" + data.id, block.emoji(), block.name)
                    : Bundle.format("history.payload.deploy.player", player, info.lastName, "" + data.id, block.emoji(), block.name);
        }
    }
}
