package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.util.Utils.bundled;

public class DiscordLinkCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        bundled(player, "commands.discord.link", discordServerUrl);
    }
}
