package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.net.Administration.Config;
import pandorum.discord.Context;

import static pandorum.util.Utils.stripAll;

public class IpCommand implements CommandRunner<Context> {
    public void accept(String[] args, Context context) {
        context.info(":desktop: " + stripAll(Config.name.string()), "IP: darkdustry.ml:@", Config.port.num());
    }
}
