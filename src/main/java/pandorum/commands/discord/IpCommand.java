package pandorum.commands.discord;

import arc.util.CommandHandler.CommandRunner;
import mindustry.net.Administration.Config;
import pandorum.discord.MessageContext;

import static pandorum.PluginVars.serverIp;
import static pandorum.util.Utils.stripAll;

public class IpCommand implements CommandRunner<MessageContext> {
    public void accept(String[] args, MessageContext context) {
        context.info(":desktop: " + stripAll(Config.serverName.string()), "IP: @:@", serverIp, Config.port.num());
    }
}
