package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.PandorumPlugin;

import static pandorum.Misc.bundled;

public class DiscordLinkCommand {
    public static void run(final String[] args, final Player player) {
        bundled(player, "commands.discord.link", PandorumPlugin.discordServerLink);
    }
}
