package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import pandorum.discord.Bot;

import static pandorum.discord.Bot.botChannel;
import static pandorum.util.PlayerUtils.sendToChat;

public class SayCommand implements Cons<String[]> {
    public void get(String[] args) {
        Log.info("Сервер: &ly@", args[0]);
        sendToChat("commands.say.chat", args[0]);
        Bot.sendMessage(botChannel, "**Сервер** » @", args[0]);
    }
}
