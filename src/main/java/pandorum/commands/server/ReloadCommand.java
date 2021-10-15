package pandorum.commands.server;

import arc.util.Log;
import pandorum.PandorumPlugin;
import pandorum.comp.Config;

import static mindustry.Vars.dataDirectory;

public class ReloadCommand {
    public static void run(final String[] args) {
        PandorumPlugin.config = PandorumPlugin.gson.fromJson(dataDirectory.child("config.json").readString(), Config.class);
        Log.info("Перезагружено.");
    }
}
