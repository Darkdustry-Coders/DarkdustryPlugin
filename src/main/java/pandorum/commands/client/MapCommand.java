package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.comp.Bundle;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;
import static pandorum.Misc.findLocale;

public class MapCommand {
    public static void run(final String[] args, final Player player) {
        bundled(player, "commands.map", state.map.tags.get("name", Bundle.format("commands.map.unknown", findLocale(player.locale))), state.map.tags.get("author", Bundle.format("commands.map.unknown", findLocale(player.locale))));
    }
}
