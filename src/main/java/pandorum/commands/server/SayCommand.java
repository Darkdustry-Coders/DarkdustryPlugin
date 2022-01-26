package pandorum.commands.server;

import arc.util.Log;

import static pandorum.util.Utils.sendToChat;
import static pandorum.discord.Bot.text;

public class SayCommand {
    public static void run(final String[] args) {
        Log.info("Сервер: &ly@", args[0]);
        sendToChat("commands.say.chat", args[0]);
        text("**Сервер**: @", args[0]);
    }
}
