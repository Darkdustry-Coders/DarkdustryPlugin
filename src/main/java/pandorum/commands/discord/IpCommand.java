package pandorum.commands.discord;

import mindustry.net.Administration;
import pandorum.discord.Context;

public class IpCommand {
    public static void run(final String[] args, final Context context) {
        context.info(":desktop: IP сервера:", "darkdustry.ml:@", Administration.Config.port.num());
    }
}
