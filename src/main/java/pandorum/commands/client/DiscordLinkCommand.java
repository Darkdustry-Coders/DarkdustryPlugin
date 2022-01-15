package pandorum.commands.client;

import mindustry.gen.Player;

import static pandorum.Misc.bundled;
import static pandorum.PluginVars.discordServerUrl;

public class DiscordLinkCommand {
    public static void run(final String[] args, final Player player) {
        bundled(player, "commands.discord.link", discordServerUrl);
    }
}
