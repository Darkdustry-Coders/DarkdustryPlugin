package pandorum.commands.discord;

import mindustry.gen.Player;
import net.dv8tion.jda.api.entities.Message;

import static pandorum.utils.Search.findPlayer;

public class LinkCommand {
    public static void run(final String[] args, final Message message) {
        Player target = findPlayer(args[0]);
    }
}
