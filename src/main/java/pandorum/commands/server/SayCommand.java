package pandorum.commands.server;

import arc.util.Log;
import pandorum.discord.BotHandler;

import static pandorum.Misc.sendToChat;

public class SayCommand implements ServerCommand {
    public static void run(final String[] args) {
        sendToChat("commands.say.chat", args[0]);
        Log.info("Server: &ly@", args[0]);
        BotHandler.text(BotHandler.botChannel, "**@**: @", "Сервер", args[0].replaceAll("https?://|@", " "));
    }
}
