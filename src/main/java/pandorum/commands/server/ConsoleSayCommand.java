package pandorum.commands.server;

import arc.util.Log;
import pandorum.annotations.ServerCommand;
import pandorum.discord.BotHandler;

import static pandorum.Misc.sendToChat;

public class ConsoleSayCommand {
    @ServerCommand(name = "say", args = "<message...>", description = "Send a message as a server.")
    public static void run(final String[] args) {
        sendToChat("commands.say.chat", args[0]);
        Log.info("Server: &ly@", args[0]);
        BotHandler.text("**Сервер**: @", args[0]);
    }
}
