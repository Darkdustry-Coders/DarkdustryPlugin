package pandorum.commands.client.player;

import mindustry.gen.Player;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;

public class MapCommand {
    public static void run(final String[] args, final Player player) {
        bundled(player, "commands.mapname", state.map.name(), state.map.author());
    }
}
