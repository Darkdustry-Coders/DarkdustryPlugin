package darkdustry.features;

import arc.Events;
import arc.struct.IntSeq;
import mindustry.game.EventType.*;
import mindustry.gen.Call;

import static mindustry.Vars.*;

public class SchemeSize {

    public static IntSeq SSUsers = new IntSeq();

    public static void load() {
        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "AreYouUsingSS", null));
        Events.on(PlayerLeave.class, event -> SSUsers.removeValue(event.player.id));

        netServer.addPacketHandler("IUseSS", (player, args) -> {
            SSUsers.add(player.id);
        });

        netServer.addPacketHandler("GivePlayerDataPlease", (player, args) -> {
            Call.clientPacketReliable(player.con, "ThisIsYourPlayerData", SSUsers.toString(" "));
        });
    }
}
