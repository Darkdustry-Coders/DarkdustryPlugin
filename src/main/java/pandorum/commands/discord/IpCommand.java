package pandorum.commands.discord;

import discord4j.core.object.entity.Message;
import mindustry.net.Administration;

import static pandorum.discord.BotHandler.info;

public class IpCommand {
    public static void run(final String[] args, final Message message) {
        info(message.getChannel().block(), "IP адрес сервера", "darkdustry.ml:@", Administration.Config.port.num());
    }
}
