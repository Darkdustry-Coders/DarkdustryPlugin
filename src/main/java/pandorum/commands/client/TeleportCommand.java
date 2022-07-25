package pandorum.commands.client;

import arc.util.Strings;
import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.Unit;

import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;

public class TeleportCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        if (!Strings.canParseFloat(args[0]) || !Strings.canParseFloat(args[1])) {
            bundled(player, "commands.admin.teleport.incorrect-number-format");
            return;
        }

        float x = Strings.parseFloat(args[0]), y = Strings.parseFloat(args[1]);
        boolean spawnedNyCore = player.unit().spawnedByCore();
        Unit unit = player.unit();

        unit.spawnedByCore(false);
        player.clearUnit();

        unit.set(x, y);
        Call.setPosition(player.con, x, y);
        Call.setCameraPosition(player.con, x, y);

        player.unit(unit);
        unit.spawnedByCore(spawnedNyCore);
    }
}
