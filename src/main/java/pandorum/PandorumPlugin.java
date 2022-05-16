package pandorum;

import arc.Core;
import arc.util.CommandHandler;
import mindustry.mod.Plugin;

import static pandorum.PluginVars.clientCommands;
import static pandorum.PluginVars.serverCommands;

public class PandorumPlugin extends Plugin {

    @Override
    public void init() {
        Core.app.addListener(new Main());
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        clientCommands = handler;
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        serverCommands = handler;
    }
}