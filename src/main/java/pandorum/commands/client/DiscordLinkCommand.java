package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.PandorumPlugin;
import pandorum.annotations.commands.ClientCommand;

import static pandorum.Misc.bundled;

public class DiscordLinkCommand {
    @ClientCommand(name = "discord", args = "", description = "Get a link to our Discord server.", admin = false)
    public static void run(final String[] args, final Player player) {
        bundled(player, "commands.discord.link", PandorumPlugin.discordServerLink);
    }
}
