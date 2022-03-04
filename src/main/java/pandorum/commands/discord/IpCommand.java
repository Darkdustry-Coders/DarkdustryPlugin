package pandorum.commands.discord;

import mindustry.net.Administration.Config;
import pandorum.discord.Context;

import static pandorum.util.Utils.stripAll;

public class IpCommand {
    public static void run(final String[] args, final Context context) {
        context.info(":desktop: " + stripAll(Config.name.string()), "IP: darkdustry.ml:@", Config.port.num());
    }
}
