package pandorum.commands.discord;

import mindustry.maps.Map;
import pandorum.discord.Context;

import static mindustry.Vars.maps;
import static pandorum.util.Search.findMap;

public class RemoveMapCommand {
    public static void run(final String[] args, final Context context) {
        Map map = findMap(args[0]);
        if (map == null) {
            context.err(":mag: Map not found.", "Check if the name is correct.");
            return;
        }

        maps.removeMap(map);
        maps.reload();
        context.success(":dagger: Map removed from the server.");
    }
}
