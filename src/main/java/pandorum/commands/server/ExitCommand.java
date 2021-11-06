package pandorum.commands.server;

import arc.util.Log;
import mindustry.Vars;
import pandorum.discord.BotMain;

public class ExitCommand implements ServerCommand {
    public static void run(final String[] args) {
        Log.info("Выключаю сервер.");
        Vars.net.dispose();
        BotMain.bot.disconnect();
        System.exit(2);
    }
}
