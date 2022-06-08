package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.net.Administration.Config;
import pandorum.discord.Context;

import static pandorum.PluginVars.serverIp;
import static pandorum.util.StringUtils.stripAll;

public class IpCommand implements CommandRunner<Context> {
    public void accept(String[] args, Context context) {
        context.info(":desktop: " + stripAll(Config.name.string()), "IP: @:@", serverIp, Config.port.num());
    }
}
