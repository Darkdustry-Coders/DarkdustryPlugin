// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package rewrite;

import arc.util.CommandHandler;
import mindustry.core.Version;
import mindustry.mod.Plugin;
import pandorum.components.Bundle;
import rewrite.commands.ClientCommands;
import rewrite.commands.DiscordCommands;
import rewrite.commands.ServerCommands;

public class DarkdustryPlugin extends Plugin {
    
    @Override
    public void init() {
        Version.build = -1;
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        for (ClientCommands command : ClientCommands.values()) handler.register(command.name(), Bundle.get(command.params), command.desc, command);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        for (ServerCommands command : ServerCommands.values()) handler.register(command.name(), Bundle.get(command.params), command.desc, command);
    }

    public void registerDiscordCommands(CommandHandler handler) {
        for (DiscordCommands command : DiscordCommands.values()) handler.register(command.name(), command.params, command.desc, command);
    }
}
