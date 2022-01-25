package pandorum.commands.discord;

import mindustry.net.Administration;
import net.dv8tion.jda.api.entities.Message;

import static pandorum.discord.Bot.info;

public class IpCommand {
    public static void run(final String[] args, final Message message) {
        info(message.getChannel(), "IP адрес сервера:", "darkdustry.ml:@", Administration.Config.port.num());
    }
}
