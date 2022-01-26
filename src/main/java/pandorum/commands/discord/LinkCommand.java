package pandorum.commands.discord;

import mindustry.gen.Player;
import net.dv8tion.jda.api.entities.Message;

import static pandorum.util.Search.findPlayer;

public class LinkCommand {
    public static void run(final String[] args, final Message message) {
        Player target = findPlayer(args[0]);
        if (target == null) {

        }
    }
}
