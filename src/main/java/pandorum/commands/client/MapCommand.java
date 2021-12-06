package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.gamemodes.RequireSimpleGamemode;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;

public class MapCommand {
    @RequireSimpleGamemode
    @ClientCommand(name = "map", args = "", description = "Information about current map.", admin = false)
    public static void run(final String[] args, final Player player) {
        bundled(player, "commands.mapname", state.map.name(), state.map.author());
    }
}
